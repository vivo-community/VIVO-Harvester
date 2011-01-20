/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.File;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Initialize Logging
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class InitLog {
	
	/**
	 * Get the first non-null non-post-trim-empty string
	 * @param strings set of strings
	 * @return the first non-null non-post-trim-empty string
	 */
	private static String getFirstValidString(String... strings) {
		for(String s : strings) {
			if(s != null && !s.trim().isEmpty()) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Setup the logger
	 * @param classname the classname initializing the log
	 */
	public static void initLogger(@SuppressWarnings("unused") Class<?> classname) {
		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
		String task = getFirstValidString(System.getenv("HARVESTER_TASK"), System.getProperty("harvester-task"), "harvester."+DateFormatUtils.ISO_DATETIME_FORMAT.format(System.currentTimeMillis()));
		context.putProperty("harvester-task", task);
		String process = getFirstValidString(System.getenv("PROCESS_TASK"), System.getProperty("process-task"), "all");
		context.putProperty("process-task", process);
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		context.reset();
		try {
			for(FileObject file : VFS.getManager().toFileObject(new File(".")).findFiles(new AllFileSelector())) {
				if(file.getName().getBaseName().equals("logback.xml")) {
					jc.doConfigure(file.getContent().getInputStream());
					break;
				}
			}
		} catch(FileSystemException e) {
			throw new IllegalArgumentException(e);
		} catch(JoranException e) {
			throw new IllegalArgumentException(e);
		}
	}
}