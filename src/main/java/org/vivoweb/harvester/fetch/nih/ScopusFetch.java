/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch.nih;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.EFetchResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.PubmedArticleSet_type0;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import org.vivoweb.harvester.util.repo.SDBJenaConnect;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Module for fetching publications from Scopus (query by Scopus Author ID) and 
 * PubMed (query by DOI or Pubmed ID).
 * For detailed information, go to: 
 * https://sourceforge.net/apps/mediawiki/vivo/index.php?title=Scopus
 * @author Eliza Chan (elc2013@med.cornell.edu)
 */
public class ScopusFetch extends NIHFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ScopusFetch.class);
	
	/**
	 * The name of the PubMed database
	 */
	private static String database = "pubmed";

	/**
	 * a base xmlrecordoutputstream
	 */
	protected static XMLRecordOutputStream baseXMLROS = new XMLRecordOutputStream(new String[]{"PubmedArticle","PubmedBookArticle"}, "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2011//EN\" \"http://www.ncbi.nlm.nih.gov/entrez/query/DTD/pubmed_110101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<[pP][mM][iI][dD].*?>(.*?)</[pP][mM][iI][dD]>.*?", null);
	
	/**
	 * map that contains Scopus Author ID of researchers in VIVO
	 * key: http://vivo.med.cornell.edu/individual/cwid-sea2003,6602763271
	 */
	private HashMap<String, String> scopusIdMap = new HashMap<String, String>();
	
	private JenaConnect vivoJena;

	/**
	 * SPARQ query to retrieve people with Scopus ID
	 */
	private String sparqlQuery = null;

	/**
	 * Scopus X-ELS-APIKey for connection
	 */
	private String scopusApiKey;
	
	/**
	 * Scopus X-ELS-Authtoken for connection
	 */
	private String scopusAuthtoken;
	
	/**
	 * Scopus Accept for connection
	 */
	private String scopusAccept;
	
	/**
	 * Scopus publication start year
	 */
	private String scopusPubYearS;
	
	/**
	 * Scopus publication end year
	 */
	private String scopusPubYearE;
	
	/**
	 * Scopus affiliation list
	 */
	private ArrayList<String> scopusAffilList;

	/**
	 * Scopus publications linked to affiliation list.
	 */
	private String scopusAffilLinked = null;
	
	/**
	 * List to store pubmed documents.
	 */
	private ArrayList<Document> pubmedDocList = new ArrayList<Document>();
	
	/**
	 * List to make sure there are no duplications of Scopus Doc Ids in the ScopusBean maps
	 * across all authors.
	 */
	private ArrayList<String> scopusDocIdList = new ArrayList<String>();
	
	/**
	 * Constructor: Primary method for running a PubMed Fetch. The email address of the person responsible for this
	 * install of the program is required by NIH guidelines so the person can be contacted if there is a problem, such
	 * as sending too many queries too quickly.
	 * @param emailAddress contact email address of the person responsible for this install of the VIVO Harvester
	 * @param searchTerm query to run on pubmed data
	 * @param maxRecords maximum number of records to fetch
	 * @param batchSize number of records to fetch per batch
	 * @param rh record handler to write to
	 */
	public ScopusFetch(String emailAddress, String searchTerm, String maxRecords, String batchSize, RecordHandler rh) {
		super(emailAddress, searchTerm, maxRecords, batchSize, rh, database);
	}
	
	/**
	 * Constructor
	 * @param args commandline argument
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	private ScopusFetch(String[] args) throws IOException, UsageException {
		this(getParser("PubmedFetch", database).parse(args));
		ArgParser parser = new ArgParser("PubmedFetchIncrement");
		parser.addArgument(new ArgDef().setShortOption('q').setLongOpt("sparql").withParameter(true, "SPARQL_QUERY_FILE").setDescription("SPARQL query filename").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("scopus-pubyear-start").withParameter(true, "SCOPUS_PUBYEAR_START").setDescription("Scopus publication year start").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("scopus-pubyear-end").withParameter(true, "SCOPUS_PUBYEAR_END").setDescription("Scopus publication year end").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('v').setLongOpt("vivoJena-config").withParameter(true, "CONFIG_FILE").setDescription("vivoJena JENA configuration filename").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('k').setLongOpt("scopus-apikey").withParameter(true, "SCOPUS_APIKEY").setDescription("Scopus APIKey").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('a').setLongOpt("scopus-accept").withParameter(true, "SCOPUS_ACCEPT").setDescription("Scopus accept").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("scopus-affiliation").withParameter(true, "SCOPUS_AFFILIATION").setDescription("Scopus affiliation").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("scopus-affiliation-linked").withParameter(true, "SCOPUS_AFFILIATION_LINKED").setDescription("Scopus affiliation linked").setRequired(false));
		ArgList opts = parser.parse(args);
		this.vivoJena = JenaConnect.parseConfig(opts.get("v"), null);
		this.scopusApiKey = opts.get("k");
		this.scopusAccept = opts.get("a");
		
		if (opts.get("f") != null) {
			this.scopusAffilList = new ArrayList<String>(Arrays.asList(opts.get("f").split(",")));
		}
		this.scopusAffilLinked = opts.get("n");
		this.sparqlQuery = getFileContent(opts.get("q"));
		if (opts.get("s") != null && opts.get("e") != null) {
			this.scopusPubYearS = opts.get("s");
			this.scopusPubYearE = opts.get("e");			
		}
	}
	
	/**
	 * Constructor
	 * @param argList parsed argument list
	 * @throws IOException error creating task
	 */
	private ScopusFetch(ArgList argList) throws IOException {
		super(argList, database);
		
	}
	
	@Override
	public void execute() {
		
		// get scopus author id from vivo
		getVivoScopusId();
		
		if (this.scopusIdMap.size() > 0) {
			// connect to Scopus and get authtoken
			boolean connected = initScopusConnect();
	
			if (connected) {
				// iterate list of Scopus Author ID and get publication data
				log.info("Query Scopus by Author ID: Start");
				
				StringBuffer errMsg = new StringBuffer();
				for (String key : this.scopusIdMap.keySet()) {
					
					String[] keySplit = key.split(",");
					String scopusId = keySplit[1];
					log.info("scopusId: " + scopusId);
					
					// query Scopus by Author ID
					String scopusQueryResponse = scopusQueryByAuthorId(scopusId);
	
					// extract DOI from the query response and populate ScopusBeanMap
					HashMap<String, ScopusBean> sbMap = new HashMap<String, ScopusBean>();
					populateScopusBeanMap(scopusQueryResponse, sbMap, errMsg);

					
					// first round: query Pubmed and populate pubmedMap using Doi
					HashMap<String, String> pubmedMap = new HashMap<String, String>();
					if (errMsg.length() == 0) {
						pubmedQueryByDoi(sbMap, pubmedMap, errMsg);
					}
					
					// second round: 1) query Scopus by Doc ID to get Pubmed ID
					ArrayList<String> pmidList = new ArrayList<String>();
					if (errMsg.length() == 0) {
						scopusQueryByDocId(sbMap, pmidList);
					}
					
					// second round: 2) query Pubmed and populate pubmedMap using Pubmed ID
					if (errMsg.length() == 0) {
						pubmedQueryByPubmedId(pmidList, sbMap, pubmedMap, errMsg);
					}
	
					// finally: populate scopusMap with articles that are not found in Pubmed
					HashMap<String, String> scopusMap = new HashMap<String, String>();
					if (errMsg.length() == 0) {
						populateScopusMap(sbMap, scopusMap);
						
						// write to files
						writeToFiles(scopusId, pubmedMap, scopusMap, errMsg);
					}
				}

				log.info("Query Scopus by Author ID: End");
			}
		}

		// for test purpose
		//scopusQueryByAuthorId("36078494300"); // Shi, Lei
		//scopusQueryByAuthorId("35969977300"); // Adelman, Ronald D
		//scopusQueryByAuthorId("7102989382"); // Adelman, Ronald D
		//scopusQueryByAuthorId("23019591700"); // Salemi, Arash
		//scopusQueryByAuthorId("24435990700"); // Abramson, Erika
		//scopusQueryByAuthorId("35117492000"); // Dorff, Kevin
		//scopusQueryByAuthorId("6602462776"); // Campagne, Fabien
		//scopusQueryByAuthorId("35328914300"); delete this
	}
	
	/**
	 * Run SPARQL to retrieve Scopus Author ID
	 */
	private void getVivoScopusId() {
		// run sparql
		ResultSet rs = runSparql();
		while(rs.hasNext()) {
			String uri = null;
			String scopusId = null;
			QuerySolution qs = rs.next();
			Iterator<String> it = qs.varNames();
			while (it.hasNext()) {
				String key = (String)it.next();
				if (qs.get(key).isResource()) { // resource URI
					uri = qs.getResource(key).getURI();
				} else if (qs.get(key).isLiteral()) { // scopusId
					scopusId = qs.getLiteral(key).getString().replace("<p>", "").replace("</p>", "");
				}
			}

			if (uri != null && scopusId != null) {
				this.scopusIdMap.put(uri + "," + scopusId, null);
			}
		}
	}

	/**
	 * Necessary step to obtain authentication token
	 */
	private boolean initScopusConnect() {
		boolean connected = true;
		try {
			URL url = new URL("http://api.elsevier.com/authenticate?platform=SCOPUS");
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("X-ELS-APIKey", this.scopusApiKey);
			conn.setRequestProperty("Accept", this.scopusAccept);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer respBuf = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				respBuf.append(inputLine);
			}
			in.close();
			
			Document doc = loadXMLFromString(respBuf.toString());
			Node authtokenNode = doc.getElementsByTagName("authenticate-response").item(0);
			if (authtokenNode.getTextContent() != null) {
				this.scopusAuthtoken  = authtokenNode.getTextContent().trim();
			}
		} 
		catch (MalformedURLException e) {
			log.error("initScopusConnect MalformedURLException: ", e);
			connected = false;
		} 
		catch (IOException e) {
			log.error("initScopusConnect IOException: ", e);
			connected = false;
		} 
		catch (Exception e) {
			log.error("initScopusConnect Exception: ", e);
			connected = false;
		}
		return connected;
	}
	
	/**
	 * SPARQL to retrieve people from VIVO with Scopus ID
	 * @return
	 */
	private ResultSet runSparql() {
		ResultSet rs = null;
		StringBuilder sQuery = new StringBuilder();
		try {
			rs = this.vivoJena.executeSelectQuery(this.sparqlQuery);
			log.info(this.sparqlQuery);
			
		} catch (IOException e) {
			log.error(this.getClass().getName() + " execute IOException: " + e);
		}
		
		return rs;
	}
	
	/**
	 * Obtain Scopus metadata by querying Scopus Author ID
	 * The query String includes the publication start and end years to
	 * limit the results returned from Scopus.
	 * 
	 * @param String scopusId
	 * @return String
	 */
	private String scopusQueryByAuthorId(String scopusId) {
		
		int totalResults = 0;
		String pubYearStr = null;
		StringBuffer completeRespBuf = new StringBuffer();
		
		if (this.scopusPubYearS != null && this.scopusPubYearE != null) {
			pubYearStr = constructPubYearQStr(this.scopusPubYearS, this.scopusPubYearE);
		}

		try {
			
			String queryStr = "http://api.elsevier.com/content/search/index:SCOPUS?query=au-id(" + scopusId + ")";
			if (pubYearStr != null) {
				queryStr += "+AND+(" + pubYearStr + ")";
			}
			String respStr = urlConnect(queryStr);
			
			if (respStr.length() > 0) {
				Document doc = loadXMLFromString(respStr);
				NodeList nodes = doc.getChildNodes();
				NodeList resultsNodes = doc.getElementsByTagName("opensearch:totalResults");
				if (resultsNodes != null) {
					String resultsNodeVal = resultsNodes.item(0).getTextContent();
					totalResults = Integer.parseInt(resultsNodeVal);
					log.info("Total results for " + scopusId + " is " + totalResults);
				}
			}

			if (totalResults > 0) { 
				// query by counts
				int start = 0;
				int count = 200;
				int countList = 0;
				ArrayList<String> qStrList = constructCompleteQStr(queryStr, count, start, totalResults);
				for (String queryCompleteStr: qStrList) {
					log.info("Scopus query: " + queryCompleteStr);
					String eachRespStr = urlConnect(queryCompleteStr);
					if (countList > 0) {
						int entryIndex = eachRespStr.indexOf("<entry>");
						if (entryIndex > -1) {
							eachRespStr = eachRespStr.substring(entryIndex);
						}
					}
					completeRespBuf.append(eachRespStr.replace("</feed>", ""));
					countList++;
				}
				completeRespBuf.append("</feed>");
			}
		} catch (MalformedURLException e) {
			log.error("scopusQueryByAuthorId MalformedURLException: ", e);
		} 
		catch (IOException e) {
			log.error("scopusQueryByAuthorId IOException: ", e);
		} 
		catch (Exception e) {
			log.error("scopusQueryByAuthorId Exception: ", e);
		}
		return completeRespBuf.toString();
	}
	
	private String urlConnect(String queryStr) {
		StringBuffer respBuf = new StringBuffer();
		try {
			URL url = new URL(queryStr);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("X-ELS-APIKey", this.scopusApiKey);
			conn.setRequestProperty("X-ELS-Authtoken", this.scopusAuthtoken);
			conn.setRequestProperty("Accept", this.scopusAccept);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
	
			while ((inputLine = in.readLine()) != null) {
				respBuf.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
			log.error("urlConnect MalformedURLException: ", e);
		} 
		catch (IOException e) {
			log.error("urlConnect IOException: ", e);
		} 
		catch (Exception e) {
			log.error("urlConnect Exception: ", e);
		}
		return respBuf.toString();
	}

	/**
	 * Constructs publication year String
	 * Method declared public for test purposes
	 * @param startYear
	 * @param endYear
	 * @return
	 */
	public String constructPubYearQStr(String startYear, String endYear) {
		// e.g. PUBYEAR+IS+2010+OR+PUBYEAR+IS+2011
		StringBuffer qBuf = new StringBuffer();
		int s = Integer.parseInt(startYear);
		int e = Integer.parseInt(endYear);
		if (e > s) {
			for (int i=s; i<e+1; i++) {
				if (qBuf.length() > 0) {
					qBuf.append("+OR+");
				}
				qBuf.append("PUBYEAR+IS+" + String.valueOf(i));
			}
		} else {
			qBuf.append("PUBYEAR+IS+" + String.valueOf(s));
		}
		return qBuf.toString();
	}

	/**
	 * Constructs query String list
	 * Method declared public for test purposes
	 * @param queryStr
	 * @param count
	 * @param start
	 * @param totalResults
	 * @return
	 */
	public ArrayList<String> constructCompleteQStr(String queryStr, int count, 
			int start, int totalResults) {
		ArrayList<String> qStrList = new ArrayList<String>();
		while (start <= totalResults) {
			String queryCompleteStr = queryStr + "&count=" + count + "&start=" + start + "&view=COMPLETE";
			qStrList.add(queryCompleteStr);
			start += count;
		}
		return qStrList;
	}

	/**
	 * This method retrieves Pubmed ID by querying Scopus by Scopus Document ID
	 * @param sbMap
	 * @param pmidList
	 */
	private void scopusQueryByDocId(HashMap<String, ScopusBean> sbMap, ArrayList<String> pmidList) {
		
		try {
			Iterator<String> sbIter = sbMap.keySet().iterator();
			while (sbIter.hasNext()) {
				String key = sbIter.next();
				ScopusBean sb = sbMap.get(key);
				if (sb.getPubmedId() == null) {
					// get Pubmed ID from Scopus
					StringBuffer respBuf = new StringBuffer();
					String queryStr = "http://api.elsevier.com/content/abstract/SCOPUS_ID:" + sb.getScopusDocId() + "?view=META";
					URL url = new URL(queryStr);
					URLConnection conn = url.openConnection();
					conn.setRequestProperty("X-ELS-APIKey", this.scopusApiKey);
					conn.setRequestProperty("X-ELS-Authtoken", this.scopusAuthtoken);
					conn.setRequestProperty("Accept", this.scopusAccept);
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String inputLine;

					while ((inputLine = in.readLine()) != null) {
						respBuf.append(inputLine);
					}
					in.close();
					
					// log.info("query by scopus doc id: " + respBuf.toString());
					Document doc = loadXMLFromString(respBuf.toString());
					NodeList nodes = doc.getChildNodes();
					NodeList pubmedNodes = doc.getElementsByTagName("pubmed-id");
					if (pubmedNodes != null) {
						String pubmedNodeVal = pubmedNodes.item(0).getTextContent();
						if (!"".equals(pubmedNodeVal)) {
							pmidList.add(pubmedNodeVal);
							sb.setPubmedId(pubmedNodeVal);
						}
					} else {
						log.info("Pubmed ID not found in Scopus.");
					}
				}
			}

		} catch (MalformedURLException e) {
			log.error("scopusQueryByDocId MalformedURLException: ", e);
		} 
		catch (IOException e) {
			log.error("scopusQueryByDocId IOException: ", e);
		} 
		catch (Exception e) {
			log.error("scopusQueryByDocId Exception: ", e);
		}
	}

	/**
	 * This method extracts metadata from a Scopus feed and then populates the ScopusBean map.
	 * @param resp
	 * @param sbMap
	 */
	private void populateScopusBeanMap(String resp, HashMap<String, ScopusBean> sbMap, StringBuffer errMsg) {
		try {
			
			if (resp.length() > 0) {
				Document doc = loadXMLFromString(resp);
				NodeList entryNodes = doc.getElementsByTagName("entry");

				// populate ScopusBean map
				for (int e=0; e<entryNodes.getLength(); e++) {
					Node entryNode = entryNodes.item(e);
					String doi = null;
					String title = null;
					String scopusDocId = null;
					String issn = null;
					String volume = null;
					String issue = null;
					String pageRange = null;
					boolean withinAffil = false;
					ScopusBean sb = new ScopusBean();
					ArrayList<Node> authors = new ArrayList<Node>();
					NodeList entryChildNodes = entryNode.getChildNodes();
					for (int j=0; j<entryChildNodes.getLength(); j++) {
						Node entryChildNode = entryChildNodes.item(j);
						if ("dc:identifier".equals(entryChildNode.getNodeName())) {
							scopusDocId = entryChildNode.getTextContent().replace("SCOPUS_ID:", "");
						} else if ("author".equals(entryChildNode.getNodeName())) {
							authors.add(entryChildNode);
						} else if ("prism:doi".equals(entryChildNode.getNodeName())) {
							doi = entryChildNode.getTextContent();
						} else if ("dc:title".equals(entryChildNode.getNodeName())) {
							title = entryChildNode.getTextContent();
						} else if ("prism:issn".equals(entryChildNode.getNodeName())) {
							issn = entryChildNode.getTextContent();
						} else if ("prism:volume".equals(entryChildNode.getNodeName())) {
							volume = entryChildNode.getTextContent();
						} else if ("prism:issueIdentifier".equals(entryChildNode.getNodeName())) {
							issue = entryChildNode.getTextContent();
						} else if ("prism:pageRange".equals(entryChildNode.getNodeName())) {
							pageRange = entryChildNode.getTextContent();
						} else if ("affiliation".equals(entryChildNode.getNodeName())) {
							NodeList affilNodes = entryChildNode.getChildNodes();
							for (int k=0; k<affilNodes.getLength(); k++) {
								Node affilNode = affilNodes.item(k);
								if ("afid".equals(affilNode.getNodeName())) {
									String affil = affilNode.getTextContent();
									if (this.scopusAffilList != null && this.scopusAffilList.contains(affil)) {
										withinAffil = true;
										break;
									}
								}
							}
							
						}
					}
		
					sb.setScopusDocId(scopusDocId);
					sb.setDoi(doi);
					sb.setAuthors(authors);
					sb.setEntryNode(entryNode);
	
					// check affiliation
					boolean addToMap = false; // add to sbMap or not - default is false
					if (this.scopusAffilLinked != null) {
						if (("true".equals(this.scopusAffilLinked) && withinAffil) || 
							("false".equals(this.scopusAffilLinked) && !withinAffil)) {
							addToMap = true;
						}
					} else {
						addToMap = true; // add all publications to map, regardless of affiliated or not
					}
	
					// check if the article already exists in VIVO
					/* comment out
					boolean existsInVivo = false;
					if (sb.getDoi() != null) { // try doi
						existsInVivo = isDoiInVivo(sb.getDoi());
					}
					if (!existsInVivo) { // try Scopus Doc ID
						existsInVivo = isScopusDocIdInVivo(sb.getScopusDocId());
					}
					*/
					// add ScopusBean to map for Pubmed queries
					if (!this.scopusDocIdList.contains(sb.getScopusDocId())) {
						/*
						if (addToMap && !existsInVivo) {
							sbMap.put(sb.getScopusDocId(), sb);
						}
						*/
						if (addToMap && sb.getScopusDocId() != null) {
							sbMap.put(sb.getScopusDocId(), sb);
						}
						this.scopusDocIdList.add(sb.getScopusDocId());
					}
				}
			}
		} catch (MalformedURLException e) {
			log.error("populateScopusBeanMap MalformedURLException: ", e);
			errMsg.append(e + "\n");
		} 
		catch (IOException e) {
			log.error("populateScopusBeanMap IOException: ", e);
			errMsg.append(e + "\n");
		} 
		catch (Exception e) {
			log.error("populateScopusBeanMap Exception: ", e);
			errMsg.append(e + "\n");
		}
	}
	
	/**
	 * Query Pubmed by DOI
	 * @param sbMap
	 * @param pubmedMap
	 */
	private void pubmedQueryByDoi(HashMap<String, ScopusBean> sbMap, HashMap<String, String> pubmedMap, 
			StringBuffer errMsg) {

		StringBuffer searchTermBuf = new StringBuffer();
		try {
			Iterator<String> sbIter = sbMap.keySet().iterator();
			while (sbIter.hasNext()) {
				String key = sbIter.next();
				ScopusBean sb = sbMap.get(key);
				//if (sb.getDoi() != null && !isDoiInVivo(sb.getDoi())) {
				if (sb.getDoi() != null) {
					String searchDoi = "(" + sb.getDoi().replaceAll("[()]", "") + "[doi])";
					if (searchTermBuf.length() > 0) { searchTermBuf.append(" OR "); }
					searchTermBuf.append(searchDoi);
				}
			}
			//log.info(searchTermBuf.toString());
			populatePubmedMap(searchTermBuf.toString(), sbMap, pubmedMap, true, false, errMsg);
		} catch (Exception e) {
			log.error("pubmedQueryByDoi Exception: ", e);
			errMsg.append(e + "\n");
		}
	}

	/**
	 * Query Pubmed by Pubmed ID
	 * @param pmidList
	 * @param sbMap
	 * @param pubmedMap
	 */
	private void pubmedQueryByPubmedId(ArrayList<String> pmidList, HashMap<String, ScopusBean> sbMap, 
			HashMap<String, String> pubmedMap, StringBuffer errMsg) {
		StringBuffer searchTermBuf = new StringBuffer();
		for (String pmid: pmidList) {
			if (searchTermBuf.length() > 0) { searchTermBuf.append(" "); }
			searchTermBuf.append(pmid);
		}
		if (searchTermBuf.length() > 0) { searchTermBuf.append("[uid]"); }
		try {
			//log.info(searchTermBuf.toString());
			populatePubmedMap(searchTermBuf.toString(), sbMap, pubmedMap, false, true, errMsg);
		} catch (Exception e) {
			log.error("pubmedQueryByPubmedId Exception: ", e);
			errMsg.append(e + "\n");
		}
	}

	/**
	 * Populate Pubmed map with metadata from Pubmed
	 * @param searchTerm
	 * @param sbMap
	 * @param pubmedMap
	 */
	private void populatePubmedMap(String searchTerm, HashMap<String, ScopusBean> sbMap, 
			HashMap<String, String> pubmedMap, boolean lookupDoi, boolean lookupPmid,
			StringBuffer errMsg) {
		try {
			int recToFetch = getLatestRecord();
			int intBatchSize = Integer.parseInt(this.getBatchSize());
			this.setSearchTerm(searchTerm);
			String[] env = runESearch(this.getSearchTerm());
			
			// publication found in Pubmed
			if(env != null && !"null".equals(env[2]) && Integer.parseInt(env[2]) > 0) {
				fetchRecords(env, "0", "" + recToFetch);
				for (Document pubmedDoc: this.pubmedDocList) {
					if (lookupDoi) {
						populateMapByDoi(pubmedDoc, sbMap, pubmedMap);
					} else if (lookupPmid) {
						populateMapByPubmedId(pubmedDoc, sbMap, pubmedMap);
					}
				}
			}
		} catch (MalformedURLException e) {
			log.error("populatePubmedMap MalformedURLException: ", e);
			errMsg.append(e);
		} 
		catch (IOException e) {
			log.error("populatePubmedMap IOException: ", e);
			errMsg.append(e);
		} 
		catch (Exception e) {
			log.error("populatePubmedMap Exception: ", e);
			errMsg.append(e);
		}
	}

	/**
	 * Populate Pubmed map by looking up the DOI value from ScopusBean map
	 * @param pubmedDoc
	 * @param sbMap
	 * @param pubmedMap
	 */
	private void populateMapByDoi(Document pubmedDoc, HashMap<String, ScopusBean> sbMap, 
		HashMap<String, String> pubmedMap) {

		String doi = null;
		Node pubmedNode = pubmedDoc.getDocumentElement();
		NodeList articleIdNodes = pubmedDoc.getElementsByTagName("ArticleId");
		for (int j=0; j<articleIdNodes.getLength(); j++) {
			Node articleIdNode = articleIdNodes.item(j);
			NamedNodeMap attrs = articleIdNode.getAttributes();
			for (int i=0; i<attrs.getLength(); i++) {
				if ("doi".equals(attrs.item(i).getNodeValue())) {
					doi = articleIdNode.getTextContent();
				}
			}
		}
		
		if (doi != null) {
			// get pmid
			String pmid = null;
			NodeList pmidNodes = pubmedDoc.getElementsByTagName("PMID");
			if (pmidNodes != null) {
				pmid = pmidNodes.item(0).getTextContent();
			}
			Iterator<String> sbIter = sbMap.keySet().iterator();
			while (sbIter.hasNext()) {
				String key = sbIter.next();
				ScopusBean sb = sbMap.get(key);
				if (doi.equals(sb.getDoi())) {
					sb.setIsInPubmed(true);
					sb.setPubmedId(pmid);
					// get authors
					boolean completed = populateAuthId(pubmedDoc, sb);
					if (completed) {
						pubmedMap.put(pmid, nodeToString(pubmedDoc));
					} else {
						log.info("--- Cannot proceed with ingesting this article " + 
							"(DOI: " + doi + ") to VIVO, " + 
							"mismatch between authors in Scopus and Pubmed");
					}
					break; // found - no need to look any further
				}
			}
		}
	}

	/**
	 * Populate Pubmed map by looking up the Pubmed ID value from ScopusBean map
	 * @param pubmedDoc
	 * @param sbMap
	 * @param pubmedMap
	 */
	private void populateMapByPubmedId(Document pubmedDoc, HashMap<String, ScopusBean> sbMap, 
		HashMap<String, String> pubmedMap) {

		String pmid = null;
		NodeList pmidNodes = pubmedDoc.getElementsByTagName("PMID");
		if (pmidNodes != null) {
			pmid = pmidNodes.item(0).getTextContent();
		}
		Iterator<String> sbIter = sbMap.keySet().iterator();
		while (sbIter.hasNext()) {
			String key = sbIter.next();
			ScopusBean sb = sbMap.get(key);
			if (pmid.equals(sb.getPubmedId())) {
				sb.setIsInPubmed(true);
				// get authors
				boolean completed = populateAuthId(pubmedDoc, sb);
				if (completed) {
					pubmedMap.put(pmid, nodeToString(pubmedDoc));
				} else {
					log.info("--- Cannot proceed with ingesting this article " + 
						"(Pubmed ID: " + pmid + ") to VIVO, " + 
						"mismatch between authors in Scopus and Pubmed");
				}
				break; // found - no need to look any further
			}
		}
	}

	/**
	 * Populate Scopus map with Scopus metadata
	 * @param sbMap
	 * @param scopusMap
	 */
	private void populateScopusMap(HashMap<String, ScopusBean> sbMap, HashMap<String, String> scopusMap) {
		Iterator<String> sbIter = sbMap.keySet().iterator();
		while (sbIter.hasNext()) {
			String key = sbIter.next();
			ScopusBean sb = sbMap.get(key);
			if (!sb.isInPubmed()) {
				Node entryNode = sb.getEntryNode();
				scopusMap.put(sb.getScopusDocId(), nodeToString(entryNode));
			}
		}
	}

	/**
	 * Add Scopus Author ID to Pubmed metadata for ingest into VIVO
	 * @param pubmedDoc
	 * @param sb
	 * @return
	 */
	private boolean populateAuthId(Document pubmedDoc, ScopusBean sb) {
		boolean completed = true;

		// get Pubmed authors
		NodeList pubmedAuthorNodes = pubmedDoc.getElementsByTagName("Author");
		
		// get Scopus authors
		ArrayList<Node> scopusAuthorNodes = sb.getAuthors();
		
		boolean sameSize = false;
		
		if (pubmedAuthorNodes.getLength() == scopusAuthorNodes.size()) {
			sameSize = true;
		}
		
		if (sameSize) {
			if (pubmedAuthorNodes.getLength() == scopusAuthorNodes.size()) {
				for (int j=0; j<pubmedAuthorNodes.getLength(); j++) {
					Node pubmedAuthorNode = pubmedAuthorNodes.item(j);
					Node scopusAuthorNode = scopusAuthorNodes.get(j);
					NodeList scopusAuthorChildNodes = scopusAuthorNode.getChildNodes();
					String scopusAuthorId = null;
					String scopusAuthorName = null;
					for (int i=0; i<scopusAuthorChildNodes.getLength(); i++) {
						Node scopusAuthorChildNode = scopusAuthorChildNodes.item(i);
						if ("authid".equals(scopusAuthorChildNode.getNodeName())) {
							scopusAuthorId = scopusAuthorChildNode.getTextContent();
							Element scopusAuthorIdEl = pubmedDoc.createElement("authid");
							scopusAuthorIdEl.setTextContent(scopusAuthorId);
							pubmedAuthorNode.appendChild(scopusAuthorIdEl);
						}
					}
				}
			}
		} else {
			for (int j=0; j<pubmedAuthorNodes.getLength(); j++) {
				Node pubmedAuthorNode = pubmedAuthorNodes.item(j);
				NodeList pubmedAuthorChildNodes = pubmedAuthorNode.getChildNodes();
				for (int k=0; k<pubmedAuthorChildNodes.getLength(); k++) {
					Node pAuthCNode = pubmedAuthorChildNodes.item(k);
					if ("LastName".equals(pAuthCNode.getNodeName())) {
						String lastname = pAuthCNode.getTextContent();
						boolean foundLastname = false;
						// search lastname from Scopus nodes and get authid
						for (Node sAuthNode:scopusAuthorNodes) {
							NodeList sAuthCNodes = sAuthNode.getChildNodes();
							String authid = null;
							String authname = null;
							for (int i=0; i<sAuthCNodes.getLength(); i++) {
								Node sAuthCNode = sAuthCNodes.item(i);
								if ("authid".equals(sAuthCNode.getNodeName())) {
									authid = sAuthCNode.getTextContent();
								} else if ("authname".equals(sAuthCNode.getNodeName())) {
									authname = sAuthCNode.getTextContent();
								}
							}
							if (authname != null && 
								authname.toLowerCase().indexOf(lastname.toLowerCase()) > -1) {
								Element scopusAuthorIdEl = pubmedDoc.createElement("authid");
								scopusAuthorIdEl.setTextContent(authid);
								pubmedAuthorNode.appendChild(scopusAuthorIdEl);
								foundLastname = true;
								break; // lastname match found, no need to continue
							}
						}
						if (!foundLastname) {
							completed = false;
						//	log.info("--- Cannot proceed with ingesting this article to VIVO, " +
						//			"mismatch between authors in Scopus and Pubmed - lastname: " + lastname + " and DOI: "+ doi);
						}
					}
				}
			}
		}
		return completed;
	}

	/**
	 * Write query results to individual files using either Pubmed ID or Scopus Document ID as
	 * part of the file names.
	 * @param authid
	 * @param pubmedMap
	 * @param scopusMap
	 */
	private void writeToFiles(String authid, HashMap<String, String> pubmedMap, 
			HashMap<String, String> scopusMap, StringBuffer errMsg) {
		String header = "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2011//EN\" \"http://www.ncbi.nlm.nih.gov/entrez/query/DTD/pubmed_110101.dtd\">\n<PubmedArticleSet>\n";
		String footer = "\n</PubmedArticleSet>";
		
		try {
			Iterator<String> pubmedIter = pubmedMap.keySet().iterator();
			while (pubmedIter.hasNext()) {
				String pmid = pubmedIter.next();
				log.trace("Pubmed Writing to output");
				String sanitizedXml = pubmedMap.get(pmid).replaceAll("<\\?xml version=\".*?>", "");
				writeRecord(authid + "_" + pmid, header + sanitizedXml.trim() + footer);
				log.trace("Pubmed Writing complete");
			}
			
			Iterator<String> scopusIter = scopusMap.keySet().iterator();
			while (scopusIter.hasNext()) {
				String scopusDocId = scopusIter.next();
				String oriEntry = "<entry>";
				String modEntry = "<entry xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n" +
						"xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
						"xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" \n" +
						"xmlns:prism=\"http://prismstandard.org/namespaces/basic/2.0/\">\n";
				log.trace("Scopus Writing to output");
				writeRecord(authid + "_" + scopusDocId, scopusMap.get(scopusDocId).replace(oriEntry, modEntry));
				log.trace("Scopus Writing complete");
			}
		} catch (MalformedURLException e) {
			log.error("writeToFiles MalformedURLException: ", e);
			errMsg.append(e);
		} 
		catch (IOException e) {
			log.error("writeToFiles IOException: ", e);
			errMsg.append(e);
		} 
		catch (Exception e) {
			log.error("writeToFiles Exception: ", e);
			errMsg.append(e);
		}
	}

	/**
	 * Given the path to the filename, returns the file content.
	 * @param path - path to filename
	 * @return String
	 */
	private String getFileContent(String path) throws IOException {
		String strContent = null;
		if (path != null) {
			try {
			InputStream is = FileAide.getInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuffer buf = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				buf.append(line + "\n");
			}
			br.close();
			strContent = buf.toString();
			} catch (IOException e) {
				log.error("Could not get file content: ", e);
				throw new IOException("Could not get file content: ", e);
			}
		}
		return strContent;
	}

	/**
	 * Method copied from WOSFetch. Should be a utility method.
	 * @param documentNode a DOM node to be changed into a properly indented string
	 * @return The indented string containing the node and sub-nodes
	 */
	private String nodeToString(Node documentNode) {
		StreamResult result =null;
		try {
			Transformer	transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			
			result = new StreamResult(new StringWriter());
			DOMSource domSource = new DOMSource(documentNode);
			transformer.transform(domSource, result);
		} catch(TransformerException e) {
			e.printStackTrace();
		} catch(TransformerFactoryConfigurationError e1) {
			e1.printStackTrace();
		}
		
		return result.getWriter().toString();
	}
	
	/**
	 * Check if publication has already been ingested.
	 * If so, there is no need to re-ingest.
	 * @param doi
	 * @return
	 */
	/*
	private boolean isDoiInVivo(String doi) throws IOException {
		String query = "PREFIX bibo: <http://purl.org/ontology/bibo/doi> ASK  { ?x bibo:doi  \"" + doi + "\" }";
		boolean doiInVivo = this.vivoJena.executeAskQuery(query);
		if (doiInVivo) {
			log.trace("Document DOI: " + doi + " already exists in VIVO.");
		}
		return doiInVivo;
	}
	*/

	/**
	 * Check if publication has already been ingested.
	 * If so, there is no need to re-ingest.
	 * @param doi
	 * @return
	 */
	/*
	private boolean isScopusDocIdInVivo(String scopusDocId) throws IOException {
		String query = "PREFIX wcmc: <http://weill.cornell.edu/vivo/ontology/wcmc#> ASK  { ?x wcmc:scopusDocId  \"" + scopusDocId + "\" }";
		boolean idInVivo = this.vivoJena.executeAskQuery(query);
		if (idInVivo) {
			log.trace("Scopus Doc ID: " + scopusDocId + " already exists in VIVO.");
		}
		return idInVivo;
	}
	*/

	/**
	 * Extract DOI from Scopus feed
	 * @param resp
	 * @return
	 */
	private ArrayList extractDoi(String resp) {
		ArrayList<String> doiList = new ArrayList<String>();
		String doiTagStart = "<prism:doi>";
		String doiTagEnd = "</prism:doi>";
		String regex = doiTagStart + ".*?" + doiTagEnd;
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(resp);
		int c = 0;
		while (m.find()) {
			String doi = resp.substring(m.start(), m.end()).replace(doiTagStart, "").replace(doiTagEnd, "");
			doiList.add(doi);
			c++;
		}
		return doiList;
	}
	
	private Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}
	
	@Override
	public void fetchRecords(String WebEnv, String QueryKey, String retStart, String numRecords) 
			throws IOException {
		EFetchPubmedServiceStub.EFetchRequest req = new EFetchPubmedServiceStub.EFetchRequest();
		req.setQuery_key(QueryKey);
		req.setWebEnv(WebEnv);
		req.setEmail(getEmailAddress());
		req.setTool(getToolName());
		req.setRetstart(retStart);
		req.setRetmax(numRecords);
		int retEnd = Integer.parseInt(retStart) + Integer.parseInt(numRecords);
		log.info("Fetching " + retStart + " to " + retEnd + " records from search");
		try {
			serializeFetchRequest(req);
		} catch(RemoteException e) {
			throw new IOException("Could not run search: ", e);
		}
	}
	
	/**
	 * Runs, sanitizes, and outputs the results of a EFetch request to the xmlWriter
	 * <ol>
	 * <li>create a buffer</li>
	 * <li>connect to pubmed</li>
	 * <li>run the efetch request</li>
	 * <li>get the article set</li>
	 * <li>create XML writer</li>
	 * <li>output to buffer</li>
	 * <li>dump buffer to string</li>
	 * <li>use sanitizeXML() on string</li>
	 * </ol>
	 * @param req the request to run and output results
	 * @throws IOException Unable to write XML to record
	 */
	private void serializeFetchRequest(EFetchPubmedServiceStub.EFetchRequest req) throws IOException {
		//Create buffer for raw, pre-sanitized output
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		//Connect to pubmed
		EFetchPubmedServiceStub service = new EFetchPubmedServiceStub();
		//Run the EFetch request
		EFetchResult result = service.run_eFetch(req);
		//Get the article set
		PubmedArticleSet_type0 articleSet = result.getPubmedArticleSet();
		XMLStreamWriter writer;
		try {
			//Create a temporary xml writer to our buffer
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer);
			MTOMAwareXMLSerializer serial = new MTOMAwareXMLSerializer(writer);
			log.debug("Buffering records");
			//Output data
			articleSet.serialize(new QName("RemoveMe"), null, serial);
			serial.flush();
			log.debug("Buffering complete");
			log.debug("buffer size: " + buffer.size());
			//Dump buffer to String
			String iString = buffer.toString("UTF-8");
			// Sanitize string (which writes it to xmlWriter)
			sanitizeXML(iString);
		} catch(XMLStreamException e) {
			throw new IOException("Unable to write to output: ", e);
		} catch(UnsupportedEncodingException e) {
			throw new IOException("Cannot get xml from buffer: ", e);
		}
	}
	
	/**
	 * Sanitizes XML in preparation for writing to output stream
	 * <ol>
	 * <li>Removes xml namespace attributes</li>
	 * <li>Removes XML wrapper tag</li>
	 * <li>Splits each record on a new line</li>
	 * <li>Writes to outputstream writer</li>
	 * </ol>
	 * @param strInput The XML to Sanitize.
	 * @throws IOException Unable to write XML to record
	 */
	private void sanitizeXML(String strInput) throws IOException {
		log.debug("Sanitizing Output");
		log.debug("XML File Length - Pre Sanitize: " + strInput.length());
//		log.debug("====== PRE-SANITIZE ======\n"+strInput);
		String newS = strInput.replaceAll(" xmlns=\".*?\"", "");
		newS = newS.replaceAll("</?RemoveMe>", "");
		//TODO: this seems really hacky here... revise somehow?
		newS = newS.replaceAll("</PubmedArticle>.*?<PubmedArticle", "</PubmedArticle>\n<PubmedArticle");
		newS = newS.replaceAll("</PubmedBookArticle>.*?<PubmedBookArticle", "</PubmedBookArticle>\n<PubmedBookArticle");
		newS = newS.replaceAll("</PubmedArticle>.*?<PubmedBookArticle", "</PubmedArticle>\n<PubmedBookArticle");
		newS = newS.replaceAll("</PubmedBookArticle>.*?<PubmedArticle", "</PubmedBookArticle>\n<PubmedArticle");
		log.debug("XML File Length - Post Sanitze: " + newS.length());
//		log.debug("====== POST-SANITIZE ======\n"+newS);
		log.debug("Sanitization Complete");
		
		// Eliza: instead of writing to output, need to modify pubmed by incorporating
		// some scopus author data. After that, it can be written to output.
		String closingTag = "</PubmedArticle>";
		String[] pubmedXmls = newS.split(closingTag);
		this.pubmedDocList = new ArrayList<Document>(); // reset
		try {
			for (String eachItem: pubmedXmls) {
				String pubmedXml = eachItem + closingTag + "\n";
				this.pubmedDocList.add(loadXMLFromString(pubmedXml));
			}
		} catch (MalformedURLException e) {
			throw new IOException("sanitizeXML MalformedURLException: ", e);
		} catch (IOException e) {
			throw new IOException("sanitizeXML IOException: ", e);
		} catch (Exception e) {
			throw new IOException("sanitizeXML Exception: ", e);
		}
	}
	
	@Override
	protected int getLatestRecord() throws IOException {
		return Integer.parseInt(runESearch("1:8000[dp]", false)[3]);
	}
	
	@Override
	public void writeRecord(String id, String data) throws IOException {
		log.trace("Adding Record "+id);
		boolean docExists = false;
		String pmid = "";
		if (id.contains("_")) {
			String[] idSplit = id.split("_");
			pmid = idSplit[1];
		}
		if (this.vivoJena != null) {
			
			try {

				/*
				String askQuery = "PREFIX bibo: <http://purl.org/ontology/bibo/> ASK  { ?x bibo:pmid  \"" + pmid + "\" }";
				
				// look up pmid
				docExists = this.vivoJena.executeAskQuery(askQuery);
				
				if (!docExists) {
					log.trace("Adding Record "+id);
					getRh().addRecord(id, data, getClass());
				} else {
					log.trace("Record " + id + " already exists in VIVO. No further action is needed.");
				}
				*/
				log.trace("Adding Record "+id);
				getRh().addRecord(id, data, getClass());

			} catch (MalformedURLException e) {
				throw new IOException("writeRecord MalformedURLException: " + e);
			} 
			catch (IOException e) {
				throw new IOException("writeRecord IOException: " + e);
			} 
			catch (Exception e) {
				throw new IOException("writeRecord Exception: " + e);
			}
		}
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser("PubmedFetch", database));
			log.info("PubmedFetch: Start");
			new ScopusFetch(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info("PubmedFetch: End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
	/**
	 * Class to hold lookup data for each publication retrieved from Scopus.
	 * @author elc2013
	 *
	 */
	private class ScopusBean {
		private boolean isInPubmed = false; // default
		private String scopusDocId;
		private String doi;
		private String issn;
		private String title;
		private String volume;
		private String issue;
		private String pageRange;
		private String pmid;

		private Node entryNode;
		private ArrayList<Node> authors = new ArrayList<Node>();
		
		// setters
		public void setIsInPubmed(boolean isInPubmed) {
			this.isInPubmed = isInPubmed;
		}
		
		public void setScopusDocId(String scopusDocId) {
			this.scopusDocId = scopusDocId;
		}
		
		public void setAuthors(ArrayList<Node> authors) {
			this.authors = authors;
		}
		
		public void setDoi(String doi) {
			this.doi = doi;
		}
		
		public void setIssn(String issn) {
			this.issn = issn;
		}
		
		public void setTitle(String title) {
			this.title = title;
		}
		
		public void setVolume(String volume) {
			this.volume = volume;
		}
		
		public void setIssue(String issue) {
			this.issue = issue;
		}
		
		public void setPageRange(String pageRange) {
			this.pageRange = pageRange;
		}

		public void setEntryNode(Node entryNode) {
			this.entryNode = entryNode;
		}
		
		public void setPubmedId(String pmid) {
			this.pmid = pmid;
		}
		
		// getters
		public boolean isInPubmed() {
			return this.isInPubmed;
		}
		
		public String getScopusDocId() {
			return this.scopusDocId;
		}
		
		public ArrayList<Node> getAuthors() {
			return this.authors;
		}
		
		public String getDoi() {
			return this.doi;
		}
		
		public String getIssn() {
			return this.issn;
		}
		
		public String getTitle() {
			return this.title;
		}
		
		public String getVolume() {
			return this.volume;
		}
		
		public String getIssue() {
			return this.issue;
		}
		
		public String getPageRange() {
			return this.pageRange;
		}

		public String getPubmedId() {
			return this.pmid;
		}

		public Node getEntryNode() {
			return this.entryNode;
		}
	}
}
