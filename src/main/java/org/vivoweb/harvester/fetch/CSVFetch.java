/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * -- several methods in this class are borrowed from the VIVO Csv2Rdf.java file written by Cornell University
 ******************************************************************************/
package org.vivoweb.harvester.fetch;


import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.qualify.ChangeNamespace;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.SpecialEntities;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.JenaRecordHandler;
import org.vivoweb.harvester.util.repo.RecordHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;

//TODO add to library org.skife.csv
import org.skife.csv.CSVReader;
import org.skife.csv.SimpleReader;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Fetches data from a CSV file and uses the VIVO CSV import parameters to load RDF Data
 * into a triple store model.
 * @author Stephen V. Williams (svwilliams@gmail.com)
 */
public class CSVFetch {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(CSVFetch.class);

	/**
	 * 
	 */
	private JenaConnect rh;
	/**
	 * csv file to be process
	 */
	private String file;
	/**
	 * the namespace for all individuals created
	 */
	private String namespace;
	/**
	 * the namespace for all properties created
	 */
	private String tboxNamespace;
	/**
	 * 
	 */
	private String typeName;
	/**
	 * 
	 */
	private String individualNameBase;
	/**
	 * 
	 */
	private String propertyNameBase;
	
	private String uriProperty;
	/**
	 * 
	 */
	private char separatorChar;
    /**
     * 
     */
    private char[] quoteChars;
	
	private int indexOfURIProp = -1;
	
	/**
	 * @param filename the csv file to process
	 * @param namespace the namespace for all properties and 
	 * @param output the 
	 * @throws Exception 
	 */
	public CSVFetch(String filename, String namespace, JenaConnect output) throws Exception{
		this(filename, ",", namespace, "http://www.w3.org/2002/07/owl#Thing", namespace, null, null, output);
	}
	
	/**
	 * Command line Constructor
	 * @param args commandline arguments
	 * @throws Exception 
	 * @throws IllegalArgumentException 
	 */
	private CSVFetch(String[] args) throws IllegalArgumentException, Exception {
		this(getParser().parse(args));
	}
	
	/**
	 * Arglist Constructor
	 * @param args option set of parsed args
	 * @throws Exception 
	 */
	private CSVFetch(ArgList args) throws Exception {	
		this(
			args.get("f"),
			args.get("s"),
			args.get("n"),
			args.get("t"), 
			args.get("u"),
			args.get("b"),
			args.get("p"),
			JenaConnect.parseConfig(args.get("o"), args.getValueMap("O"))
		);
	}
	
	/**
	 * Library style Constructor
	 * @param fileName the file path to the CSV file
	 * @param separatorValue the value used to seperate columns (usually a ',')
	 * @param namespace the namespace for all of the properties created
	 * @param localClass the class to assign each row to
	 * @param uriPrefix the namespace of the uri for each subject (may be different due to computer value)
	 * @param uriPattern the pattern to assign before (such as fis) the unique identifier
	 * @param uriField  the field (if used) to draw the unique number from
	 * @param output the jennaconnect to a model to output the new RDF
	 * @throws Exception 
	 */
	public CSVFetch(String fileName, String separatorValue, String namespace, String localClass, String uriPrefix, String uriPattern, String uriField, JenaConnect output) throws Exception {
		
		if (separatorValue.length() != 1) {
			throw new Exception("Invalid Seperator Value: it must be a single character");
		}
		this.rh = output;
		this.separatorChar = separatorValue.charAt(0);
		this.namespace = namespace;
		this.tboxNamespace = namespace;
		this.file = fileName;
		this.typeName = localClass;
		if (uriPrefix == null) {
			this.individualNameBase = "";
		} else {
			this.individualNameBase = uriPrefix;
		}
		this.propertyNameBase = uriPattern;
		this.uriProperty = uriField;
	}
	
