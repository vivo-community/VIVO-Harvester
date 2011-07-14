/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
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
	 * @param level the log level
	 */
	private static void setLogLevel(String level) {
		String logLevel = level;
		if(logLevel == null) {
			logLevel = "";
		}
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
		} else {
			System.clearProperty("console-trace");
			System.clearProperty("console-debug");
			System.clearProperty("console-info");
			System.clearProperty("console-warnerror");
		}
		LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
		String task = getFirstValidString(System.getenv("HARVESTER_TASK_DATE"), System.getProperty("harvester-task"), "harvester." + DateFormatUtils.ISO_DATETIME_FORMAT.format(System.currentTimeMillis()));
		context.putProperty("harvester-task", task);
		String process = getFirstValidString(System.getenv("PROCESS_TASK"), System.getProperty("process-task"), "all");
		context.putProperty("process-task", process);
		JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(context);
		
		try {
			InputStream is;
			is = FileAide.getFirstFileNameChildInputStream(".", "logback-test.xml");
			if(is == null) {
				is = FileAide.getFirstFileNameChildInputStream(".", "logback.xml");
			}
			if(is == null) {
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream("logback-test.xml");
			}
			if(is == null) {
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream("logback.xml");
			}
			if(is != null) {
				context.reset();
				context.stop();
				jc.doConfigure(is);
				context.start();
			}
		} catch(IOException e) {
			throw new IllegalArgumentException(e);
		} catch(JoranException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * Setup the logger
	 * @param args the commandline args passed
	 * @param parser the arg parser to use
	 * @param noLogIfNotSetFlags will turn off console logging if any of these flags are not set (note: wordiness will overwrite this)
	 * @throws IOException error processing
	 * @throws UsageException user requested usage message
	 */
	public static void initLogger(String[] args, ArgParser parser, String... noLogIfNotSetFlags) throws IOException, UsageException {
		String logLevel = System.getProperty("console-log-level");
		String harvLevel = System.getProperty("harvester-level");
		System.setProperty("harvester-level", "OFF");
		if((args != null) && (parser != null)) {
			ArgList argList = parser.parse(args, false);
			for(String testFlag : noLogIfNotSetFlags) {
				boolean test = false;
				try {
					test = (!argList.has(testFlag));
				} catch (IllegalArgumentException e) {
					for(String subFlag : testFlag.split("|")) {
						try {
							if(argList.has(subFlag)) {
								test = false;
								break;
							}
						} catch (IllegalArgumentException se) {
							test = true;
						}
					}
				}
				if(test) {
					logLevel = "OFF";
					break;
				}
			}
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
