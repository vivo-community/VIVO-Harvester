/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.jenaconnect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.cli.util.args.SimpleNodeParser;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.XMLAide;
import org.w3c.dom.Node;

/**
 * Connection Helper for Jena Models
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public abstract class JenaConnectFactory {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JenaConnectFactory.class);
	
	/**
	 * Config File Based Factory
	 * @param configNode the config node
	 * @param overrideParams the parameters to override the node's values with
	 * @return JenaConnect instance
	 * @throws IOException xml parse error
	 */
	public static JenaConnect parseConfig(Node configNode, Map<String, String> overrideParams) throws IOException {
		Map<String, String> paramList = new SimpleNodeParser().parse(configNode, null);
		if(overrideParams != null) {
			for(String key : overrideParams.keySet()) {
				paramList.put(key, overrideParams.get(key));
			}
		}
		for(String param : paramList.keySet()) {
			if(!param.equalsIgnoreCase("dbUser") && !param.equalsIgnoreCase("dbPass")) {
				log.trace("'" + param + "' - '" + paramList.get(param) + "'");
			}
		}
		return build(paramList);
	}
	
	/**
	 * Config Stream Based Factory that overrides parameters
	 * @param configStream the config input stream
	 * @param overrideParams the parameters to override the file's values with
	 * @return JenaConnect instance
	 * @throws IOException error connecting
	 */
	public static JenaConnect parseConfig(InputStream configStream, Map<String, String> overrideParams) throws IOException {
		return parseConfig(XMLAide.getDocumentNode(configStream), overrideParams);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFileName the config file path
	 * @return JenaConnect instance
	 * @throws IOException xml parse error
	 */
	public static JenaConnect parseConfig(String configFileName) throws IOException {
		return parseConfig(configFileName, null);
	}
	
	/**
	 * Config File Based Factory
	 * @param configFileName the config file path
	 * @param overrideParams the parameters to override the file with
	 * @return JenaConnect instance
	 * @throws IOException xml parse error
	 */
	public static JenaConnect parseConfig(String configFileName, Map<String, String> overrideParams) throws IOException {
		return parseConfig(FileAide.getInputStream(configFileName), overrideParams);
	}
	
	/**
	 * Build a JenaConnect based on the given parameter set
	 * @param params the value map
	 * @return the JenaConnect
	 * @throws IOException error connecting to jena model
	 */
	private static JenaConnect build(Map<String, String> params) throws IOException {
		// for(String param : params.keySet()) {
		// log.debug(param+" => "+params.get(param));
		// }
		if((params == null) || params.isEmpty()) {
			return null;
		}
		if(!params.containsKey("type")) {
			throw new IllegalArgumentException("Must specify 'type' parameter {'rdb','sdb','tdb','mem'}");
		}
		JenaConnect jc;
		if(params.get("type").equalsIgnoreCase("mem")) {
			jc = new MemJenaConnect(params.get("modelName"));
		} else if(params.get("type").equalsIgnoreCase("rdb")) {
			jc = new RDBJenaConnect(params.get("dbUrl"), params.get("dbUser"), params.get("dbPass"), params.get("dbType"), params.get("dbClass"), params.get("modelName"));
		} else if(params.get("type").equalsIgnoreCase("sdb")) {
			jc = new SDBJenaConnect(params.get("dbUrl"), params.get("dbUser"), params.get("dbPass"), params.get("dbType"), params.get("dbClass"), params.get("dbLayout"), params.get("modelName"));
		} else if(params.get("type").equalsIgnoreCase("tdb")) {
			jc = new TDBJenaConnect(params.get("dbDir"), params.get("modelName"));
		} else {
			throw new IllegalArgumentException("unknown type: " + params.get("type"));
		}
		if((!params.containsKey("checkEmpty") || (params.get("checkEmpty").toLowerCase() == "true")) && jc.isEmpty()) {
			JenaConnectFactory.log.warn("jena model empty database: " + ((params.get("type").equalsIgnoreCase("tdb"))?params.get("dbDir"):params.get("dbUrl")) + " modelName: " + jc.getModelName());
		}
		return jc;
	}
}
