/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;

/**
 * Smush
 * @author Cornell University VIVO Team (Algorithm)
 * @author James Pence (jrpence@ufl.edu) (Harvester Tool)
 */
public class Smush {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Smush.class);
	/**
	 * model containing statements to be scored
	 */
	private JenaConnect inputJC;
	/**
	 * model in which to store temp copy of input and vivo data statements
	 */
	private JenaConnect outputJena;
	/**
	 * the predicates to look for in inputJC model
	 */
	private List<String> inputPredicates;
	/**
	 * limit match Algorithm to only match rdf nodes in inputJC whose URI begin with this namespace
	 */
	private String namespace;
	/**
	 * Change the input model to match the output model
	 */
	private boolean inPlace;
	
	/**
	 * Constructor
	 * @param inputJena model containing statements to be smushed
	 * @param outputJena model containing only resources about the smushed statements is returned
	 * @param inputPredicates the predicates to look for in inputJC model
	 * @param namespace limit match Algorithm to only match rdf nodes in inputJC whose URI begin with this namespace
	 * @param inPlace replace the input model with the output model
	 */
	public Smush(JenaConnect inputJena, JenaConnect outputJena, List<String> inputPredicates, String namespace, boolean inPlace) {
		if(inputJena == null) {
			throw new IllegalArgumentException("Input model cannot be null");
		}
		this.inputJC = inputJena;
		
		this.outputJena = outputJena;
		if(inputPredicates == null) {
			throw new IllegalArgumentException("Input Predicate cannot be null");
		}
		this.inputPredicates = inputPredicates;
		this.namespace = namespace;
		this.inPlace = inPlace;
	}
	
	/**
	 * Constructor
	 * @param inputJena model containing statements to be smushed
	 * @param inputPredicates the predicates to look for in inputJC model
	 * @param namespace limit match Algorithm to only match rdf nodes in inputJC whose URI begin with this namespace
	 */
	public Smush(JenaConnect inputJena, List<String> inputPredicates, String namespace) {
		this(inputJena, null, inputPredicates, namespace, true);
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 * @throws UsageException user requested usage message
	 */
	private Smush(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor Scoring.close();
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	private Smush(ArgList opts) throws IOException {
		this(
			JenaConnect.parseConfig(opts.get("i"), opts.getValueMap("I")), 
			JenaConnect.parseConfig(opts.get("o"), opts.getValueMap("O")), 
			opts.getAll("P"), 
			opts.get("n"), 
			opts.has("r")
		);
	}
	
	/**
	 * Get the ArgParser
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("Smush");
		// Models
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJC JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of inputJC jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("outputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJC JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of outputJena jena model config using VALUE").setRequired(false));
		
		// Parameters
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("inputJena-predicates").withParameters(true, "PREDICATE").setDescription("PREDICATE(s) on which, to match. Multiples are done in series not simultaineously.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "NAMESPACE").setDescription("only match rdf nodes in inputJC whose URI begin with NAMESPACE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("replace").setDescription("replace input model with changed / output model").setRequired(false));
		return parser;
	}
	
	/**
	 * A simple resource smusher based on a supplied inverse-functional property.
	 * @param inputJC - model to operate on
	 * @param subsJC model to hold subtractions
	 * @param addsJC model to hold additions
	 * @param property - property for smush
	 * @param ns - filter on resources addressed (if null then applied to whole model)
	 */
	public static void findSmushResourceChanges(JenaConnect inputJC, JenaConnect subsJC, JenaConnect addsJC, String property, String ns) {
		log.debug("Smushing on property <" + property + "> within "+((ns != null )?"namespace <"+ ns + ">":"any namespace"));
		Model inModel = inputJC.getJenaModel();
		Model subsModel = subsJC.getJenaModel();
		Model addsModel = addsJC.getJenaModel();
		Property prop = inModel.createProperty(property);
		inModel.enterCriticalSection(Lock.READ);
		try {
			NodeIterator objIt = inModel.listObjectsOfProperty(prop);
			try {
				while(objIt.hasNext()) {
					RDFNode obj = objIt.next();
					ResIterator subjIt = inModel.listSubjectsWithProperty(prop, obj);
					try {
						boolean first = true;
						Resource smushToThisResource = null;
						while (subjIt.hasNext()) {
							Resource subj = subjIt.next();
							if(subj.getNameSpace().equals(ns) || ns == null){
								if (first) {
									smushToThisResource = subj;
									first = false;
									log.debug("Smush running for <"+subj+">");
								} else {
									log.trace("Smushing <"+subj+"> into <"+smushToThisResource+">");
									StmtIterator stmtIt = inModel.listStatements(subj,(Property)null,(RDFNode)null);
									try {
										while(stmtIt.hasNext()) {
											Statement stmt = stmtIt.next();
											log.trace("Changing <"+stmt.getPredicate()+"> <"+stmt.getObject()+"> from <"+stmt.getSubject()+"> to <"+smushToThisResource+">");
											subsModel.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
											addsModel.add(smushToThisResource, stmt.getPredicate(), stmt.getObject());
										}
									} finally {
										stmtIt.close();
									}
									stmtIt = inModel.listStatements((Resource) null, (Property)null, subj);
									try {
										while(stmtIt.hasNext()) {
											Statement stmt = stmtIt.next();
											log.trace("Changing <"+stmt.getSubject()+"> <"+stmt.getPredicate()+"> from <"+stmt.getObject()+"> to <"+smushToThisResource+">");
											subsModel.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
											addsModel.add(stmt.getSubject(), stmt.getPredicate(), smushToThisResource);
										}
									} finally {
										stmtIt.close();
									}
								}
							}
						}
					} finally {
						subjIt.close();
					}
				}
			} finally {
				objIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
	}
	
	/**
	 * Execute is that method where the smushResoures method is ran for each predicate.
	 */
	public void execute() {
		JenaConnect subsJC = new MemJenaConnect();
		JenaConnect addsJC = new MemJenaConnect();
		for(String runName : this.inputPredicates) {
			findSmushResourceChanges(this.inputJC, subsJC, addsJC, runName, this.namespace);
		}
		if(this.inPlace){
			this.inputJC.removeRdfFromJC(subsJC);
			this.inputJC.loadRdfFromJC(addsJC);
		}
		if(this.outputJena != null) {
			this.outputJena.loadRdfFromJC(this.inputJC);
			this.outputJena.removeRdfFromJC(subsJC);
			this.outputJena.loadRdfFromJC(addsJC);
			this.outputJena.sync();
		}
		this.inputJC.sync();
	}

	/**
	 * Main method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new Smush(args).execute();
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
