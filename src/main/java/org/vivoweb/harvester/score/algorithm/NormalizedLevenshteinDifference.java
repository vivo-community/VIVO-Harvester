/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation Christopher
 * Barnes, Narayan Raum - scoring ideas and algorithim Yang Li - pairwise scoring Algorithm Christopher Barnes - regex
 * scoring algorithim
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.apache.commons.lang.StringUtils;

/**
 * Normalized Levenshtein Difference Score Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedLevenshteinDifference implements Algorithm {
	
	@Override
	public float calculate(String itemX, String itemY) {
		float maxSize = Math.max(itemX.length(), itemY.length())/1f;
		return ((maxSize - StringUtils.getLevenshteinDistance(itemX, itemY))/maxSize);
	}
	
}
