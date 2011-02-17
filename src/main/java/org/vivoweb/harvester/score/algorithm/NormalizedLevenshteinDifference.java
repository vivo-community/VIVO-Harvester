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

import org.apache.commons.lang.StringUtils;

/**
 * Normalized Levenshtein Difference Score Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedLevenshteinDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		float maxSize = Math.max(itemX.length(), itemY.length()) / 1f;
		return ((maxSize - StringUtils.getLevenshteinDistance(itemX.toString(), itemY.toString())) / maxSize);
	}
	
}