	/**
	 * @throws IOException 
	 */
	public void execute() throws IOException {
		//WebappDaoFactory wadf
		InputStream fis = new FileInputStream(this.file);
		Model destination = this.rh.getJenaModel();
		
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		OntModel tboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontModel.addSubModel(tboxOntModel);
        OntClass theClass = tboxOntModel.createClass(this.tboxNamespace+this.typeName);
		
        CSVReader cReader = new SimpleReader();
		cReader.setSeperator(this.separatorChar);
		//cReader.setQuoteCharacters(this.quoteChars);	
		/*
		URIGenerator uriGen = (wadf != null && destination != null) 
				? new RandomURIGenerator(wadf, destination)
		       : new SequentialURIGenerator();
		*/
		List<String[]> fileRows = cReader.parse(fis);
		
		String[] columnHeaders = fileRows.get(0);
		
        DatatypeProperty[] dpArray = new DatatypeProperty[columnHeaders.length];

        for (int i=0; i<columnHeaders.length; i++) {
            dpArray[i] = tboxOntModel.createDatatypeProperty(this.tboxNamespace+this.propertyNameBase+columnHeaders[i].replaceAll("\\W",""));

            //setting the column id to generate URI
            if (this.uriProperty != null && this.uriProperty.equals(columnHeaders[i].toString())){
            	this.indexOfURIProp = i;
            }
            System.out.println(dpArray[i].toString());
        }
        
        
        Individual ind = null;
        for (int row=1; row<fileRows.size(); row++) {    	
        	
        	String[] cols = fileRows.get(row);
	        if (this.indexOfURIProp != -1){
	        	String uri = this.namespace+this.individualNameBase+cols[this.indexOfURIProp].trim();
	        	ind = ontModel.createIndividual(uri, theClass);
	        } else {
	        	String uri = ChangeNamespace.getUnusedURI(this.namespace+"/individual/n", ontModel);
	        	ind = ontModel.createIndividual(uri, theClass);
	        }
	        for (int col=0; col<cols.length; col++) {
				String value = cols[col].trim();
	            if (value.length()>0) {
	                ind.addProperty(dpArray[col], value); // no longer using: , XSDDatatype.XSDstring);
	                // TODO: specification of datatypes for columns
	            }
	        }
        }
        
        ontModel.removeSubModel(tboxOntModel);
		
		//Model[] resultModels = new Model[2];
		//resultModels[0] = ontModel;
		//resultModels[1] = tboxOntModel;
		destination.add(ontModel);
		destination.add(tboxOntModel);
		System.out.println(this.rh.exportRdfToString("RDF/XML"));
		destination.close();
		//return resultModels;
	}
	

	

	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("CSVFetch");
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("file").withParameter(true, "CSV_FILE").setDescription("csv file to import").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('s').setLongOpt("seperated").withParameter(true, "SEP_VARIABLE").setDescription("seperation method, automatically set to ',' ").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "NAMESPACE_BASE").setDescription("the base namespace to use for each node created").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("type").withParameter(true, "LOCAL_CLASS").setDescription("local name of the class that all rows will be set as").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('u').setLongOpt("uriPrefix").withParameter(true, "URI_PREFIX").setDescription("the prefix to add infront of the uri").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('b').setLongOpt("propertyPrefix").withParameter(true, "URI_PATTERN_BASE").setDescription("the field to base the URI on").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("uriParameter").withParameter(true, "URI_PATTERN_BASE").setDescription("the ur pattern base").setRequired(false));
		
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
	
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
			new CSVFetch(args).execute();
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
	
	/**
	 * @author stwi5210
	 *
	 */
	private interface URIGenerator {
		public String getNextURI();
	}
	
//	private class RandomURIGenerator implements URIGenerator {
//		
//		//private WebappDaoFactory wadf;
//		
//		//private Random random = new Random(System.currentTimeMillis());
//		
//		//public RandomURIGenerator(WebappDaoFactory webappDaoFactory, Model destination) {
//		public RandomURIGenerator(Model destination) {
//		
//			//this.wadf = webappDaoFactory;
//			//this.destination = destination;
//		}
//		
////		public String getNextURI() {
////			boolean uriIsGood = false;
////			boolean inDestination = false;
////			String uri = null;
////	        int attempts = 0;
////	        Model destination = new Model();
////	        Random random = new Random(System.currentTimeMillis());
////			if(namespace!=null && !namespace.isEmpty()){
////        		while( uriIsGood == false && attempts < 30 ){	
////        			uri = namespace+individualNameBase+random.nextInt( Math.min(Integer.MAX_VALUE,(int)Math.pow(2,attempts + 13)) );
////        			//String errMsg = wadf.checkURI(uri);
////        			Resource res = ResourceFactory.createResource(uri);
////        			inDestination = destination.contains(res, null);
////        			//if( errMsg != null && !inDestination)
////        			if(!inDestination)
////        				uri = null;
////        			else
////        				uriIsGood = true;				
////        			attempts++;
////        		}
////        	}
////        	return uri;
////		}
//		
//	}
	
	/**
	 * @author stwi5210
	 *
	 */
	//private class SequentialURIGenerator implements URIGenerator {
	//	private int index = 0;
	//	
	//	public String getNextURI() {
	//		index++;
	//		return namespace + individualNameBase + Integer.toString(index); 
	//	}
	//}
}
