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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.JenaConnect;

/**
 * Find nodes with no name and give them a name
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class RenameBlankNodes {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ChangeNamespace.class);
	/**
	 * The model to perform rename in
	 */
	private final JenaConnect inModel;
	/**
	 * The model to contain renamed nodes
	 */
	private final JenaConnect outModel;
	/**
	 * The part of the namespace between the base and the ID number
	 */
	private final String namespaceEtc;
	/**
	 * dedupModel
	 */
	private final JenaConnect dedupModel;
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
	 * @param inModel The model to perform rename in
	 * @param outModel The model to write output to
	 * @param namespaceEtc The part of the namespace between the base and the ID number
	 * @param dedupModel dedupModel
	 * @param pattern pattern
	 * @param property property
	 */
	public RenameBlankNodes(JenaConnect inModel, JenaConnect outModel, String namespaceEtc, JenaConnect dedupModel, String pattern, String property) {
		this.inModel = inModel;
		this.outModel = outModel;
		this.namespaceEtc = namespaceEtc;
		this.dedupModel = dedupModel;
		this.pattern = pattern;
		this.property = property;
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
	 * @param inModel The model to perform rename in
	 * @param outModel The model to write output to
	 * @param namespaceEtc The part of the namespace between the base and the ID number
	 * @param dedupModel dedupModel
	 * @param pattern pattern
	 * @param property property
	 */
	@SuppressWarnings("unused")
	public static void renameBNodes(JenaConnect inModel, JenaConnect outModel, String namespaceEtc, JenaConnect dedupModel, String pattern, String property) {
		//TODO mbarbieri: complete
	}

	/**
	 * Rename blank nodes
	 */
	public void execute() {
		renameBNodes(this.inModel, this.outModel, this.namespaceEtc, this.dedupModel, this.pattern, this.property);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("RenameBlankNodes");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for input jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of input jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("outputModel").withParameter(true, "CONFIG_FILE").setDescription("config file for output jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of output jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("dedupModel").withParameter(true, "CONFIG_FILE").setDescription("optional: config file for dedup jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('D').setLongOpt("dedupModelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of dedup jena model config using VALUE").setRequired(false));
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
