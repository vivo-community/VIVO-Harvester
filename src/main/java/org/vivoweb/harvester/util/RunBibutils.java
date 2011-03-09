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

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;

/**
 * @author Michael Barbieri (mbarbier@ufl.edu)
 * Executes the command-line tool bibutils on all files in the specified folder, converting from a type specified by the name of the subfolder.  For example, files
 * in &lt;inputPath&gt;/bib will be converted from the bib format to the MODS format.
 */
public class RunBibutils {
	/**
	 * The folder for the input files.  This should contain subfolders named for the format to convert from: bib, biblatex, copac, end, endx, isi, med, ris 
	 */
	private final String inputPath;
	/**
	 * The folder for the converted MODS XML files.  This will be flattened (contain no subfolders), with each file prefixed by its former type to avoid name collisions.
	 */
	private final String outputPath;
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SanitizeMODSXML.class);
	
	/**
	 * Constructor
	 * @param inputPath the folder for the input files.
	 * @param outputPath the folder for the converted MODS XML files
	 */
	public RunBibutils(String inputPath, String outputPath) {
		this.inputPath = stripFinalSlash(inputPath);
		this.outputPath = stripFinalSlash(outputPath);
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
	 */
	public RunBibutils(ArgList argList) {
		this(argList.get("inputPath"), argList.get("outputPath"));
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SanitizeMODSXML");
		parser.addArgument(new ArgDef().setShortOption('i').setLongOpt("inputPath").withParameter(true, "INPUT_PATH").setDescription("Path to folder containing input files").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('o').setLongOpt("outputPath").withParameter(true, "OUTPUT_PATH").setDescription("Path of folder to which to write output files").setRequired(true));
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
	 * Checks to make sure the input path and the output path are both directories. It not, log errors and explode.
	 * @throws RuntimeException if either input path or output path is not a directory
	 */
	private void checkValidInputs() {
		File inputDir = new File(this.inputPath);
		File outputDir = new File(this.outputPath);
		
		String errorMessage = "";
		if(!inputDir.isDirectory()) {
			String oneLineError = "Not a directory: " + this.inputPath;
			log.error(oneLineError);
			errorMessage += oneLineError + "\n";
		}
		if(outputDir.exists() && (!outputDir.isDirectory())) {
			String oneLineError = "Not a directory: " + this.outputPath;
			log.error(oneLineError);
			errorMessage += oneLineError + "\n";
		}
		
		if(!errorMessage.equals("")) {
			errorMessage = errorMessage.substring(0, errorMessage.length() - 1); //strip last newline
			throw new RuntimeException(errorMessage); //explode
		}
	}
	


	/**
	 * Convert all files in inputPath directory to MODS XML according to type specified by subdirectory
	 * @throws IOException if an error in reading or writing occurs
	 */
	public void execute() throws IOException {
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
