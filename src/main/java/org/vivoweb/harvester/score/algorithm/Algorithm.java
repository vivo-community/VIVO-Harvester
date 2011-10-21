/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * Interface for Score algorithms
 * Using this standardized interface allows any custom algorithms to be added easily
 * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
 * @author Stephen Williams svwilliams@ctrip.ufl.edu
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public interface Algorithm {
	/**
	 * Perform a calculation to determine what percent match the given Strings are
	 * @param itemX compare this with the other String
	 * @param itemY compare this with the other String
	 * @return a float (0.0, 1.0) representing what percent match the given Strings are
	 */
	public abstract float calculate(CharSequence itemX, CharSequence itemY);
	
	/**
	 * Perform a calculation to determine what percent match the given Strings are.
	 * A list of common names are given so that the score can be modified depending on how common the last name is.
	 * @param itemX compare this with the other String
	 * @param itemY compare this with the other String
	 * @param commonNames list of common names for modifying the score as needed
	 * @return a float (0.0, 1.0, 1.1) representing what percent match the given Strings are
	 */
	public abstract float calculate(CharSequence itemX, CharSequence itemY, String commonNames);
}
