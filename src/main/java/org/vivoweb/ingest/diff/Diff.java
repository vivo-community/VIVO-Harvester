package org.vivoweb.ingest.diff;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Diff {
	
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Diff.class);
	/**
	 * Model to read records from
	 */
	private JenaConnect input;
	/**
	 * Model to write records to
	 */
	private JenaConnect output;

	
	/**
	 * 
	 */
	public Diff(ArgList argList){
		
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Diff");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "URI_BASE").setDescription("use URI_BASE when importing relative uris").setRequired(false));
		
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dumptofile").withParameter(true, "FILENAME").setDescription("filename for output").setRequired(false));
		
		return parser;
	}
	
	
	/**
	 * 
	 */
	public void execute() {
		
		/*
		 * c - b = a
		 * minuend - subtrahend = difference
		 * 
		 * ie 	minuend.diff(subtrahend) = differenece
		 * 		c.diff(b) = a
		 */
		Model diffModel = ModelFactory.createDefaultModel();
		Model minuendModel = ModelFactory.createDefaultModel();
		Model subtrahendModel = ModelFactory.createDefaultModel();
		
		diffModel = minuendModel.difference(subtrahendModel);	
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log.info(getParser().getAppName()+": Start");
		try {
			new Diff(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.fatal(e.getMessage());
			System.out.println(getParser().getUsage());
		} catch(IOException e) {
			log.fatal(e.getMessage(), e);
			// System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.fatal(e.getMessage(), e);
		}
		log.info(getParser().getAppName()+": End");
	}

}
