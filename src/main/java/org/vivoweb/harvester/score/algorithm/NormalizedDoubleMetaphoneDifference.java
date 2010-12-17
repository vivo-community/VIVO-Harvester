/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation Christopher
 * Barnes, Narayan Raum - scoring ideas and algorithim Yang Li - pairwise scoring Algorithm Christopher Barnes - regex
 * scoring algorithim
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.apache.commons.codec.language.DoubleMetaphone;

/**
 * Normalized DoubleMetaphone Difference Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedDoubleMetaphoneDifference implements Algorithm {
	
	@Override
	public double calculate(String itemX, String itemY) {
		DoubleMetaphone dm = new DoubleMetaphone();
		String dmX = dm.encode(itemX);
		String dmY = dm.encode(itemY);
		return new NormalizedLevenshteinDifference().calculate(dmX, dmY);
	}
	
}
