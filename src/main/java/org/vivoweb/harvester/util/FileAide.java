/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assists in common tasks using Files
 * @author Christopher Haines hainesc@ufl.edu
 */
public class FileAide {
	/**
	 * SLF4J Logger
	 */
	static Logger log = LoggerFactory.getLogger(FileAide.class);
	/**
	 * Set of registered temp files to delete on JVM shutdown
	 */
	static Set<String> deleteOnExitSet;


	/**
	 * Resolves a FileObject relative to the execution directory
	 * @param path the path to resolve
	 * @return the FileObject
	 * @throws IOException error resolving
	 */
	private static FileObject getFileObject(String path) throws IOException {
		if(path == null) {
			return null;
		}
		return VFS.getManager().resolveFile(new File("."), path);
	}
	
	/**
	 * Checks if the path exists
	 * @param path the path to resolve
	 * @return true if exists, false otherwise
	 * @throws IOException error resolving path
	 */
	public static boolean exists(String path) throws IOException {
		return getFileObject(path).exists();
	}
	
	/**
	 * Deletes the file at the given path
	 * @param path the path to delete
	 * @return true if deleted, false otherwise
	 * @throws IOException error resolving path
	 */
	public static boolean delete(String path) throws IOException {
		FileObject file = getFileObject(path);
		if(!file.exists()) {
			return true;
		}
		if(isFolder(path)) {
			try {
				for(FileObject subFile : file.findFiles(new AllFileSelector())) {
					try {
						String subPath = subFile.getName().getPath();
						if(!subPath.equals(path)) {
							delete(subPath);
						}
					} catch(FileSystemException e) {
						//log.trace(e.getMessage(), e);
					}
				}
				file.delete(new AllFileSelector());
			} catch(FileSystemException e) {
				//log.trace(e.getMessage(), e);
				throw new IOException("Error deleting file!");
			}
		}
		return file.delete();
	}
	
	/**
	 * Creates a file at the given path
	 * @param path the path to the file to create
	 * @throws IOException error resolving path
	 */
	public static void createFile(String path) throws IOException {
		getFileObject(path).createFile();
	}
	
	/**
	 * Creates a folder at the given path
	 * @param path the path to the folder to create
	 * @throws IOException error resolving path
	 */
	public static void createFolder(String path) throws IOException {
		getFileObject(path).createFolder();
	}
	
	/**
	 * Determines if a folder is at the given path
	 * @param path the path to determine if it is a folder
	 * @return true if a folder, false otherwise
	 * @throws IOException error resolving path
	 */
	public static boolean isFolder(String path) throws IOException {
		FileType type = getFileObject(path).getType();
		return (type == FileType.FOLDER || type == FileType.FILE_OR_FOLDER);
	}
	
	/**
	 * Determines if a file is at the given path
	 * @param path the path to determine if it is a file
	 * @return true if a file, false otherwise
	 * @throws IOException error resolving path
	 */
	public static boolean isFile(String path) throws IOException {
		FileType type = getFileObject(path).getType();
		return (type == FileType.FILE || type == FileType.FILE_OR_FOLDER);
	}
	
	/**
	 * Determines if the given path is Readable
	 * @param path the path to determine if it is a readable
	 * @return true if readable, false otherwise
	 * @throws IOException error resolving path
	 */
	public static boolean isReadable(String path) throws IOException {
		return getFileObject(path).isReadable();
	}
	
	/**
	 * Determines if the given path is Writeable
	 * @param path the path to determine if it is a writeable
	 * @return true if writeable, false otherwise
	 * @throws IOException error resolving path
	 */
	public static boolean isWriteable(String path) throws IOException {
		return getFileObject(path).isWriteable();
	}
	
	/**
	 * Resolves the path and gets an input stream for this file
	 * @param path the path to resolve
	 * @return the InputStream
	 * @throws IOException error resolving
	 */
	public static InputStream getInputStream(String path) throws IOException {
		if(path == null) {
			return null;
		}
		return getFileObject(path).getContent().getInputStream();
	}
	
//	/**
//	 * Resolves the path and gets the contents of the file as a byte array
//	 * @param path the path to resolve
//	 * @return the byte array
//	 * @throws IOException error resolving
//	 */
//	public static byte[] getContent(String path) throws IOException {
//		if(path == null) {
//			return null;
//		}
//		FileContent content = getFileObject(path).getContent();
//		byte[] retVal = new byte[(int)content.getSize()];
//		InputStream is = content.getInputStream();
//		is.read(retVal);
//		return retVal;
//	}
	
	/**
	 * Resolves the path and gets the contents of the file as a text string
	 * @param path the path to resolve
	 * @return the string
	 * @throws IOException error resolving
	 */
	public static String getTextContent(String path) throws IOException {
		return getTextContent(path, null);
	}
	
