/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * Merge multiple rdf files into one
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class Merge {
	/**
	 * SLF4J Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(Merge.class);
	/**
	 * Input RecordHandler
	 */
	private RecordHandler input;
	/**
	 * Output RecordHandler
	 */
	private RecordHandler output;
	/**
	 * Regex for finding primary records (with a grouping for the subsection to use to find sub-records)
	 */
	private Pattern regex;
	
	/**
	 * Constructor
	 * @param input input recordhandler
	 * @param output output recordhandler
	 * @param regex regex for finding primary records (with a grouping for the subsection to use to find sub-records)
	 */
	public Merge(RecordHandler input, RecordHandler output, String regex) {
		this.input = input;
		if(this.input == null) {
			throw new IllegalArgumentException("Must provide an input recordhandler");
		}
		this.output = output;
		if(this.output == null) {
			throw new IllegalArgumentException("Must provide an output recordhandler");
		}
		this.regex = Pattern.compile(regex);
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	public Merge(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList arguments
	 * @throws IOException error connecting to record handler
	 */
	public Merge(ArgList argList) throws IOException {
		this(
			RecordHandler.parseConfig(argList.get("i"), argList.getValueMap("I")), 
			RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")), 
			argList.get("b")
		);
	}
	
	/**
	 * Merge records in input using regex and write to output
	 * @param input input recordhandler
	 * @param output output recordhandler
	 * @param regex regex for finding primary records (with a grouping for the subsection to use to find sub-records)
	 * @throws IOException error in record handling
	 */
	public static void merge(RecordHandler input, RecordHandler output, Pattern regex) throws IOException {
		Map<String, String> matchedIDs = new HashMap<String, String>();
		log.info("Building List of Primary Records");
		for(Record r : input) {
			Matcher m = regex.matcher(r.getID());
			if(m.matches()) {
				log.debug("Matched record '" + r.getID() + "' => '" + m.group(1) + "'");
				matchedIDs.put(r.getID(), m.group(1));
			}
		}
		int count = matchedIDs.size();
		log.info("Building List Complete: Matched " + count + " records");
		log.info("Beginning Merging Records into Primary Records");
		int cur = 0;
		JenaConnect jc = new MemJenaConnect();
		for(String rid : new TreeSet<String>(matchedIDs.keySet())) {
			cur++;
			String matchTerm = matchedIDs.get(rid);
			log.debug("(" + cur + "/" + count + ": " + Math.round(10000f * cur / count) / 100f + "%): merging '" + matchTerm + "'");
			jc.truncate();
			for(String id : input.find(matchTerm)) {
				log.trace("Merging Record '" + id + "' into '" + matchTerm + "'");
				jc.loadRdfFromString(input.getRecord(id).getData(), null, null);
			}
			output.addRecord(matchTerm, jc.exportRdfToString(), Merge.class);
		}
		log.info("Merging into Primary Records Complete");
		jc.close();
	}
	
	/**
	 * Runs the merge
	 * @throws IOException error executing
	 */
	public void execute() throws IOException {
		merge(this.input, this.output, this.regex);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Merge");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		// Params
		parser.addArgument(new ArgDef().setShortOption('b').setLongOpt("baseRegex").withParameter(true, "REGEX").setDescription("match records using REGEX and use the first Group to find sub-records").setRequired(true));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new Merge(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}
