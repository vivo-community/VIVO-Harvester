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
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VFS;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
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
			if((s != null) && !s.trim().isEmpty()) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * Set the log level variables
	 * @param logLevel the log level
	 */
	private static void setLogLevel(String logLevel) {
		if(logLevel != null) {
			if(logLevel.equalsIgnoreCase("trace") || logLevel.equalsIgnoreCase("all")) {
				System.setProperty("console-trace", "ACCEPT");
				System.setProperty("console-debug", "ACCEPT");
				System.setProperty("console-info", "ACCEPT");
				System.setProperty("console-warnerror", "WARN");
			} else if(logLevel.equalsIgnoreCase("debug")) {
				System.setProperty("console-trace", "DENY");
				System.setProperty("console-debug", "ACCEPT");
				System.setProperty("console-info", "ACCEPT");
				System.setProperty("console-warnerror", "WARN");
			} else if(logLevel.equalsIgnoreCase("info")) {
				System.setProperty("console-trace", "DENY");
				System.setProperty("console-debug", "DENY");
				System.setProperty("console-info", "ACCEPT");
				System.setProperty("console-warnerror", "WARN");
			} else if(logLevel.equalsIgnoreCase("warn")) {
				System.setProperty("console-trace", "DENY");
				System.setProperty("console-debug", "DENY");
				System.setProperty("console-info", "DENY");
				System.setProperty("console-warnerror", "WARN");
			} else if(logLevel.equalsIgnoreCase("error")) {
				System.setProperty("console-trace", "DENY");
				System.setProperty("console-debug", "DENY");
				System.setProperty("console-info", "DENY");
				System.setProperty("console-warnerror", "ERROR");
			} else if(logLevel.equalsIgnoreCase("off")) {
				System.setProperty("console-trace", "DENY");
				System.setProperty("console-debug", "DENY");
				System.setProperty("console-info", "DENY");
				System.setProperty("console-warnerror", "OFF");
			}
		}
		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
		String task = getFirstValidString(System.getenv("HARVESTER_TASK_DATE"), System.getProperty("harvester-task"), "harvester." + DateFormatUtils.ISO_DATETIME_FORMAT.format(System.currentTimeMillis()));
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
	
	/**
	 * Setup the logger
	 * @param args the commandline args passed
	 * @param parser the arg parser to use
	 * @throws IOException error processing
	 */
	public static void initLogger(String[] args, ArgParser parser) throws IOException {
		String logLevel = System.getProperty("console-log-level");
		String harvLevel = System.getProperty("harvester-level");
		System.setProperty("harvester-level", "OFF");
		setLogLevel("OFF");
		if((args != null) && (parser != null)) {
			ArgList argList = new ArgList(parser, args);
			if(argList.has("w")) {
				logLevel = argList.get("w");
			}
		}
		if(harvLevel != null) {
			System.setProperty("harvester-level", harvLevel);
		} else {
			System.clearProperty("harvester-level");
		}
		setLogLevel(logLevel);
	}
}