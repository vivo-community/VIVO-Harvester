/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch.linkeddata;

import java.io.IOException; 
import java.util.List;


import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import org.vivoweb.harvester.fetch.linkeddata.discovery.DiscoverUrisUsingListrdf;
import org.vivoweb.harvester.fetch.linkeddata.discovery.DiscoveryWorker;
import org.vivoweb.harvester.fetch.linkeddata.discovery.DiscoveryWorkerException;
import org.vivoweb.harvester.fetch.linkeddata.service.HttpLinkedDataService;
import org.vivoweb.harvester.fetch.linkeddata.service.LinkedDataService;
import org.vivoweb.harvester.fetch.linkeddata.util.http.BasicHttpWorker;
import org.vivoweb.harvester.fetch.linkeddata.util.http.HttpWorker;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler; 
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
 

/**
 * Class for harvesting from OAI Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class LinkedDataFetch  {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(LinkedDataFetch.class);
	
	/*
	 *service URL
	 */
	private String url;
	
	/*
	 * vClass to be displayed
	 */
	Iterable<String> vClasses;
	
	 
	
	/**
	 * Model to write records to
	 */
	private JenaConnect output;
	
	
	 
	 
	/**
	 * The record handler to write records to
	 */
	private RecordHandler rhOutput;
	 
	/**
     * @return the rhOutput
     */
    public RecordHandler getRhOutput() {
        return this.rhOutput;
    }

    /**
     * @param rhOutput the rhOutput to set
     */
    public void setRhOutput(RecordHandler rhOutput) {
        this.rhOutput = rhOutput;
    }
	 
    protected static final int TIMEOUT = 50;
    
	/**
	 * Constructor
	 * @param args command line arguments
	 * @throws IOException error connecting to record handler
	 * @throws UsageException user requested usage message
	 */
	private LinkedDataFetch(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/*protected LinkedDataFetch(String url, <List>String vClasses, RecordHandler recordHandler) {
		this.rhOutput = recordHandler;  
		this.url = url;
		this.vClasses = vClasses;		 
	}*/
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error connecting to record handler
	 */
	private LinkedDataFetch(ArgList argList) throws IOException {
		 
		this(
			argList.get("U"),
			argList.getAll("v"),
			RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")) 
		);
		 
		// Require record handler
		if (this.rhOutput == null) {
			throw new IllegalArgumentException("Must provide an output {-o, -O}");
		}
		
		// get service url
		//this.url = argList.get("U");
		
		if (this.url == null) {
			throw new IllegalArgumentException("Must provide the service URL");
		}
		
		// get vClass
		 
		//this.vClasses = argList.getAll("v");		
		if (this.vClasses == null) {
			throw new IllegalArgumentException("Must provide at least one name for a vClass");
		}
	}
	
	 
	public LinkedDataFetch(String url, List<String> vClasses, RecordHandler recordHandler) {
		this.url = url;
		this.vClasses = vClasses;
		this.rhOutput = recordHandler;
	}

	/**
	 * Executes the task
	 * @throws IOException error getting recrords
	 */
	public void execute() throws IOException {
		// Model m = output.getJenaModel();
		HttpClientConnectionManager connManager = new BasicHttpClientConnectionManager();
		RequestConfig requestConfig = RequestConfig.custom()
	            .setSocketTimeout(TIMEOUT * 1000)
	            .setConnectTimeout(TIMEOUT * 1000)
	            .build();
		//CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpClient httpclient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .build();
		 
		HttpWorker httpWorker = new BasicHttpWorker(httpclient);		 
		DiscoveryWorker discovery = new DiscoverUrisUsingListrdf(this.vClasses, httpWorker);
		//LinkedDataService linkedDataService = new ModelLinkedDataService(m);
		LinkedDataService linkedDataService = new HttpLinkedDataService(httpclient);
		int recid = 0;
		int total = 0;
		try {
		   Iterable<String> uris = discovery.getUrisForSite(this.url);
		   
		   for (String uri: uris) {
			   log.debug(uri);
			   String linkedData = linkedDataService.getLinkedData(uri);
			   log.debug(linkedData);
			   this.rhOutput.addRecord("ID_" + recid, linkedData, this.getClass());
			   recid++; total++;
		   }
		} catch (DiscoveryWorkerException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);	
		} catch (Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
		} finally {
		    httpclient.close();	
		}
		 
		log.info("Added " + total + " Records");
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("LinkedDataFetch"); 
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('U').setLongOpt("url").withParameter(true, "URL").setDescription("service url").setRequired(true)); 
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vclass").withParameterValueMap("VCLASS", "TYPE").setDescription("the vclasses to be displayed").setRequired(true));
		return parser;
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
			new LinkedDataFetch(args).execute();
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
