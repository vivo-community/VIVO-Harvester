/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/

package org.vivoweb.harvester.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

/**
 * Create Image Folders
 */
public class CreateImageFolders {
	
	/**
	 * Main Method
	 * @param args command-line arguments
	 */
	public static void main(String args[]) {
		try {
			//Get the directory name
			String path = args[0];
			
			//Store the list of UFID's of the people who don't have an image in VIVO
			HashSet<String> listFromVivo = new HashSet<String>();			
			BufferedReader bufferReader = new BufferedReader(new FileReader(path + "/ufids.txt"));
			String tempLine;
			while((tempLine = bufferReader.readLine()) != null) {		
				listFromVivo.add(tempLine.substring(0,8));
			}
			bufferReader.close();
			
			//Create and transfer images to upload and backup folders
			Runtime.getRuntime().exec("mkdir " + path + "/upload " + path + "/backup");
			File folder = new File(path+"/images");
			for(File f : folder.listFiles()) {
				if(f.isFile()) {
					String fileName = f.getName();
					if(listFromVivo.contains(fileName.substring(0, 8))) {
						Runtime.getRuntime().exec("mv " + path + "/images/" + fileName + " " + path + "/upload");
					} else {
						Runtime.getRuntime().exec("mv " + path + "/images/" + fileName + " " + path + "/backup");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}