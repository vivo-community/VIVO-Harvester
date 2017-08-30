/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.fetch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator; 
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.SpecialEntities;
import org.vivoweb.harvester.util.WebAide;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.util.repo.RecordStreamOrigin;
import org.vivoweb.harvester.util.repo.XMLRecordOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.sparql.resultset.ResultSetFormat;
import org.apache.jena.sparql.resultset.ResultsFormat;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
 

/**
 * Class for harvesting from JSON Data Sources
 * @author Dale Scheppler
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class SparqlFetch implements RecordStreamOrigin {
    /**
     * SLF4J Logger
     */
    private static Logger log = LoggerFactory.getLogger(SparqlFetch.class);
	//private static Log log = LogFactory.getLog(SparqlFetch.class);

    /**
     * Sparql endpoint
     */
    private String endpoint;
    
    /*
     * fileName containing SPARQL
     */
    private String fileName;

    /**
     * The record handler to write records to
     */
    private RecordHandler rhOutput;

        /**
     * Namespace for RDF made from this database
     */
    private String uriNS;

    public String queryResultFormat;

    /*
     * Some getters and setters
     */


    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getQueryResultFormat() {
		return queryResultFormat;
	}

	public void setQueryResultFormat(String queryResultFormat) {
		this.queryResultFormat = queryResultFormat;
	}

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


    /**
     * @return namespace URI
     */
    public String getUriNS() {
        return this.uriNS;
    }

    /**
     * @param uriNS namespace URI
     */
    public void setUriNS(String uriNS) {
        this.uriNS = uriNS;
    }


    /**
     * the base for each instance's xmlRos
     */
    private static XMLRecordOutputStream xmlRosBase = new XMLRecordOutputStream(new String[]{"record"}, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><harvest>", "</harvest>", ".*?<identifier>(.*?)</identifier>.*?", null);

    /**
     * Constructor
     * @param args command line arguments
     * @throws IOException error connecting to record handler
     * @throws UsageException user requested usage message
     */
    private SparqlFetch(String[] args) throws IOException, UsageException {
        this(getParser().parse(args));
    }

    /**
     * Constructor
     * @param argList parsed argument list
     * @throws IOException error connecting to record handler
     */
    /**
     * @param args list of arguments
     * @throws IOException an exception
     */
    private SparqlFetch(ArgList args) throws IOException {

       this(
          args.get("e"), // endpoint
          RecordHandler.parseConfig(args.get("o")), // output override
          args.get("n"), // namespace
          args.get("f"),
          args.get("Q")// namespace
       );

    }

    /**
     * Constructor
     * @param endpoins Sparql Endpoint
     * @param rhOutput The recordhandler to write to
     * @param uriNS the default namespace
     * @param fileName file containing sparql 
     */
    public SparqlFetch(String endpoint, RecordHandler rhOutput, String uriNS, String fileName, String queryResultFormat) {
        setEndpoint(endpoint);
        setRhOutput(rhOutput);
        setUriNS(uriNS);
        setFileName(fileName);
        setQueryResultFormat(queryResultFormat);

        

    }


    /**
     * Get the ArgParser for this task
     * @return the ArgParser
     */
    private static ArgParser getParser() {
        ArgParser parser = new ArgParser("SparqlFetch");

        parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").setDescription("RecordHandler config file path").withParameter(true, "CONFIG_FILE"));
        parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));

        // harvester specific arguments
        parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("endpoint").setDescription("sparql endpoint ").withParameter(true, "ENDPOINT").setRequired(true));
        parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("fileName").setDescription("file containing sparql ").withParameter(true, "FILENAME").setRequired(true));
        parser.addArgument(new ArgDef().setShortOption('Q').setLongOpt("queryResultFormat").withParameter(true, "RESULT_FORMAT").setDescription("the format to return the results in ('RS_RDF',etc for select queries / 'RDF/XML',etc for construct/describe queries)").setRequired(false));
		
        parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespaceBase").withParameter(true, "NAMESPACE_BASE").setDescription("the base namespace to use for each node created").setRequired(false));
        

        return parser;
    }
    
    /**
	 * RDF formats
	 */
	 
	/*protected static HashMap<String, ResultSetFormat> formatSymbols = new HashMap<String, ResultSetFormat>();
    static {
            formatSymbols.put(ResultSetFormat.syntaxXML.getSymbol(), ResultSetFormat.syntaxXML);
            formatSymbols.put(ResultSetFormat.syntaxRDF_XML.getSymbol(), ResultSetFormat.syntaxRDF_XML);
            formatSymbols.put(ResultSetFormat.syntaxRDF_N3.getSymbol(), ResultSetFormat.syntaxRDF_N3);
            formatSymbols.put(ResultSetFormat.syntaxCSV.getSymbol(), ResultSetFormat.syntaxCSV);
            formatSymbols.put(ResultSetFormat.syntaxText.getSymbol(), ResultSetFormat.syntaxText);
            formatSymbols.put(ResultSetFormat.syntaxJSON.getSymbol(), ResultSetFormat.syntaxJSON);
    }*/
    protected static HashMap<String, ResultsFormat> formatSymbols = new HashMap<String, ResultsFormat>();
    static { 
    	    
            formatSymbols.put(ResultsFormat.FMT_RS_XML.getSymbol(), ResultsFormat.FMT_RS_XML);
            formatSymbols.put(ResultsFormat.FMT_RDF_XML.getSymbol(), ResultsFormat.FMT_RDF_XML);
            formatSymbols.put(ResultsFormat.FMT_RDF_N3.getSymbol(), ResultsFormat.FMT_RDF_N3);
            formatSymbols.put(ResultsFormat.FMT_RS_CSV.getSymbol(), ResultsFormat.FMT_RS_CSV);
            formatSymbols.put(ResultsFormat.FMT_TEXT.getSymbol(), ResultsFormat.FMT_TEXT);
            formatSymbols.put(ResultsFormat.FMT_RS_JSON.getSymbol(), ResultsFormat.FMT_RS_JSON);
    }



    /**
     * Builds a json node record namespace
     * @param nodeName the node to build the namespace for
     * @return the namespace
     */
    private String buildNodeRecordNS(String nodeName) {
        return this.uriNS + nodeName;
    }

    /**
     * Builds a table's field description namespace
     * @param nodeName the node to build the namespace for
     * @return the namespace
     */
    private String buildNodeFieldNS(String nodeName) {
        return this.uriNS + "fields/" + nodeName + "/";
    }

    /**
     * Builds a node type description namespace
     * @param nodeName the node to build the namespace for
     * @return the namespace
     */
    private String buildNodeTypeNS(String nodeName) {
        return this.uriNS + "types#" + nodeName;
    }


    /**
     * Executes the task
     * @throws IOException error getting recrords
     */
    public void execute() throws IOException {
        log.info("Execute SparqlFetch");
         

        try {
            XMLRecordOutputStream xmlRos = xmlRosBase.clone();
            xmlRos.setRso(this);

            if (this.endpoint == null) {
            	System.out.println(getParser().getUsage());
            	System.exit(1);
            }
            String qs = FileAide.getTextContent(fileName);
            //log.info(qs);
            QueryExecution qexec = null;
            ArrayList<HashMap> reslist = null;
            String rsString = new String();
            ResultsFormat rsf = null;
            try {
                Query query = QueryFactory.create(qs);
                qexec = QueryExecutionFactory.sparqlService(this.endpoint, query);
                reslist = new ArrayList<HashMap>();
                rsf = formatSymbols.get(this.queryResultFormat);
                if (rsf == null) {
                     rsf = ResultsFormat.FMT_TEXT;
                }
                ResultSet resultSet = qexec.execSelect();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                //OutputStream outputStream = System.out;
            	 
            	if (resultSet.hasNext()) {
            	   ResultSetFormatter.output(outputStream, resultSet, rsf);
            	   rsString = outputStream.toString("UTF-8");
            	    
            	} else {
            		System.out.println("Empty result set.");
            	} 



             } catch (Exception ex) {
                //ex.printStackTrace();
                log.error("Exception while doing sparql query ", ex);
                throw ex;
             } finally {
                if (qexec != null) {
                   qexec.close();
                }
             }
            
            
            log.info(rsString);
            log.info("resultSetFormat: "+rsf.getSymbol());
            //if (rsf.getSymbol().equals(ResultSetFormat.syntaxJSON)) {
            if (rsf.getSymbol().equals(ResultsFormat.FMT_RS_JSON)) {
               if (StringUtils.isNotEmpty(rsString)) {
                  JSONObject jsonObject  = (JSONObject) JSONValue.parse(rsString);
                  JSONObject resultsJson = (JSONObject) jsonObject.get("results");
                  Iterator iter = resultsJson.keySet().iterator();
                  while (iter.hasNext()) {
                    String key = (String) iter.next();
                    Object val = resultsJson.get(key);
                    //System.out.println("key: "+ key + ", value: "+val);
                  }
            
               } else {
            	System.err.println("Spqrql Query did not return any results");
               }
            }
            /* int count = 0;
            String name = new String();
            StringBuilder sb = new StringBuilder();
            //log.info("fixedkey: "+ fixedkey);
            StringBuilder recID = new StringBuilder();
            recID.append("node_-_");
            recID.append(String.valueOf(count));

            //log.trace("Creating RDF for "+name+": "+recID);
            // Build RDF BEGIN
            // Header info
            String nodeNS = "node-" + name;
            sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>\n");
            sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
            sb.append("         xmlns:");
            sb.append(nodeNS);
            sb.append("=\"");
            sb.append(buildNodeFieldNS(name));
            sb.append("\"\n");
            sb.append("         xml:base=\"");
            sb.append(buildNodeRecordNS(name));
            sb.append("\">\n");

            // Record info BEGIN
            sb.append("  <rdf:Description rdf:ID=\"");
            sb.append(recID);
            sb.append("\">\n");

            // insert type value
            sb.append("    <rdf:type rdf:resource=\"");
            sb.append(buildNodeTypeNS(name));
            sb.append("\"/>\n");

            while (iter.hasNext()) {
                //String key = (String) iter.next();
                //Object val = jsonObject.get(key);
                String fixedkey = key.replaceAll(" ","_");

                // Field BEGIN

                String field = nodeNS + ":" + fixedkey;
                sb.append("    <");
                sb.append(SpecialEntities.xmlEncode(field));
                sb.append(">");

                // insert field value
                // insert an empty string if the val is null
                if (val == null) {
                   log.error("val is null for key: "+key);
                   sb.append(""); // put in an empty string
                } else {
                   sb.append(SpecialEntities.xmlEncode(val.toString().trim()));
                }
                // Field END
                sb.append("</");
                sb.append(SpecialEntities.xmlEncode(field));
                sb.append(">\n");
            }
            // Record info END
            sb.append("  </rdf:Description>\n");

            // Footer info
            sb.append("</rdf:RDF>");
            // Build RDF END

            // Write RDF to RecordHandler
            //log.trace("Adding record: " + fixedkey + "_" + recID);
            //log.trace("data: "+ sb.toString());
            //log.info("rhOutput: "+ this.rhOutput);
            //log.info("recID: "+recID);
            this.rhOutput.addRecord(name + "_" + recID, sb.toString(), this.getClass());
            count++;*/
                 
         
     
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            throw new IOException(e);
        }
    }





    @Override
    public void writeRecord(String id, String data) throws IOException {
        log.trace("Adding record "+id);
        this.rhOutput.addRecord(id, data, getClass());
    }

    protected  void logMapObject(Object obj) {
        HashMap<? , ?> mapobject = (HashMap) obj;
        Iterator<?> iter = mapobject.keySet().iterator();
        while (iter.hasNext()) {
           Object keyobj = iter.next();
           Object valobj = mapobject.get(keyobj);
           log.info(keyobj +": "+ valobj);
        }
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
            new SparqlFetch(args).execute();
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
                log.error(error.getMessage());
                System.exit(1);
            } else {
                System.exit(0);
            }
        }
    }
}
