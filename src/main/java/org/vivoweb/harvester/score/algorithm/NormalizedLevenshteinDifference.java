/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.apache.commons.lang.StringUtils;

/**
 * Normalized Levenshtein Difference Score Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedLevenshteinDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		if(itemX == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(itemY == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		
		float maxSize = Math.max(itemX.length(), itemY.length()) / 1f;
		if (maxSize == 0f) {
			return 0f;
		}
		return ((maxSize - StringUtils.getLevenshteinDistance(itemX.toString(), itemY.toString())) / maxSize);
	}
	
}
