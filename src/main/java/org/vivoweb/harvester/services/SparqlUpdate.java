/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;

/**
 * Execute Sparql update in Jena model in an instance of VIVO
 * using SparqlUpdate web service
 * @author John Fereira (jaf30@cornell.edu) 
 */
public class SparqlUpdate {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SparqlUpdate.class);
	/**
	 * Model to write to
	 */
	private String model;	 
	 
	/**
	 * input rdf file
	 */
	private String inRDF;
	
	/*
	 * VIVO admin user name
	 */
	private String username;
	
	/*
	 * VIVO admin password
	 */
	private String password; 
	
	/*
	 * Sparql update URL
	 */
	private String url;
	
	/*
	 * update type: add or delete
	 */
	
	private String type;
	 
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error parsing options
	 * @throws UsageException user requested usage message
	 */
	private SparqlUpdate(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	private SparqlUpdate(ArgList argList) throws IOException {
		 
		// setup output
		this.model = argList.get("m");
		
		// load any specified rdf file data
		this.inRDF = argList.get("r"); ;		
		 
		// output to file, if requested
		this.username = argList.get("u");
		
		// get password
		this.password = argList.get("p"); 
		
		// get url
		this.url = argList.get("U");
		
		// get update type
		this.type = argList.get("t");
		
		// Require model args
		if(this.model == null) {
			throw new IllegalArgumentException("Must provide an output model");
		}
		
		// Require input rdf 
		if (this.inRDF == null) {
			throw new IllegalArgumentException("Must provide an input rdf file name");
		}
		
		// Require user name 
		if (this.username == null) {
			throw new IllegalArgumentException("Must provide a VIVO admin username");
		}
		
		// Require password
		if (this.password == null) {
			throw new IllegalArgumentException("Must provide a VIVO admin password");
		}
		
		// Require sparql update url
		if (this.url == null) {
			throw new IllegalArgumentException("Must provide a Sparql Update URL");
		}
		
		if (this.type == null) {
			throw new IllegalArgumentException("Must provide an update type: add or delete");			
		}
		
		if (this.type.equalsIgnoreCase("add") || (this.type.equalsIgnoreCase("delete"))) {
		    // the type was specified as add or delete, that's good				
		} else {
			throw new IllegalArgumentException("The update type must be add or delete");	
		}
		
		 
	}
	
	/**
	 * Copy data from input to output
	 * @throws IOException error
	 */
	private void execute() throws IOException {
	    StringBuffer updateBuffer = new StringBuffer();
	    if (this.type.equals("add")) {
	       updateBuffer.append("INSERT DATA {");
	    } else {
		   updateBuffer.append("DELETE DATA {");
	    }
	    updateBuffer.append("GRAPH <").append(this.model).append("> {");
	  
	    //String rdfString = FileAide.getTextContent(this.inRDF);
	    Model model = ModelFactory.createDefaultModel();
		deduceRdfFormatAndParseData(model, new File(this.inRDF));

		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			model.write(out, "N-TRIPLES");
			updateBuffer.append(out.toString("UTF-8"));
		}
		updateBuffer.append("  }");
		updateBuffer.append("}");
		System.out.println(updateBuffer);

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(this.url);

            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("email", this.username));
            nvps.add(new BasicNameValuePair("password", this.password));
            nvps.add(new BasicNameValuePair("update", updateBuffer.toString()));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                System.out.println(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                try (InputStream is = entity.getContent()) {
                    IOUtils.copy(is, System.out);
                }

            }

        }
	}

	private void deduceRdfFormatAndParseData(Model model, File rdfFile) throws IOException {
		String[] supportedFormats = new String[]{"RDF/XML", "TURTLE", "N3"};
		for (String format : supportedFormats) {
			try (InputStream in = Files.newInputStream(rdfFile.toPath())) {
				try {
					model.read(in, null, format);
					return;
				} catch (Exception e) {
					// pass
				}
			}
		}

		throw new IOException("Unable to deduce RDF format for file: " + rdfFile.getName());
	}

	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SparqlUpdate");
		// Inputs
		
		parser.addArgument(new ArgDef().setShortOption('r').setLongOpt("rdf").withParameter(true, "RDF_FILE").setDescription("rdf filename to load into output model").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("username").withParameter(true, "USERNAME").setDescription("vivo admin user name").setRequired(true)); 
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("password").withParameter(true, "PASSWORD").setDescription("vivo admin password").setRequired(true)); 
		parser.addArgument(new ArgDef().setShortOption('U').setLongOpt("url").withParameter(true, "URL").setDescription("sparql update url").setRequired(true)); 
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("type").withParameter(true, "UPDATE TYPE").setDescription("type of update: add or delete").setRequired(true));
		// Outputs
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("model").withParameter(true, "MODEL").setDescription("name of jena model").setRequired(true)); 
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
			new SparqlUpdate(args).execute();
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
