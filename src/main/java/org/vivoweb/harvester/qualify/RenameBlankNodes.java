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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
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
import com.hp.hpl.jena.util.ResourceUtils;
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
	 */
	public RenameBlankNodes(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
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
	public RenameBlankNodes(ArgList argList) throws IOException {
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
		Model dedupModel = dedupJC.getJenaModel();
		Property propertyRes = ResourceFactory.createProperty(property);
		OntModel dedupUnionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we're not using OWL here, just the OntModel submodel infrastructure
		dedupUnionModel.addSubModel(outModel);
		if (dedupModel != null) {
			dedupUnionModel.addSubModel(dedupModel);
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
								ResourceUtils.renameResource(outRes,namespaceEtc+pattern+"_"+stmt.getObject().toString());
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
