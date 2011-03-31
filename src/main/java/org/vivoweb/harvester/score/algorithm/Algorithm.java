/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
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
}