	/**
	 * Resolves the path and gets the contents of the file as a text string
	 * @param path the path to resolve
	 * @param charsetName the characterset to use
	 * @return the string
	 * @throws IOException error resolving
	 */
	public static String getTextContent(String path, String charsetName) throws IOException {
		if(path == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		InputStreamReader isr;
		if(charsetName == null) {
			isr = new InputStreamReader(getInputStream(path));
		} else {
			isr = new InputStreamReader(getInputStream(path), charsetName);
		}
		BufferedReader br = new BufferedReader(isr);
		String temp = null;
		while((temp = br.readLine()) != null) {
			sb.append(temp);
		}
		return sb.toString();
	}
	
	/**
	 * Resolves the path and sets the contents of the file from a text string (overwrites existing files)
	 * @param path the path to resolve
	 * @param value the value to set as the text content
	 * @throws IOException error resolving
	 */
	public static void setTextContent(String path, String value) throws IOException {
		setTextContent(path, value, true);
	}
	
	/**
	 * Resolves the path and sets the contents of the file from a text string
	 * @param path the path to resolve
	 * @param value the value to set as the text content
	 * @param overwrite overwrite an existing file
	 * @throws IOException error resolving
	 */
	public static void setTextContent(String path, String value, boolean overwrite) throws IOException {
		if(path == null) {
			throw new IllegalArgumentException("File path must not be null");
		}
		BufferedWriter bw = null;
		try {
			if(!overwrite && exists(path)) {
				throw new IOException("Failed to set file text content because file " + path + " already exists.");
			}
			createFile(path);
			if(!isWriteable(path)) {
				throw new IOException("Insufficient file system privileges to modify file " + path);
			}
			bw = new BufferedWriter(new OutputStreamWriter(getOutputStream(path)));
			bw.append(value);
			bw.close();
		} catch(IOException e) {
			if(bw != null) {
				try {
					bw.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
			throw e;
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch(Exception ignore) {
					// Ignore
				}
			}
		}
	}
	
	/**
	 * Resolves the path and gets an output stream for this file
	 * @param path the path to resolve
	 * @return the OutputStream
	 * @throws IOException error resolving
	 */
	public static OutputStream getOutputStream(String path) throws IOException {
		return getOutputStream(path, false);
	}
	
	/**
	 * Resolves the path and gets an output stream for this file
	 * @param path the path to resolve
	 * @param append append to the file
	 * @return the OutputStream
	 * @throws IOException error resolving
	 */
	public static OutputStream getOutputStream(String path, boolean append) throws IOException {
		if(path == null) {
			return null;
		}
		return getFileObject(path).getContent().getOutputStream(append);
	}
	
	/**
	 * Get a set of non-hidden direct children of the given path
	 * @param path the path to search under
	 * @return a set of non-hidden direct children
	 * @throws IOException error resolving path
	 */
	public static Set<String> getNonHiddenChildren(String path) throws IOException {
		Set<String> allFileListing = new HashSet<String>();
		for(FileObject file : getFileObject(path).findFiles(Selectors.SELECT_CHILDREN)) {
			if(!file.isHidden() && (file.getType() == FileType.FILE)) {
				allFileListing.add(file.getName().getBaseName());
			}
		}
		return allFileListing;
	}
	
	/**
	 * Get an inputstream from the first file under the given path with a matching fileName
	 * @param path the path to search under
	 * @param fileName the file name to match
	 * @return an input stream to the matched file, null if none found
	 * @throws IOException error resolving path
	 */
	public static InputStream getFirstFileNameChildInputStream(String path, String fileName) throws IOException {
		for(FileObject file : FileAide.getFileObject(path).findFiles(new AllFileSelector())) {
			if(file.getName().getBaseName().equals(fileName)) {
				return file.getContent().getInputStream();
			}
		}
		return null;
	}
	
	/**
	 * Creates an empty file in the default temporary-file directory, using the given prefix and suffix to generate its name. Invoking this method is equivalent to invoking createTempFile(prefix, suffix, null).
	 * @param prefix The prefix string to be used in generating the file's name; must be at least three characters long.
	 * @param suffix The suffix string to be used in generating the file's name; may be null, in which case the suffix ".tmp" will be used.
	 * @return An abstract pathname denoting a newly-created empty file.
	 * @throws IllegalArgumentException - If the prefix argument contains fewer than three characters.
	 * @throws IOException - If a file could not be created.
	 * @throws SecurityException - If a security manager exists and its SecurityManager.checkWrite(java.lang.String) method does not allow a file to be created.
	 */
	public static synchronized File createTempFile(String prefix, String suffix) throws IOException {
		File tempFile = File.createTempFile(prefix, suffix);
		if ( deleteOnExitSet == null ) {
			deleteOnExitSet = new LinkedHashSet<String>();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					synchronized (FileAide.deleteOnExitSet) {
						for ( String fpath : FileAide.deleteOnExitSet) {
							try {
								if (!delete(fpath) ) {
									log.warn("Failed to delete temporary file space {}, please remove manually  ",fpath);
								} else {
									log.trace("Deleted temporary file space {}  ",fpath);
								}
							} catch(IOException e) {
								log.warn("Failed to delete temporary file space {}, please remove manually  ",fpath);
							}
						}
						FileAide.deleteOnExitSet.clear();
						FileAide.deleteOnExitSet = null;
					}
				}
			});
		}
		synchronized (FileAide.deleteOnExitSet) {
			FileAide.deleteOnExitSet.add(tempFile.getAbsolutePath());
			log.trace("Allocating temporary file space {}  ",tempFile.getAbsolutePath());
		}
		return tempFile;
	}
}
