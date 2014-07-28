/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException; 

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
 
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils; 

/**
 * List instances of classes in an instance of VIVO
 * using ListRDF web service
 * @author John Fereira (jaf30@cornell.edu) 
 */
public class ListRDF {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ListRDF.class);
	 
	/*
	 *service URL
	 */
	private String url;
	
	/*
	 * VIVO admin user name
	 */
	private String username;
	
	/*
	 * VIVO admin password
	 */
	private String password; 
	
	/*
	 * vClass to be displayed
	 */
	private String vClass;
	
	/*
	 * format of the output
	 */
	private String format; 
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error parsing options
	 * @throws UsageException user requested usage message
	 */
	private ListRDF(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	private ListRDF(ArgList argList) throws IOException {
		
		// get service url
		this.url = argList.get("U");
		
		if (this.url == null) {
			throw new IllegalArgumentException("Must provide the service URL");
		}
		
		// get username
		this.username = argList.get("u");
				
		// get password
		this.password = argList.get("p");
		
		// Require user name 
		if (this.username == null) {
			throw new IllegalArgumentException("Must provide a VIVO admin username");
		}
				
		// Require password
		if (this.password == null) {
			throw new IllegalArgumentException("Must provide a VIVO admin password");
		}
		
		// get vClass
		this.vClass = argList.get("v");
				
		if (this.url == null) {
			throw new IllegalArgumentException("Must provide the name of a vClass");
		}
		
		// get format
		this.format = argList.get("f");
						
		if (this.format == null) {
		   this.format = "text";
		} 
		 
	}
	
	/**
	 * Copy data from input to output
	 * @throws IOException error
	 */
	private void execute() throws IOException {
	   //System.out.println("To be implemented");
	   
	  
	   CloseableHttpClient httpclient = HttpClients.createDefault();
	   try {
	      HttpPost httpPost = new HttpPost(this.url);
	      List <NameValuePair> nvps = new ArrayList <NameValuePair>();
	      nvps.add(new BasicNameValuePair("email", this.username));
	      nvps.add(new BasicNameValuePair("password", this.password));
	      
	      nvps.add(new BasicNameValuePair("vclass", this.vClass));
	      
	      // test if format was specified, default to text/plain
	      if (this.format.equals("ntriples")) {
	    	  httpPost.setHeader("Accept: ", "text/plain");	    	  
	      } else if (this.format.equals("rdfxml")) {
	    	  httpPost.setHeader("Accept: ", "application/rdf+xml");  
	      } else if (this.format.equals("n3")) {
	    	  httpPost.setHeader("Accept: ", "text/n3");  
	      } else if (this.format.equals("turtle")) {
	    	  httpPost.setHeader("Accept: ", "text/turtle");  
	      } else if (this.format.equals("json")) {
	    	  httpPost.setHeader("Accept: ", "application/json");  
	      }
	      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
	      CloseableHttpResponse response = httpclient.execute(httpPost);
	      try {
              System.out.println(response.getStatusLine());
              HttpEntity entity = response.getEntity();
              InputStream is = entity.getContent();
              try {
            	 IOUtils.copy(is, System.out);
              } finally {
                 is.close();
              }
              
          } finally {
              response.close();
          }
	       
	   } finally {
	      httpclient.close();	
	   }
	   
	   
	   
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("ListRDF");
		// Inputs
		
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vclass").withParameter(true, "VCLASS").setDescription("the vclass to be displayed").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("format").withParameter(true, "FORMAT").setDescription("the format of the output (text, ntriples, n3, rdfxml, json").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('U').setLongOpt("url").withParameter(true, "URL").setDescription("service url").setRequired(true));  
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("username").withParameter(true, "USERNAME").setDescription("vivo admin user name").setRequired(true)); 
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("password").withParameter(true, "PASSWORD").setDescription("vivo admin password").setRequired(true));
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
			new ListRDF(args).execute();
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
