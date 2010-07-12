package org.vivoweb.ingest.transfer;

import java.io.IOException;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.ingest.util.JenaConnect;
import org.vivoweb.ingest.util.Task;
import org.xml.sax.SAXException;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Transfer data from one Jena model to another
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class Transfer extends Task {
	
	/**
	 * Log4J Logger
	 */
	private static Log log = LogFactory.getLog(Transfer.class);
	/**
	 * Model to read records from
	 */
	private Model input;
	/**
	 * Model to write records to
	 */
	private Model output;
	
	/**
	 * Constructor
	 * @param in input Model
	 * @param out output Model
	 */
	public Transfer(Model in, Model out) {
	  this.input = in;
	  this.output = out;
	}
	
	/**
	 * Copy data from input to output
	 */
	private void transfer() {
		this.output.add(this.input);
	}
	
	/**
	 * Main Method
	 * @param args command line arguments
	 */
	public static void main(String... args) {
		if(args.length != 2) {
			IllegalArgumentException e = new IllegalArgumentException("Transfer requires 2 arguments, both being Jena Model Configuration Files");
			log.error(e.getMessage(),e);
			throw e;
		}
		try {
			JenaConnect in = JenaConnect.parseConfig(args[1]);
			JenaConnect out = JenaConnect.parseConfig(args[2]);
			new Transfer(in.getJenaModel(), out.getJenaModel()).transfer();
		} catch(Exception e) {
			log.error(e.getMessage(),e);
		}
	}
	
	public static Transfer getInstance(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
		String inConfig = getParam(params, "inputModel", true);
		String outConfig = getParam(params, "outputModel", true);
		JenaConnect in = JenaConnect.parseConfig(inConfig);
		JenaConnect out = JenaConnect.parseConfig(outConfig);
		return new Transfer(in.getJenaModel(), out.getJenaModel());
	}
	
	@Override
	public void executeTask() throws NumberFormatException {
		transfer();
	}
}
