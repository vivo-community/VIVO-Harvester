/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * ConsoleLogFilter
 * @author Christopher Haines
 */
public class ConsoleLogFilter extends Filter<ILoggingEvent> {
	
	/**
	 * The current Logging Level
	 */
	private static Level logLevel = Level.INFO;
	
	/**
	 * Set the logging level
	 * @param level the level to set, if invalid results in INFO
	 */
	public static void setLogLevel(String level) {
		logLevel = Level.toLevel(level, Level.INFO);
	}
	
	/**
	 * Get the current logging level
	 * @return the current logging level
	 */
	public static String getLogLevel() {
		return logLevel.toString();
	}
	
	@Override
	public FilterReply decide(ILoggingEvent event) {
		if(event.getLevel().isGreaterOrEqual(logLevel)) {
			return FilterReply.ACCEPT;
		}
		return FilterReply.NEUTRAL;
	}
}
