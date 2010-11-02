package org.vivoweb.ingest.transfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.IterableAdaptor;
import org.vivoweb.ingest.util.args.ArgDef;
import org.vivoweb.ingest.util.args.ArgList;
import org.vivoweb.ingest.util.args.ArgParser;
import org.vivoweb.ingest.util.repo.JenaConnect;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * Changes the namespace for all matching uris
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ChangeNamespace {
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(ChangeNamespace.class);
	/**
	 * The model
	 */
	private JenaConnect model;
	/**
	 * The old namespace
	 */
	private String oldNamespace;
	/**
	 * The new namespace
	 */
	private String newNamespace;

	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error reading config
	 * @throws SAXException error parsing config
	 * @throws ParserConfigurationException error parsing config 
	 */
	public ChangeNamespace(ArgList argList) throws ParserConfigurationException, SAXException, IOException {
		this.model = JenaConnect.parseConfig(argList.get("j"), argList.getProperties("J"));
		this.oldNamespace = argList.get("o");
		this.newNamespace = argList.get("n");
	}
	
	/**
	 * Gets an unused URI in the the given namespace for a model
	 * @param namespace the namespace
	 * @param model the model
	 * @return the uri
	 * @throws IllegalArgumentException empty namespace
	 */
	public static String getUnusedURI(String namespace, JenaConnect model) throws IllegalArgumentException {
		if(namespace == null || namespace.equals("")) {
			throw new IllegalArgumentException("namespace cannot be empty");
		}
		String uri = null;
		Random random = new Random();
		while(uri == null) {
			uri = namespace + "n" + random.nextInt(Integer.MAX_VALUE);
			if(model.containsURI(uri)) {
				uri = null;
			}
		}
		return uri;
	}
	
	/**
	 * Changes the namespace for all matching uris
	 * @param model the model to change namespaces for
	 * @param oldNamespace the old namespace
	 * @param newNamespace the new namespace
	 * @throws IllegalArgumentException empty namespace
	 */
	public static void changeNS(JenaConnect model, String oldNamespace, String newNamespace) throws IllegalArgumentException {
		if(oldNamespace == null || oldNamespace.equals("")) {
			throw new IllegalArgumentException("old namespace cannot be empty");
		}
		if(newNamespace == null || newNamespace.equals("")) {
			throw new IllegalArgumentException("new namespace cannot be empty");
		}
		if(oldNamespace.equals(newNamespace)) {
			return;
		}
		ArrayList<String> urlCheck = new ArrayList<String>();
		for(Resource res : IterableAdaptor.adapt(model.getJenaModel().listSubjects())) {
			if(oldNamespace.equals(res.getNameSpace())) {
				String uri = null;
				boolean urlFound = false;
				while (!urlFound) {
					uri = getUnusedURI(newNamespace, model);
					if (!urlCheck.contains(uri)) {
						urlCheck.add(uri);
						urlFound = true;
					}
				}
				ResourceUtils.renameResource(res, uri);
			}
		}
	}
	
	/**
	 * Change namespace
	 */
	private void execute() {
		changeNS(this.model, this.oldNamespace, this.newNamespace);
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ChangeNamespace");
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jenaModel").withParameter(true, "CONFIG_FILE").setDescription("config file for jena model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('J').setLongOpt("jenaModelOverride").withParameterProperties("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		
		// Params
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("oldNamespace").withParameter(true, "OLD_NAMESPACE").setDescription("The old namespace").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("newNamespace").withParameter(true, "NEW_NAMESPACE").setDescription("The new namespace").setRequired(true));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		log.info(getParser().getAppName()+": Start");
		try {
			new ChangeNamespace(new ArgList(getParser(), args)).execute();
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
