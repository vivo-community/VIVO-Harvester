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
import org.apache.commons.vfs.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.CSVtoJDBC;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.RecordHandler;
import org.vivoweb.harvester.fetch.JDBCFetch;

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
	 * @param opts
	 * @throws IOException
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public CSVtoRDF(ArgList opts) throws IOException, SQLException, ClassNotFoundException{
		this(opts.get("i"),RecordHandler.parseConfig(opts.get("o"), opts.getValueMap("O")),opts.get("n"));
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public CSVtoRDF(String... args) throws IOException, SQLException, ClassNotFoundException {
		this(new ArgList(getParser(), args));
	}

	/**
	 * @param CSVfilename
	 * @param output
	 * @param uriNameSpace
	 * @throws SQLException 
	 * @throws FileSystemException 
	 * @throws ClassNotFoundException 
	 */
	public CSVtoRDF(String CSVfilename, RecordHandler output, String uriNameSpace) throws SQLException, FileSystemException, ClassNotFoundException{
		this.tablename = "csvtordf";
		Class.forName("org.h2.Driver");
		this.conn = DriverManager.getConnection("jdbc:h2:mem:TempCSVtoRDF", "sa", "");
		this.conn.setAutoCommit(false);
		this.toDatabase = new CSVtoJDBC(CSVfilename, this.conn, "csvtordf");
		this.namespace = uriNameSpace;
		this.outputRH = output;
	}

	/**
	 * @param CSVfilestream 
	 * @param output
	 * @param uriNameSpace
	 * @throws SQLException 
	 */
	public CSVtoRDF(InputStream CSVfilestream, RecordHandler output, String uriNameSpace) throws SQLException{
		this.tablename = "csvtordf";
		this.conn = DriverManager.getConnection("jdbc:h2:mem:TempCSVtoRDF", "sa", "");
		this.conn.setAutoCommit(false);
		this.toDatabase = new CSVtoJDBC(CSVfilestream, this.conn, this.tablename);
		this.namespace = uriNameSpace;
		this.outputRH = output;
	}
	
	/**
	 * @throws IOException 
	 * @throws SQLException 
	 * 
	 */
	public void execute() throws IOException, SQLException {
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
			
			this.fromDatabase = new JDBCFetch(this.conn,this.outputRH,this.namespace,null, null, null, null, dataFields, idFields,null,null, null);
			this.fromDatabase.execute();
	}
	
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("CSVtoRDF");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputFile").withParameter(true, "FILENAME").setDescription("csv file to be read into the Recordhandler").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("RecordHandler config file path").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('n').setLongOpt("namespace").withParameter(true, "NAMESPACE").setDescription("").setRequired(false));
		return parser;
	}
	
	/**
	 * @param args
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
