/******************************************************************************************************************************
 * Harvester Tool Copyright (c) 2011 Christopher Haines, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 ******************************************************************************************************************************
 * Algorithm Copyright (c) 2011, Cornell University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of Cornell University nor the names of its contributors
 *       may be used to endorse or promote products derived from this software 
 *       without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************************************************************/
package org.vivoweb.harvester.qualify;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

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
	private JenaConnect inputJena;
	/**
	 * model in which to store temp copy of input and vivo data statements
	 */
	private JenaConnect outputJena;
	/**
	 * the predicates to look for in inputJena model
	 */
	private List<String> inputPredicates;
	/**
	 * limit match Algorithm to only match rdf nodes in inputJena whose URI begin with this namespace
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
	 * @param inputPredicates the predicates to look for in inputJena model
	 * @param namespace limit match Algorithm to only match rdf nodes in inputJena whose URI begin with this namespace
	 * @param inPlace replace the input model with the output model
	 */
	public Smush(JenaConnect inputJena, JenaConnect outputJena, List<String> inputPredicates, String namespace, boolean inPlace) {
		if(inputJena == null) {
			throw new IllegalArgumentException("Input model cannot be null");
		}
		this.inputJena = inputJena;
		
		this.outputJena = outputJena;
		if(this.outputJena == null) {
			log.info("Output model null generating a memory model");
			this.outputJena = new MemJenaConnect();
		}
		
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
	 * @param inputPredicates the predicates to look for in inputJena model
	 * @param namespace limit match Algorithm to only match rdf nodes in inputJena whose URI begin with this namespace
	 */
	public Smush(JenaConnect inputJena, List<String> inputPredicates, String namespace) {
		this(inputJena, null, inputPredicates, namespace, true);
	}
	
	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 */
	public Smush(String... args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor Scoring.close();
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	public Smush(ArgList opts) throws IOException {
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
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJena JENA configuration filename").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of inputJena jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("outputJena-config").withParameter(true, "CONFIG_FILE").setDescription("inputJena JENA configuration filename").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of inputJena jena model config using VALUE").setRequired(false));
		
		// Parameters
		parser.addArgument(new ArgDef().setShortOption('P').setLongOpt("inputJena-predicates").withParameters(true, "PREDICATE").setDescription("PREDICATE on which, to match").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "NAMESPACE").setDescription("only match rdf nodes in inputJena whose URI begin with NAMESPACE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rename").setDescription("replace input model with changed / output model").setRequired(false));
		return parser;
	}
	

	
	/**
	 * A simple resource smusher based on a supplied inverse-functional property.
	 * @param inJC - model to operate on
	 * @param property - property for smush
	 * @param namespace - filter on resources addressed (if null then applied to whole model)
	 * @return a new model containing only resources about the smushed statements.
	 */
	public static JenaConnect smushResources(JenaConnect inJC, String property, String namespace) { 
		JenaConnect outJC = new MemJenaConnect();
		Model inModel = inJC.getJenaModel();
		Property prop = inModel.createProperty(property);
		Model outModel = outJC.getJenaModel();
		outModel.add(inModel);
		inModel.enterCriticalSection(Lock.READ);
		try {
			ClosableIterator<RDFNode> closeIt = inModel.listObjectsOfProperty(prop);
			try {
				for (Iterator<RDFNode> objIt = closeIt; objIt.hasNext();) {
					RDFNode rdfn = objIt.next();
					ClosableIterator<Resource> closfIt = inModel.listSubjectsWithProperty(prop, rdfn);
					try {
						boolean first = true;
						Resource smushToThisResource = null;
						for (Iterator<Resource> subjIt = closfIt; closfIt.hasNext();) {
							Resource subj = subjIt.next();
							if(! (subj.getNameSpace().equals(namespace) || namespace == null ) ){
								continue;
							}
							if (first) {
								smushToThisResource = subj;
								first = false;
								continue;
							}
							
							ClosableIterator<Statement> closgIt = inModel.listStatements(subj,(Property)null,(RDFNode)null);
							try {
								for (Iterator<Statement> stmtIt = closgIt; stmtIt.hasNext();) {
									Statement stmt = stmtIt.next();
									outModel.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
									outModel.add(smushToThisResource, stmt.getPredicate(), stmt.getObject());
								}
							} finally {
								closgIt.close();
							}
							closgIt = inModel.listStatements((Resource) null, (Property)null, subj);
							try {
								for (Iterator<Statement> stmtIt = closgIt; stmtIt.hasNext();) {
									Statement stmt = stmtIt.next();
									outModel.remove(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
									outModel.add(stmt.getSubject(), stmt.getPredicate(), smushToThisResource);
								}
							} finally {
								closgIt.close();
							}
						}
					} finally {
						closfIt.close();
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
		return outJC;
	}
	
	/**
	 * Execute is that method where the smushResoures method is ran for each predicate.
	 */
	public void execute() {
		Model outModel = this.outputJena.getJenaModel();
		for(String runName : this.inputPredicates) {
			JenaConnect results = smushResources(this.inputJena, runName, this.namespace);
			outModel.add(results.getJenaModel());
		}
		if(this.inPlace){
			this.inputJena.truncate();
			this.inputJena.loadRdfFromJC(this.outputJena);
		}
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
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
}
