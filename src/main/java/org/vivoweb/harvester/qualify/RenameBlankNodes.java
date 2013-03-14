/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * Find nodes with no name and give them a name
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class RenameBlankNodes {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RenameBlankNodes.class);
	/**
	 * The model to perform rename in
	 */
	private final JenaConnect inJC;
	/**
	 * The model to contain renamed nodes
	 */
	private final JenaConnect outJC;
	/**
	 * The part of the namespace between the base and the ID number
	 */
	private final String namespaceEtc;
	/**
	 * deduplication test model
	 */
	private final JenaConnect dedupJC;
	/**
	 * pattern
	 */
	private final String pattern;
	/**
	 * property
	 */
	private final String property;
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private RenameBlankNodes(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param inJC The model to perform rename in
	 * @param outJC The model to write output to
	 * @param namespaceEtc The part of the namespace between the base and the ID number
	 * @param dedupJC deduplication test model
	 * @param pattern pattern
	 * @param property property
	 */
	public RenameBlankNodes(JenaConnect inJC, JenaConnect outJC, String namespaceEtc, JenaConnect dedupJC, String pattern, String property) {
		this.inJC = inJC;
		this.outJC = outJC;
		this.namespaceEtc = namespaceEtc;
		this.dedupJC = dedupJC;
		this.pattern = pattern;
		this.property = property;
		if(this.inJC == null) {
			throw new IllegalArgumentException("Must provide an input jena model");
		}
		if(this.outJC == null) {
			throw new IllegalArgumentException("Must provide an output jena model");
		}
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error reading config
	 */
	private RenameBlankNodes(ArgList argList) throws IOException {
		this(
			JenaConnect.parseConfig(argList.get("i"), argList.getValueMap("I")),
			JenaConnect.parseConfig(argList.get("o"), argList.getValueMap("O")),
			argList.get("n"),
			JenaConnect.parseConfig(argList.get("d"), argList.getValueMap("D")),
			argList.get("t"),
			argList.get("p")
		);
	}
	
	/**
	 * Rename blank nodes
	 * @param inJC The model to perform rename in
	 * @param outJC The model to write output to
	 * @param namespaceEtc The part of the namespace between the base and the ID number
	 * @param dedupJC deduplication test model
	 * @param pattern pattern
	 * @param property property
	 */
	public static void renameBNodes(JenaConnect inJC, JenaConnect outJC, String namespaceEtc, JenaConnect dedupJC, String pattern, String property) {
		Model inModel = inJC.getJenaModel();
		Model outModel = outJC.getJenaModel();
		Property propertyRes = ResourceFactory.createProperty(property);
		OntModel dedupUnionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we're not using OWL here, just the OntModel submodel infrastructure
		dedupUnionModel.addSubModel(outModel);
		if (dedupJC != null) {
			Model dedupModel = dedupJC.getJenaModel();
			if (dedupModel != null) {
				dedupUnionModel.addSubModel(dedupModel);
			}
		}
		// the dedupUnionModel is so we can guard against reusing a URI in an 
		// existing model, as well as in the course of running this process
		inModel.enterCriticalSection(Lock.READ);
		Set<String> doneSet = new HashSet<String>();
		
		try {
			outJC.loadRdfFromJC(inJC);
			ClosableIterator<Resource> closeIt = inModel.listSubjects();
			try {
				for (Iterator<Resource> it = closeIt; it.hasNext();) {
					Resource res = it.next();
					if (res.isAnon() && !(doneSet.contains(res.getId()))) {
						// now we do something hacky to get the same resource in the outModel, since there's no getResourceById();
						ClosableIterator<Statement> closfIt = outModel.listStatements(res,propertyRes,(RDFNode)null);
						Statement stmt = null;
						try {
							if (closfIt.hasNext()) {
								stmt = closfIt.next();
							}
						} finally {
							closfIt.close();
						}
						if (stmt != null) {
							Resource outRes = stmt.getSubject();
							if(stmt.getObject().isLiteral()){
								RenameResources.renameResource(outRes,namespaceEtc+pattern+"_"+stmt.getObject().toString());
							}
							doneSet.add(res.getId().toString());
						}
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
	}

	/**
	 * Rename blank nodes
	 */
	public void execute() {
		renameBNodes(this.inJC, this.outJC, this.namespaceEtc, this.dedupJC, this.pattern, this.property);
		this.inJC.sync();
		this.outJC.sync();
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("RenameBlankNodes");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("outputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dedupModel").withParameter(true, "CONFIG_FILE").setDescription("optional: config file for deduplication test jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('D').setLongOpt("dedupModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of deduplication test jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespaceEtc").withParameter(true, "NAMESPACE_ETC").setDescription("part of the namespace between the base and the ID number").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("pattern").withParameter(true, "PATTERN").setDescription("pattern").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("property").withParameter(true, "PROPERTY").setDescription("property").setRequired(false));
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
			new RenameBlankNodes(args).execute();
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
