/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.test.harvester.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.score.Score;
import org.vivoweb.harvester.util.InitLog;
import org.vivoweb.harvester.util.repo.JenaConnect;

/**
 * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class ScoreArgsTest extends TestCase {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(ScoreArgsTest.class);
	/**
	 * vivo test configuration file
	 */
	private File vivoXML;
	/** */
	private String overrideVArg;
	/** */
	private String overrideOArg;
	/** */
	/** */
	private String overrideIArg;
	/** */
	private HashMap<String, String> overrideIArgMap;
	/** */
	private HashMap<String, String> overrideOArgProp;
	/** */
	private HashMap<String, String> overrideVArgProp;
	/** */
	private String oArg;
	/** */
	private String vArg;
	/** */
	private String iArg;
	/** */
	private JenaConnect input;
	/** */
	private JenaConnect vivo;
	/** */
	private JenaConnect output;
	
	/**
	 * Test match algorithm with rename flag
	 * @throws IOException error
	 */
	public void testMatchWithRename() throws IOException {
		log.info("BEGIN testMatchWithRename");
		log.debug("Test -i iArg -v vArg -o oArg -m 'vivoworkEmail=scoreworkemail' -r");
		String[] args = new String[]{"-i", this.iArg, "-v", this.vArg, "-o", this.oArg, "-m", "http://vivoweb.org/ontology/core#workEmail=http://vivoweb.org/ontology/score#workEmail", "-r"};
		new Score(args);
		log.info("END testMatchWithRename");
	}
	
	/**
	 * Test match algorithm with rename and inplace flag
	 * @throws IOException error
	 */
	public void testInplaceMatchWithRename() throws IOException {
		log.info("BEGIN testInplaceMatchWithRename");
		log.debug("Test -i iArg -v vArg -m 'vivoworkEmail=scoreworkemail' -r -a");
		String[] args = new String[]{"-i", this.iArg, "-v", this.vArg, "-m", "http://vivoweb.org/ontology/core#workEmail=http://vivoweb.org/ontology/score#workEmail", "-r", "-a"};
		new Score(args);
		log.info("END testInplaceMatchWithRename");
	}
	
	/**
	 * Test match algorithm with linking
	 * @throws IOException error
	 */
	public void testMatchWithLink() throws IOException {
		log.info("BEGIN testMatchWithLink");
		log.debug("Test -i iArg -v vArg  -o oArg -m 'vivoworkEmail=scoreworkemail' -l 'vivoObjProp=scoreObjProp' -a");
		String[] args = new String[]{"-i", this.iArg, "-v", this.vArg, "-o", this.oArg, "-m", "http://vivoweb.org/ontology/core#workEmail=http://vivoweb.org/ontology/score#workEmail", "-l", "http://vivoweb.org/ontology/core#linkMe=http://vivoweb.org/ontology/core#meLink"};
		new Score(args);
		log.info("END testMatchWithLink");
	}
	
	/**
	 * Test to make sure Score blows up when illegal parameter Q is passed
	 */
	public void testIllegalQParam() {
		log.info("BEGIN testIllegalQParam");
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg -Q");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg, "-Q"};
		try {
			new Score(args);
			log.error("Invalid Q param not rejected -- score object invalid");
			fail("Invalid Q param not rejected -- score object invalid");
		} catch(Exception e) {
			// this is expected
			log.debug("Caught Expected Exception: "+e.getMessage());
		}
		log.info("END testIllegalQParam");
	}
	
	/**
	 * Test to make sure model not wiped when -w flag is omitted
	 * @throws IOException error
	 */
	public void testKeepWorkingModel() throws IOException {
		log.info("BEGIN testKeepWorkingModel");
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg};
		new Score(args).execute();
		if(this.input.getJenaModel().isEmpty()) {
			log.error("Model emptied despite -w arg missing");
			fail("Model emptied despite -w arg missing");
		}
		log.info("END testKeepWorkingModel");
	}
	
	/**
	 * Test to ensure -w flag causes working model to be empty
	 * @throws IOException error
	 */
	public void testWipeWorkingModel() throws IOException {
		log.info("BEGIN testWipeWorkingModel");
		log.debug("Testing don't keep working model");
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArgl -w");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg, "-w"};
		new Score(args).execute();
		if(!this.input.getJenaModel().isEmpty()) {
			log.error("Model not empty -w arg violated");
			fail("Model not empty -w arg violated");
		}
		log.info("END testWipeWorkingModel");
	}
	
	/**
	 * Test to ensure that the output model gets wiped before running when -q flag set
	 * @throws IOException error
	 */
	public void testEmptyOutputModel() throws IOException {
		log.info("BEGIN testEmptyOutputModel");
		log.debug("Testing empty output model");
		// empty output model
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg -q");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg,"-q"};
		//get size
		long modelSize = this.output.getJenaModel().size();
		new Score(args).execute();
		if (modelSize > this.output.getJenaModel().size()) {
			log.error("Output model not emptied before run");
			fail("Output model not emptied before run");
		}
		log.info("END testEmptyOutputModel");
	}
	
	/**
	 * Test to ensure that output model is not empties when -q flag omitted
	 * @throws IOException error
	 */
	public void testNoEmptyOutputModel() throws IOException {
		log.info("BEGIN testNoEmptyOutputModel");
		// don't empty output model
		log.debug("Test -i iArg -I IArg -v vArg -V VArg -o oArg -O OArg");
		String[] args = new String[]{"-i", this.iArg, "-I", this.overrideIArg, "-v", this.vArg, "-V", this.overrideVArg, "-o", this.oArg, "-O", this.overrideOArg};
		//get size
		long modelSize = this.output.getJenaModel().size();
		new Score(args).execute();
		if (modelSize < this.output.getJenaModel().size()) {
			log.error("Output model emptied before run");
			fail("Output model emptied before run");
		}
		log.info("END testNoEmptyOutputModel");
	}
	
	@Override
	protected void setUp() {
		InitLog.initLogger(ScoreArgsTest.class);
		// create VIVO.xml
		try {
			this.vivoXML = File.createTempFile("scoretest_vivo", ".xml");
			BufferedWriter out = new BufferedWriter(new FileWriter(this.vivoXML));
			out.write(""+
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Model>" +
					"<Param name=\"type\">sdb</Param>" +
					"<Param name=\"dbLayout\">layout2</Param>" +
					"<Param name=\"dbClass\">org.h2.Driver</Param>" +
					"<Param name=\"dbType\">H2</Param>" +
					"<Param name=\"dbUrl\">jdbc:h2:mem:test</Param>" +
					"<Param name=\"modelName\">testVivoModel</Param>" +
					"<Param name=\"dbUser\">sa</Param>" +
					"<Param name=\"dbPass\"></Param>" +
				"</Model>"
			);
			out.close();
		} catch(IOException e) {
			throw new IllegalArgumentException(e.getMessage(),e);
		}
		
		// inputs
		this.iArg = this.vivoXML.getAbsolutePath();
		this.vArg = this.vivoXML.getAbsolutePath();
		
		// outputs
		this.oArg = this.vivoXML.getAbsolutePath();
		
		// model overrides
		this.overrideIArg = "modelName=testInputModel";
		this.overrideIArgMap = new HashMap<String, String>();
		this.overrideIArgMap.put("modelName", "testInputModel");
		
		this.overrideOArg = "modelName=testOutputModel";
		this.overrideOArgProp = new HashMap<String, String>();
		this.overrideOArgProp.put("modelName", "testOutputModel");
		
		this.overrideVArg = "modelName=testVivoModel";
		this.overrideVArgProp = new HashMap<String, String>();
		this.overrideVArgProp.put("modelName", "testVivoModel");
		
		try {
			// load input models
			this.input = JenaConnect.parseConfig(this.iArg, this.overrideIArgMap);
			// Load up input data before starting
			this.input.loadRdfFromString(ScoreTest.scoreInput, null, null);
			
			this.vivo = JenaConnect.parseConfig(this.vArg, this.overrideVArgProp);
			// Load up vivo data before starting
			this.vivo.loadRdfFromString(ScoreTest.vivoRDF, null, null);
			
			this.output = JenaConnect.parseConfig(this.oArg, this.overrideOArgProp);
		} catch(IOException e) {
			throw new IllegalArgumentException(e.getMessage(),e);
		}
	}
	
	@Override
	protected void tearDown() {
		if(this.input != null) {
			try {
				this.input.truncate();
			} catch(Exception e) {
				//Ignore
			} finally {
				try {
					this.input.close();
				} catch(Exception e) {
					//Ignore
				}
			}
			this.input = null;
		}
		if(this.vivo != null) {
			try {
				this.vivo.truncate();
			} catch(Exception e) {
				//Ignore
			} finally {
				try {
					this.vivo.close();
				} catch(Exception e) {
					//Ignore
				}
			}
			this.vivo = null;
		}
		if(this.output != null) {
			try {
				this.output.truncate();
			} catch(Exception e) {
				//Ignore
			} finally {
				try {
					this.output.close();
				} catch(Exception e) {
					//Ignore
				}
			}
			this.output = null;
		}
		if(this.vivoXML != null) {
			try {
				if(this.vivoXML.exists()) {
					if(!this.vivoXML.delete()) {
						log.error("File \""+this.vivoXML.getAbsolutePath()+"\" not deleted!");
					}
				}
			} catch(Exception e) {
				//Ignore
			}
			this.vivoXML = null;
		}
	}
}
