/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch.linkeddata;

import java.io.IOException; 
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Model;

import org.vivoweb.harvester.fetch.linkeddata.discovery.DiscoverUrisUsingListrdf;
import org.vivoweb.harvester.fetch.linkeddata.discovery.DiscoveryWorker;
import org.vivoweb.harvester.fetch.linkeddata.discovery.DiscoveryWorkerException;
import org.vivoweb.harvester.fetch.linkeddata.service.HttpLinkedDataService;
import org.vivoweb.harvester.fetch.linkeddata.service.LinkedDataService;
import org.vivoweb.harvester.fetch.linkeddata.util.http.BasicHttpWorker;
//import org.vivoweb.harvester.fetch.linkeddata.util.http.BasicHttpWorkerRequest;
import org.vivoweb.harvester.fetch.linkeddata.util.http.HttpWorker;
import org.vivoweb.harvester.fetch.linkeddata.util.http.HttpWorkerRequest;
//import org.vivoweb.harvester.fetch.linkeddata.util.http.HttpWorkerRequest.Method;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler; 
//import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
import org.w3c.dom.Document;
 

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
	
	/*
	 * optional rich export include dirs
	 */
	Iterable<String> includes;
	
	 
	
	/**
	 * Model to write records to
	 */
	private JenaConnect output;
	
	
	 
	 
	/**
	 * The record handler to write records to
	 */
	private RecordHandler rhOutput;
	
	/**
	 * format of the output
	 */
	private String format;
	 
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
    
    
	 
    public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}



	protected static final int TIMEOUT = 100;
    
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
			RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")),
			argList.getAll("I"),
			argList.get("f")
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
		
		if (this.format == null) {
			this.format = "rdfxml";
		}
		
		 
		
	}
	
	 
	public LinkedDataFetch(String url, List<String> vClasses, RecordHandler recordHandler, List<String> includes, String format) {
		this.url = url;
		this.vClasses = vClasses;
		this.rhOutput = recordHandler;
		this.includes = includes;
		this.format = format;
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
		 
		CloseableHttpClient httpclient = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager)
                .build();
		 
		HttpWorker httpWorker = new BasicHttpWorker(httpclient);		 
		DiscoveryWorker discovery = new DiscoverUrisUsingListrdf(this.vClasses, httpWorker);
		
		LinkedDataService linkedDataService = new HttpLinkedDataService(httpclient);
		int recid = 0;
		int total = 0;
		try {
		   Iterable<String> uris = discovery.getUrisForSite(this.url);
		   String individualID = new String();
		   for (String uri: uris) {
			   // kludge to exclude admin uri
			   if (StringUtils.endsWith(uri, "#admin")) {
				   // this is the admin user, exclude it and continue
				   continue;
			   }
			   individualID = StringUtils.substringAfterLast(uri, "/");
			   //String lduri = uri + "?format=" + this.format + "&include=all";
			   StringBuffer incBuf = new StringBuffer();
			   
			   if (this.includes != null ) {
				   
				   for (String dirName: this.includes) {
					  incBuf.append("&include="+ dirName);   
				   }
			   }
			   String lduri = new String();
			   if (incBuf.length() > 0) {
			      lduri = uri + "/"+ individualID +".rdf"+ incBuf.toString().replaceFirst("&", "?");
			   } else {
				  lduri = uri + "/"+ individualID +".rdf"; 
			   }
			   //log.info("uri: "+uri);
			   log.info("lduri: "+lduri);
			   try {
			      String linkedData = linkedDataService.getLinkedData(lduri );
			      //String linkedData =  getLinkedDataRDF(lduri, httpWorker);
			      //String linkedData =  getLinkedData(lduri );
			      //log.info(linkedData);
			      this.rhOutput.addRecord("ID_" + recid, linkedData, this.getClass());
			      recid++; total++;
			   } catch (Exception ex) {
				  log.error(ex.getMessage());   
			   }
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
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	private static String getLinkedDataRDF(String uri, HttpWorker httpWorker) throws Exception {
		
		 HttpWorkerRequest req = httpWorker.get(uri).accept("application/rdf+xml");
		 return req.asString().execute().toString();
		 
	} 
	
	/**
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	private static String getLinkedData(String uri) throws Exception {
		Header header = new BasicHeader(HttpHeaders.ACCEPT, "application/rdf+xml");
		RequestConfig requestConfig = RequestConfig.custom()
	        .setConnectTimeout(10000)
	        .setConnectionRequestTimeout(10000)
	        .setSocketTimeout(10000)
	        .build();
	    CloseableHttpClient httpclient = HttpClientBuilder.create()
	        .disableAuthCaching()
	        .disableAutomaticRetries()
	        .disableConnectionState()
	        .disableContentCompression()
	        .disableCookieManagement()
	        .disableRedirectHandling()
	        .setDefaultRequestConfig(requestConfig)
	        .build();
		//CloseableHttpClient httpclient = HttpClients.createDefault();
		  
		StringWriter writer = new StringWriter();
		String ld = new String();
		try {
			HttpGet get = new HttpGet(uri);
			get.addHeader(header);
			//List <NameValuePair> nvps = new ArrayList <NameValuePair>(); 
		    //nvps.add(new BasicNameValuePair("email", "jaf30@cornell.edu"));
		    //nvps.add(new BasicNameValuePair("password", "vivoadmin"));
		    
			
			CloseableHttpResponse response = httpclient.execute(get);
			try {
				if( response == null )
	                throw new Exception("HTTP response for " +uri+ " was null.");
	            if( response != null &&
	                response.getStatusLine() != null &&
	                response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	                throw new Exception("could not get HTTP for " + uri +
	                                    " status: " + response.getStatusLine() );
	            }
				HttpEntity entity = response.getEntity();
				InputStream is = entity.getContent();
				try {
					IOUtils.copy(is, writer, "UTF-8");
					ld = writer.toString();
				} finally {
					is.close();
				}
				
			} finally {
				response.close();
			}
			
		} finally {
			httpclient.close();
		}
		return ld;
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
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("include").withParameterValueMap("INCLUDE", "dir").setDescription("rich export includes, use all for all directories").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("format").withParameter(true, "FORMAT").setDescription("output format").setRequired(false));
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
