/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.repo.Record;
import org.vivoweb.harvester.util.repo.RecordHandler;

/**
 * @author Michael Barbieri (mbarbier@ufl.edu)
 * Executes the command-line tool bibutils on all files in the specified folder, converting from a type specified in command line.
 */
public class RunBibutils {
	/**
	 * The folder for the bibutils executables. 
	 */
	private final String bibutilsBasePath;
	/**
	 * The format of the input files.  This determines which executable is used.  Acceptable values are bib, biblatex, copac, end, endx, isi, med, ris.
	 */
	private final String inputFormat;
	/**
	 * The record handler for the input files. 
	 */
	private final RecordHandler inStore;
	/**
	 * The record handler for the converted MODS XML files.
	 */
	private final RecordHandler outStore;
	/**
	 * Force all input files to be processed even if nothing has changed.
	 */
	private final boolean force;
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RunBibutils.class);

	/**
	 * Constructor
	 * @param bibutilsBasePath the folder for the bibutils executables
	 * @param inputFormat the format to convert from
	 * @param inStore the record handler for the input files.
	 * @param outStore the record handler for the converted MODS XML files
	 * @param force process all records even if they do not need to be processed
	 */
	public RunBibutils(String bibutilsBasePath, String inputFormat, RecordHandler inStore, RecordHandler outStore, boolean force) {
		this.bibutilsBasePath = stripFinalSlash(bibutilsBasePath);
		this.inputFormat = inputFormat;
		this.inStore = inStore;
		this.outStore = outStore;
		this.force = force;
		checkValidInputs();
	}

	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public RunBibutils(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}

	/**
	 * Constructor
	 * @param argList option set of parsed args
	 * @throws IOException if an error occurred setting up a record handler
	 */
	public RunBibutils(ArgList argList) throws IOException {
		this(argList.get("b"), argList.get("m"), RecordHandler.parseConfig(argList.get("i"), argList.getValueMap("I")), RecordHandler.parseConfig(argList.get("o"), argList.getValueMap("O")), argList.has("f"));
	}

	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("RunBibutils");
		parser.addArgument(new ArgDef().setShortOption('b').setLongOpt("bibutilsBasePath").withParameter(true, "BIBUTILS_BASE_PATH").setDescription("Path to folder containing bibutils executables").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("inputFormat").withParameter(true, "INPUT_FORMAT").setDescription("Format of the input files.  Acceptable values are bib, biblatex, copac, end, endx, isi, med, ris.").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("input").withParameter(true, "CONFIG_FILE").setDescription("Record handler for input files").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('I').setLongOpt("inputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of input recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("output").withParameter(true, "CONFIG_FILE").setDescription("Record handler for output files").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('O').setLongOpt("outputOverride").withParameterValueMap("RH_PARAM", "VALUE").setDescription("override the RH_PARAM of output recordhandler using VALUE").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('f').setLongOpt("force").setDescription("force translation of all input records, even if previously processed").setRequired(false));
		return parser;
	}

	/**
	 * Checks to see if the given path has a slash at the end, and if so, removes it
	 * @param path the path to check
	 * @return the path with the slash removed
	 */
	private String stripFinalSlash(String path) {
		String returnValue = path;
		if(returnValue.endsWith("/")) {
			returnValue = returnValue.substring(0, returnValue.length() - 1);
		}
		
		return returnValue;
	}

	/**
	 * Checks to make sure the bibutils base path is a directory and the input format is one of the acceptable values.  If
	 * not, log errors and explode.
	 * @throws RuntimeException if either input path or output path is not a directory
	 */
	private void checkValidInputs() {
		File bibutilsBaseDir = new File(this.bibutilsBasePath);

		String errorMessage = "";
		if(!bibutilsBaseDir.isDirectory()) {
			String oneLineError = "Not a directory: " + this.bibutilsBasePath;
			log.error(oneLineError);
			errorMessage += oneLineError + "\n";
		}

		if(!Arrays.asList("bib", "biblatex", "copac", "end", "endx", "isi", "med", "ris").contains(this.inputFormat)) {
			String oneLineError = "Not a valid input format: " + this.inputFormat;
			log.error(oneLineError);
			errorMessage += oneLineError + "\n";
		}

		if(!errorMessage.equals("")) {
			errorMessage = errorMessage.substring(0, errorMessage.length() - 1); //strip last newline
			throw new RuntimeException(errorMessage); //explode
		}
	}



	/**
	 * Convert all files in inputPath directory to MODS XML according to specified type
	 * @throws IOException if an error in reading or writing occurs
	 */
	public void execute() throws IOException {
		File tempInputFile = File.createTempFile("vivo", ".bib");
		tempInputFile.deleteOnExit();

		int translated = 0;
		int skipped = 0;

		for(Record r : this.inStore) {
			if(this.force || r.needsProcessed(this.getClass())) {
				log.trace("Running bibutils on record " + r.getID());
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				translateRecordWithBibutils(r, baos, tempInputFile);
				this.outStore.addRecord(r.getID(), baos.toString(), this.getClass());
				r.setProcessed(this.getClass());
				baos.close();
				translated++;
			} else {
				log.trace("No bibutils run needed: " + r.getID());
				skipped++;
			}
		}
		log.info(String.valueOf(translated) + " records translated into MODS by Bibutils.");
		log.info(String.valueOf(skipped) + " records did not need translation into MODS by Bibutils.");
	}


	/**
	 * Translates a single record by using bibutils on it.  Bibutils works only on files, but we already have streams from the record handler, so
	 * we write the streams out to temp files before calling bibutils on them.
	 * @param inputRecord the record pertaining to the input file
	 * @param outStream the stream to which data output by bibutils will be written
	 * @param tempInputFile the file to which the input stream's data will be written
	 * @throws IOException if an I/O problem occurred in any step of this process
	 */
	private void translateRecordWithBibutils(Record inputRecord, OutputStream outStream, File tempInputFile) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(tempInputFile);
		fos.write(inputRecord.getData().getBytes());
		fos.close();

		String command = this.bibutilsBasePath + "/" + this.inputFormat + "2xml " + tempInputFile.getAbsolutePath();
		
		runBibutilsCommand(command, outStream);
	}


	/**
	 * Runs the specified command line.
	 * @param command the command to execute
	 * @param outStream the stream to which data output by bibutils will be written
	 * @throws IOException if an error occurred
	 */
	private void runBibutilsCommand(String command, OutputStream outStream) throws IOException
	{
		log.info("running command: " + command);
		Process pr = Runtime.getRuntime().exec(command);

		BufferedReader processOutputReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		boolean haveWrittenOpeningAngleBracket = false; //used to ignore bogusness
		for(int currentByte = processOutputReader.read(); currentByte != -1; currentByte = processOutputReader.read()) {
			
			if(haveWrittenOpeningAngleBracket) {
				outStream.write(currentByte);
			}
			else if(((char)currentByte) == '<') {
				outStream.write(currentByte);
				haveWrittenOpeningAngleBracket = true;
			}
		}

		BufferedReader processErrorReader = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		for(String line = processErrorReader.readLine(); line != null; line = processErrorReader.readLine()) {
			log.debug("Bibutils output: " + line);
		}

		int exitVal;

		try {
			exitVal = pr.waitFor();
		}
		catch(InterruptedException e) {
			throw new IOException(e.getMessage(), e);
		}
		log.info("Bibutils exited with error code " + exitVal);
	}




	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new RunBibutils(new ArgList(getParser(), args)).execute();
		} catch(IllegalArgumentException e) {
			log.debug(e.getMessage(), e);
			System.out.println(getParser().getUsage());
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			log.info(getParser().getAppName() + ": End");
		}
	}
}
