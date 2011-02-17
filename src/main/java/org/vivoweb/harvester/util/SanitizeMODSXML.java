/*******************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, Michael Barbieri. All
 * rights reserved. This program and the accompanying materials are made available under the terms of the new BSD
 * license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors: Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, Michael Barbieri - initial
 * API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Hashtable;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;

/**
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class SanitizeMODSXML {
	/**
	 * The XML file to sanitize.
	 */
	private final String inputPath;
	/**
	 * The sanitized XML file.
	 */
	private final String outputPath;
	/**
	 * The mapping of XML-illegal characters to their sanitized-string versions.
	 */
	private final Map<String, String> replacementMapping = generateReplacementMapping();
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SanitizeMODSXML.class);
	
	/**
	 * Constructor
	 * @param inputPath the XML file to sanitize
	 * @param outputPath the sanitized XML file
	 */
	public SanitizeMODSXML(String inputPath, String outputPath) {
		this.inputPath = stripFinalSlash(inputPath);
		this.outputPath = stripFinalSlash(outputPath);
		checkValidInputs();
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 */
	public SanitizeMODSXML(String[] args) throws IOException {
		this(new ArgList(getParser(), args));
	}
	
	/**
	 * Constructor
	 * @param argList option set of parsed args
	 */
	public SanitizeMODSXML(ArgList argList) {
		this(argList.get("inputPath"), argList.get("outputPath"));
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("SanitizeMODSXML");
		parser.addArgument(new ArgDef().setLongOpt("inputPath").withParameter(true, "INPUT_PATH").setDescription("Path to input file").setRequired(true));
		parser.addArgument(new ArgDef().setLongOpt("outputPath").withParameter(true, "OUTPUT_PATH").setDescription("Path of file to output (will overwrite)").setRequired(true));
		return parser;
	}
	
	/**
	 * Checks to see if the given path has a slash at the end, and if so, removes it
	 * @param path the path to check
	 * @return the path with the slash removed
	 */
	private String stripFinalSlash(String path) {
		String returnValue = path;
		if(returnValue.endsWith("/"))
			returnValue = returnValue.substring(0, returnValue.length() - 1);
		
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
	 * Initialize the mapping of bad values to their replacements
	 * @return the mapping
	 */
	private Map<String, String> generateReplacementMapping() {
		Map<String, String> mapping = new Hashtable<String, String>();
		//not using for now
		return mapping;
	}
	
	/**
	 * Sanitize all files in directory
	 * @throws IOException if an error in reading or writing occurs
	 */
	public void execute() throws IOException {
		File inputDir = new File(this.inputPath);
		File outputDir = new File(this.outputPath);
		
		if(!outputDir.exists())
			outputDir.mkdir();
		
		File[] children = inputDir.listFiles();
		for(File file : children) {
			if(file.isFile()) {
				String inputFilePath = this.inputPath + "/" + file.getName();
				String outputFilePath = this.outputPath + "/" + file.getName();
				sanitizeFile(inputFilePath, outputFilePath);
			}
		}
	}
	
	/**
	 * Sanitize a MODS XML file
	 * @param inputFilePath the path to the file to sanitize
	 * @param outputFilePath the path to which to write the sanitized file
	 * @throws IOException if an error in reading or writing occurs
	 */
	private void sanitizeFile(String inputFilePath, String outputFilePath) throws IOException {
		String xmlData = readFile(inputFilePath);
		writeFile(outputFilePath, xmlData);
	}
	
	/**
	 * Loads an entire file into a String.
	 * @param filePath the path of the file
	 * @return a String containing all data in the file
	 * @throws IOException if a read error occurs
	 */
	private String readFile(String filePath) throws IOException {
		File file = new File(filePath);
		long fileLength = file.length();
		StringBuilder builder = new StringBuilder(fileLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)fileLength);
		Reader reader = new InputStreamReader(new FileInputStream(file));
		int character = 0;
		while(character != -1) {
			character = reader.read();
			char characterAsChar = (char)character;
			builder.append(getReplacement(characterAsChar));
		}
		reader.close();
		String output = builder.toString();
		output = trimBadUnicodeCharacters(output);
		//		System.out.println(output.substring(output.length() - 10));
		//		System.out.println((int)(output.charAt(output.length() - 1)));
		return output;
	}
	
	/**
	 * Pure hack for now. readFile() is adding three Unicode-65535 characters to the end of the file for some reason.
	 * Ideally this should be prevented/filtered at the moment the character is "read", but this has been attempted and
	 * did not work. Doing it this way to bypass the problem for now.
	 * @param input the string to trim the characters off of
	 * @return the trimmed string
	 */
	private String trimBadUnicodeCharacters(String input) {
		String output = input;
		while(output.charAt(output.length() - 1) == 65535) {
			output = output.substring(0, output.length() - 1);
		}
		return output;
	}
	
	/**
	 * Writes a string to a file.
	 * @param filePath the path to the file
	 * @param content the data to write to the file
	 * @throws IOException if an error occurred writing to the file
	 */
	private void writeFile(String filePath, String content) throws IOException {
		//PrintStream outputStream = new PrintStream(new File(filePath));
		//outputStream.print(content);
		FileOutputStream outputStream = new FileOutputStream(new File(filePath));
		outputStream.write(content.getBytes());
	}
	
	/**
	 * Tests a character to see if it should be replaced with another one or combination
	 * @param character the character to test
	 * @return The character to replace
	 */
	private String getReplacement(char character) {
		String replacement;
		/*
		 * these replacements were determined by inspection. For example, it was observed that words with a Unicode 11
		 * made sense only when that character was replaced by "ff", for example
		 * "no performance di#erence between Gigabit Ethernet", where # represents Unicode 11
		 */
		switch(character) {
			case 11:
				replacement = "ff";
				break;
			case 12:
				replacement = "fi";
				break;
			case 14:
				replacement = "ffi";
				break;
			case 6:
				replacement = "&gt;"; //might be "greater than or equal to"
				break;
			default:
				replacement = String.valueOf(character);
		}
		return replacement;
	}
	
	/**
	 * Goes through a string, and replaces all instances of the keys in the replacement mapping with the values in the
	 * replacement mapping.
	 * @param input the String to perform the search and replace on
	 * @return the String with the replacements made
	 */
	@SuppressWarnings("unused")
	private String doReplacement(String input) {
		String output = input;
		Set<String> keySet = this.replacementMapping.keySet();
		for(String key : keySet) {
			output = output.replace(key, this.replacementMapping.get(key));
		}
		return output;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		try {
			InitLog.initLogger(SanitizeMODSXML.class, args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new SanitizeMODSXML(new ArgList(getParser(), args)).execute();
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
