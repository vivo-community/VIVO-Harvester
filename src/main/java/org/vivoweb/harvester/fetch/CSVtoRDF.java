package org.vivoweb.harvester.fetch;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.CSVtoJDBC;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * @author jrpence
 *
 */
public class CSVtoRDF {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(JDBCFetch.class);
	/** */
	private CSVtoJDBC toDatabase;
	/** */
	private JDBCFetch fromDatabase;
	/** */
	private RecordHandler outputRH;
	/** */
	private Connection conn;
	/** */
	private String namespace;
	/** */
	private String tablename;
	
	/**
	 * @param opts ArgList of command line arguments
	 * @throws IOException exception thrown if there is a problem with parsing the configs
	 * @throws SQLException if there is a problem during the database usage
	 * @throws ClassNotFoundException lets the program know if there is a desired class missing
	 */
	public CSVtoRDF(ArgList opts) throws IOException, SQLException, ClassNotFoundException{
		this(opts.get("i"),RecordHandler.parseConfig(opts.get("o"), opts.getValueMap("O")),opts.get("n"));
	}

	/**
	 * @param args array of command line arguments
	 * @throws IOException exception thrown if there is a problem with parsing the configs
	 * @throws SQLException if there is a problem during the database usage
	 * @throws ClassNotFoundException lets the program know if there is a desired class missing
	 */
	public CSVtoRDF(String... args) throws IOException, SQLException, ClassNotFoundException {
		this(new ArgList(getParser(), args));
	}

	/**
	 * @param CSVfilename Path and filename of the CSVfile
	 * @param output destination recordHandler
	 * @param uriNameSpace Name space to be used for the rdf elements 
	 * @throws SQLException if there is a problem during the database usage
	 * @throws IOException Exception for file access problems
	 * @throws ClassNotFoundException lets the program know if there is a desired class missing
	 */
	public CSVtoRDF(String CSVfilename, RecordHandler output, String uriNameSpace) throws SQLException, IOException, ClassNotFoundException{
		this.tablename = "csv";
		Class.forName("org.h2.Driver");
		this.conn = DriverManager.getConnection("jdbc:h2:mem:TempCSVtoRDF", "sa", "");
		this.conn.setAutoCommit(false);
		this.toDatabase = new CSVtoJDBC(CSVfilename, this.conn, "csv");
		this.namespace = uriNameSpace;
		this.outputRH = output;
	}

	/**
	 * @param CSVfilestream An input stream of CSV data
	 * @param output destination recordHandler
	 * @param uriNameSpace Name space to be used for the rdf elements
	 * @throws SQLException if there is a problem during the database usage
	 */
	public CSVtoRDF(InputStream CSVfilestream, RecordHandler output, String uriNameSpace) throws SQLException{
		this.tablename = "csv";
		this.conn = DriverManager.getConnection("jdbc:h2:mem:TempCSVtoRDF", "sa", "");
		this.conn.setAutoCommit(false);
		this.toDatabase = new CSVtoJDBC(CSVfilestream, this.conn, this.tablename);
		this.namespace = uriNameSpace;
		this.outputRH = output;
	}
	
	/**
	 * @throws IOException If there is an I/O problem during either execute
	 * 
	 */
	public void execute() throws IOException {
			this.toDatabase.execute();

			Set<String> tblnm = new TreeSet<String>();
			tblnm.add(this.tablename);
			Map<String, List<String>> idFields = new HashMap<String, List<String>>();
			Map<String, List<String>> dataFields = new HashMap<String, List<String>>();
			List<String> idfield = new ArrayList<String>();
			idfield.add("ROWID");
			idFields.put(this.tablename,idfield);
			List<String> dFields = this.toDatabase.getFields();
			dataFields.put(this.tablename,dFields);
			
			this.fromDatabase = new JDBCFetch(this.conn,this.outputRH,this.namespace,null, null, tblnm, null, dataFields, idFields,null,null, null);
			this.fromDatabase.execute();
	}
	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("CSVtoRDF");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "FILENAME").setDescription("csv file to be read into the Recordhandler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "NAMESPACE").setDescription("").setRequired(false));
		return parser;
	}
	
	/**
	 * @param args array of command line arguments
	 */
	public static void main(String[] args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new CSVtoRDF(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage(), e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
}
