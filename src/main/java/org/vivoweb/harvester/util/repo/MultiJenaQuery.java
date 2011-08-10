/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.repo;

import java.io.IOException;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;

/**
 * Execute a query over multiple Jena models
 * 
 * todo: this is hacky.  It only allows two Jena models at the moment, as that's what our needs are.  It can be
 *       generalized if we can figure out how to handle multiple configs *and* their overrides in the ArgParser.
 * 
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class MultiJenaQuery {
	
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(MultiJenaQuery.class);

	/**
	 * First model we are connecting to
	 */
	private JenaConnect jena1;
	
	/**
	 * Second model we are connecting to
	 */
	private JenaConnect jena2;
	
	/**
	 * Model in which to store temp copy of inputs
	 */
	private JenaConnect tempJena;
	
	/**
	 * Query to execute against 
	 */
	private String query;
	
	/**
	 * Format to output results in ('RS_RDF', etc for select queries / 'RDF/XML', etc for construct/describe queries)
	 */
	private String queryResultFormat;
	
	/**
	 * Path of file to output results to
	 */
	private String outputFile;

	/**
	 * Constructor
	 * @param args argument list
	 * @throws IOException error parsing options
	 * @throws UsageException user requested usage message
	 */
	private MultiJenaQuery(String... args) throws IOException, UsageException {
		this(getParser().parse(args));
	}

	
	/**
	 * Constructor
	 * @param opts parsed argument list
	 * @throws IOException error parsing options
	 */
	private MultiJenaQuery(ArgList opts) throws IOException {
		this(
			JenaConnect.parseConfig(opts.get("j"), opts.getValueMap("J")), 
			JenaConnect.parseConfig(opts.get("k"), opts.getValueMap("K")),
			opts.get("q"),
			opts.get("Q"),
			opts.get("f"),
			opts.get("t")
		);
	}
	
	/**
	 * Constructor
	 * @param jena1 first JenaConnect
	 * @param jena2 second JenaConnect
	 * @param query query to execute against the two
	 * @param queryResultFormat format to output results in
	 * @param tempDir location to store temp copy model
	 * @param outputFile the file to output results to
	 */
	private MultiJenaQuery(JenaConnect jena1, JenaConnect jena2, String query, String queryResultFormat, String outputFile, String tempDir) {
		this.jena1 = jena1;
		this.jena2 = jena2;
		this.query = query;
		this.queryResultFormat = queryResultFormat;
		this.outputFile = outputFile;
		
		if(tempDir == null) {
			log.trace("temp model directory is not specified, using system temp directory");
			this.tempJena = new MemJenaConnect();
		} else {
			this.tempJena = new TDBJenaConnect(tempDir);
		}
	}
	
	
	
	
	/**
	 * Execute query over Jena models
	 * @throws IOException error connecting to the models or outputting data
	 */
	public void execute() throws IOException {
		
		log.debug("jena1 empty: " + this.jena1.isEmpty());
		log.debug("jena2 empty: " + this.jena2.isEmpty());
		
		runQuery();
	}
	
	
	/**
	 * Run the query and output result
	 * @throws IOException error connecting to the models or outputting data
	 */
	private void runQuery() throws IOException {

		OutputStream out;
		if(this.outputFile != null) {
			out = FileAide.getOutputStream(this.outputFile);
			log.info("Outputting to " + this.outputFile);
		} else {
			out = System.out;
		}

		Dataset ds = prepDataset();

		log.debug("Preparing to execute query:\n" + this.query);
		Query queryObject = QueryFactory.create(this.query, Syntax.syntaxARQ);
		QueryExecution queryExec = QueryExecutionFactory.create(queryObject, ds);

		if(queryObject.isSelectType()) {
			ResultSetFormat rsf = JenaConnect.formatSymbols.get(this.queryResultFormat);
			if(rsf == null) {
				rsf = ResultSetFormat.syntaxText;
			}
			ResultSetFormatter.output(out, queryExec.execSelect(), rsf);
		} else if(queryObject.isAskType()) {
			out.write((Boolean.toString(queryExec.execAsk())+"\n").getBytes());
		} else {
			MemJenaConnect resultModel = new MemJenaConnect();
			if(queryObject.isConstructType()) {
				queryExec.execConstruct(resultModel.getJenaModel());
			} else if(queryObject.isDescribeType()) {
				queryExec.execDescribe(resultModel.getJenaModel());
			} else {
				throw new IllegalArgumentException("Query Invalid: Not Select, Construct, Ask, or Describe");
			}
			
			resultModel.exportRdfToStream(out, this.queryResultFormat);
		}
	}

	
	/**
	 * Build the clones and get the dataset
	 * @return the dataset
	 * @throws IOException error connecting to the models
	 */
	private Dataset prepDataset() throws IOException {
		log.trace("testing jena1");
//		if(!this.jena1.executeAskQuery("ASK { ?s ?p ?o }", true)) {
		if(!this.jena1.executeAskQuery("ASK { ?s ?p ?o }", false)) {
			log.trace("Empty Dataset");
		}
		log.trace("testing jena2");
//		if(!this.jena2.executeAskQuery("ASK { ?s ?p ?o }", true)) {
		if(!this.jena2.executeAskQuery("ASK { ?s ?p ?o }", false)) {
			log.trace("Empty Dataset");
		}

		log.info(this.jena1.getModelName());
		log.info(this.jena2.getModelName());
		
		// Bring all models into a single Dataset
		JenaConnect jena1Clone = this.tempJena.neighborConnectClone(this.jena1.getModelName());
		//jena1Clone.truncate();
		jena1Clone.loadRdfFromJC(this.jena1);
		
		JenaConnect jena2Clone = this.tempJena.neighborConnectClone(this.jena2.getModelName());
		//jena2Clone.truncate();
		jena2Clone.loadRdfFromJC(this.jena2);

		Dataset ds = this.tempJena.getDataset();
		log.trace("testing Dataset");
//		if(!this.tempJena.executeAskQuery("ASK { ?s ?p ?o }", false)) {
//		if(!this.tempJena.executeAskQuery("ASK { ?s ?p ?o }", true)) {
		if(!this.tempJena.executeAskQuery("PREFIX arq: <urn:x-arq:> ASK FROM NAMED <urn:x-arq:UnionGraph> { GRAPH arq:UnionGraph { ?s ?p ?o } }", true)) {

			log.trace("Empty Dataset");
		}
		return ds;
	}

	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("MultiJenaQuery");
		parser.addArgument(new ArgDef().setShortOption('j').setLongOpt("jena1").withParameter(true, "CONFIG_FILE").setDescription("config file for first jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('J').setLongOpt("jenaOverride1").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of first jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('k').setLongOpt("jena2").withParameter(true, "CONFIG_FILE").setDescription("config file for second jena model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('K').setLongOpt("jenaOverride2").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of second jena model config using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('q').setLongOpt("query").withParameter(true, "SPARQL_QUERY").setDescription("sparql query to execute").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('Q').setLongOpt("queryResultFormat").withParameter(true, "RESULT_FORMAT").setDescription("the format to return the results in ('RS_RDF',etc for select queries / 'RDF/XML',etc for construct/describe queries)").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("fileOutput").withParameter(true, "OUTPUT_FILE").setDescription("the file to output the results in, if not specified writes to stdout").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('t').setLongOpt("tempJenaDir").withParameter(true, "DIRECTORY_PATH").setDescription("directory to store temp jena model").setRequired(false));
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser(), "ft");
			log.info(getParser().getAppName() + ": Start");
			new MultiJenaQuery(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.err.println(getParser().getUsage());
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
