/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/

package org.vivoweb.harvester.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import javax.activation.MimetypesFileTypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;

/**
 * This Class takes the images directory and segregates them in to two folders upload and backup
 * @author name ufid
 */
public class CreateImageFolders {
	
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(CreateImageFolders.class);
	
	/**
	 * Path to the ImageScript directory
	 */
	private String pathToImageScriptDirectory;
	
	/**
	 * Contains the list of UFID's who doesn't have images in VIVO
	 */
	private HashSet<String> ufidSet;
	
	/**
	 * 
	 */
	private BufferedReader bufferReader;
	
	/**
	 * 
	 */
	private File folder;
	
	
	/**
	 * Stores the Ufid's in a HashSet
	 * @param path the path to the text file containing Ufid's
	 * @throws FileNotFoundException cannot find ufids.txt
	 * @throws IOException error reading from ufids.txt
	 * 
	 */
	private void getUfids(String path) throws FileNotFoundException, IOException {		
		String tempLine;				
		try {
			this.bufferReader = new BufferedReader(new FileReader(path + "/ufids.txt"));						
			while((tempLine = this.bufferReader.readLine()) != null) 
			{				
				this.ufidSet.add(tempLine.substring(0, 8));
			}
			this.bufferReader.close();
		} catch(FileNotFoundException e) {
			throw new IOException(e);
		} catch(IOException e) {
			throw new IOException(e);
		}
	}
	

	/**
	 * @throws IOException error executing the "mv" command 
	 */
	private void transferImages() throws IOException {
	
		String fileName;
		/*
		 * Make sure to create upload and backup folders in the script if they are not created!
		 */
		//Runtime.getRuntime().exec("mkdir " + getPathToImageDirectory() + "/upload " + path + "/backup");		
				
		try {			
			for(File f : this.folder.listFiles()) {
				if(new MimetypesFileTypeMap().getContentType(f).contains("image")) {
					fileName = f.getName();
					System.out.println("Image name:"+fileName);
					if(this.ufidSet.contains(fileName.substring(0, 8))) {							
						Runtime.getRuntime().exec("mv " + this.pathToImageScriptDirectory + "/images/" + fileName + " " + this.pathToImageScriptDirectory + "/upload");						
					} else {						
						Runtime.getRuntime().exec("mv " + this.pathToImageScriptDirectory + "/images/" + fileName + " " + this.pathToImageScriptDirectory + "/backup");						
					}
				}
			}
		} catch(IOException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("CreateImageFolders");
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("pathToImageScriptDirectory").withParameter(true, "PATH").setDescription("path to the Image Script Directory").setRequired(true));
		return parser;
	}
	
	/**
	 * Command line Constructor
	 * @param args command line arguments
	 * @throws UsageException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private CreateImageFolders(String[] args) throws IllegalArgumentException, IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * ArgList Constructor
	 * @param argList option set of parsed args
	 */
	private CreateImageFolders(ArgList argList) {
		this(argList.get("p"));
	}
	
	/**
	 * Library style Constructor
	 * @param pathToImageScriptFolder
	 */
	public CreateImageFolders(String pathToImageScriptFolder) 
	{
		this.pathToImageScriptDirectory = pathToImageScriptFolder;
		this.ufidSet = new HashSet<String>();
		this.folder = new File(this.pathToImageScriptDirectory + "/images");
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void execute() throws IOException
	{
		getUfids(this.pathToImageScriptDirectory);
		transferImages();
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
			new CreateImageFolders(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:", e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
}