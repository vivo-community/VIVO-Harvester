/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.translate;

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

/**
 * @author Michael Barbieri (mbarbier@ufl.edu)
 * Executes the command-line tool bibutils on all files in the specified folder, converting from a type specified in command line.
 */
public class RunBibutils extends Translator {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(RunBibutils.class);
	/**
	 * The folder for the bibutils executables. 
	 */
	private final String bibutilsBasePath;
	/**
	 * The format of the input files.  This determines which executable is used.  Acceptable values are bib, biblatex, copac, end, endx, isi, med, ris.
	 */
	private final String inputFormat;

	/**
	 * Constructor
	 * @param bibutilsBasePath the folder for the bibutils executables
	 * @param inputFormat the format to convert from
	 */
	public RunBibutils(String bibutilsBasePath, String inputFormat) {
		this.bibutilsBasePath = stripFinalSlash(bibutilsBasePath);
		this.inputFormat = inputFormat;
		
		// Check to make sure the recordhandlers exist, bibutils base path is a directory, and the input format is one of the acceptable values
		String errorMessage = "";
		
		File bibutilsBaseDir = new File(this.bibutilsBasePath);
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
			throw new IllegalArgumentException(errorMessage); //explode
		}
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
	
	@Override
	public String translate(String input) {
		try {
			File tempInputFile = File.createTempFile("runBibutilsTempFile", null);
			FileOutputStream fos = new FileOutputStream(tempInputFile);
			fos.write(input.getBytes());
			fos.close();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			String command = this.bibutilsBasePath + "/" + this.inputFormat + "2xml " + tempInputFile.getAbsolutePath();
			
			runBibutilsCommand(command, baos);
			
			tempInputFile.delete();
			
			return baos.toString();
		} catch(IOException e) {
			throw new Error(e);
		}
	}
	
	/**
	 * Runs the specified command line.
	 * @param command the command to execute
	 * @param outStream the stream to which data output by bibutils will be written
	 * @throws IOException if an error occurred
	 */
	private void runBibutilsCommand(String command, OutputStream outStream) throws IOException {
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
		} catch(InterruptedException e) {
			throw new IOException(e.getMessage(), e);
		}
		if(exitVal != 0) {
			log.error("Bibutils exited with error code " + exitVal);
		} else {
			log.info("Bibutils completed execution");
		}
	}

	@Override
	protected Logger getLog() {
		return log;
	}
}
