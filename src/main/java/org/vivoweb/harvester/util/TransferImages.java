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
 * @author Sivananda Reddy Thummala Abbigari, 64254635
 */
public class TransferImages {
	
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(TransferImages.class);
	
	/**
	 * Path to the ImageScript directory
	 */
	private String pathToImageScriptDirectory;
	
	/**
	 * Path to the EmpoyeeID's file
	 */
	private String pathToEmployeeIDsFile;
	
	/**
	 * Contains the list of Employee ID's who doesn't have images in VIVO
	 */
	private HashSet<String> employeeIDSet;
	
	/**
	 * Used for efficiently reading Employee ID's's form employeeIDs.txt file
	 */
	private BufferedReader bufferReader;
	
	/**
	 * Used for reading files the images directory
	 */
	private File folder;
	
	
	/**
	 * Stores the Employee ID's's in a HashSet
	 * @param path the path to the text file containing Employee ID's
	 * @throws FileNotFoundException cannot find employeeIDs.txt
	 * @throws IOException error reading from employeeIDs.txt
	 * 
	 */
	private void getEmployeeIDs(String path) throws FileNotFoundException, IOException {		
		String tempLine;				
		try {
			this.bufferReader = new BufferedReader(new FileReader(path));						
			while((tempLine = this.bufferReader.readLine()) != null) 
			{				
				this.employeeIDSet.add(tempLine.substring(0, 8));
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
	
		Process moveFullImageToUploadDirectory;
		Process moveThumbnailToUploadDirectory;
		Process moveFullImageToBackUpDirectory;		
		Process moveThumbnailToBackUpDirectory;
		String fileName;				
		try {			
			for(File f : this.folder.listFiles()) {
				//if(new MimetypesFileTypeMap().getContentType(f).contains("image")) { //Uncomment this if you need to explicitly check for the mime type of the image 					
					fileName = f.getName();							
					if(this.employeeIDSet.contains(fileName.substring(0,8))) {							
						moveFullImageToUploadDirectory = Runtime.getRuntime().exec("mv " + this.pathToImageScriptDirectory + "/fullImages/" + fileName + " " + this.pathToImageScriptDirectory + "/upload/fullImages/");						
						moveFullImageToUploadDirectory.waitFor();
						moveFullImageToUploadDirectory.getInputStream().close();
						moveFullImageToUploadDirectory.getOutputStream().close();
						moveFullImageToUploadDirectory.getErrorStream().close();
						
						moveThumbnailToUploadDirectory = Runtime.getRuntime().exec("mv " + this.pathToImageScriptDirectory + "/thumbnails/"+"thumbnail" + fileName + " " + this.pathToImageScriptDirectory + "/upload/thumbnails/");																			
						moveThumbnailToUploadDirectory.waitFor();
						moveThumbnailToUploadDirectory.getInputStream().close();
						moveThumbnailToUploadDirectory.getOutputStream().close();
						moveThumbnailToUploadDirectory.getErrorStream().close();
					} else {																
						moveFullImageToBackUpDirectory = Runtime.getRuntime().exec("mv " + this.pathToImageScriptDirectory + "/fullImages/" + fileName + " " + this.pathToImageScriptDirectory + "/backup/fullImages/");						
						moveFullImageToBackUpDirectory.waitFor();
						moveFullImageToBackUpDirectory.getInputStream().close();
						moveFullImageToBackUpDirectory.getOutputStream().close();
						moveFullImageToBackUpDirectory.getErrorStream().close();
						
						moveThumbnailToBackUpDirectory = Runtime.getRuntime().exec("mv " + this.pathToImageScriptDirectory + "/thumbnails/" +"thumbnail" + fileName + " " + this.pathToImageScriptDirectory + "/backup/thumbnails/");												
						moveThumbnailToBackUpDirectory.waitFor();
						moveThumbnailToBackUpDirectory.getInputStream().close();
						moveThumbnailToBackUpDirectory.getOutputStream().close();
						moveThumbnailToBackUpDirectory.getErrorStream().close(); 												
					} 
				//}
			}
		} catch(IOException e) {
			throw new IOException(e);
		} catch(InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("TransferImages");
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("pathToImageScriptDirectory").withParameter(true, "PATH").setDescription("path to the Image Script Directory").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('e').setLongOpt("pathToEmployeedIDsFile").withParameter(true, "PATH").setDescription("path to the EmployeeID's file").setRequired(true));
		return parser;
	}
	
	/**
	 * Command line Constructor
	 * @param args command line arguments
	 * @throws UsageException 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	private TransferImages(String[] args) throws IllegalArgumentException, IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * ArgList Constructor
	 * @param argList option set of parsed args
	 */
	private TransferImages(ArgList argList) {
		this(argList.get("p"), argList.get("e"));		
	}
	
	/**
	 * Library style Constructor
	 * @param pathToImageScriptFolder
	 */
	public TransferImages(String pathToImageScriptFolder, String pathToEmployeeIDsFile) 
	{
		this.pathToImageScriptDirectory = pathToImageScriptFolder;
		this.pathToEmployeeIDsFile = pathToEmployeeIDsFile; 
		this.employeeIDSet = new HashSet<String>();
		this.folder = new File(this.pathToImageScriptDirectory + "/fullImages");
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	public void execute() throws IOException
	{
		
		getEmployeeIDs(this.pathToEmployeeIDsFile);		
		transferImages();
		log.info("Transfered images to upload and backup directories!");
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
			new TransferImages(args).execute();
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