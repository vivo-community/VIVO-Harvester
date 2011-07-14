/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.apache.commons.codec.language.DoubleMetaphone;

/**
 * Normalized DoubleMetaphone Difference Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedDoubleMetaphoneDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		if(itemX.length() == 0 || itemY.length() == 0) {
			return 0f;
		}
		DoubleMetaphone dm = new DoubleMetaphone();
		String dmX = dm.encode(itemX.toString());
		String dmY = dm.encode(itemY.toString());
		return new NormalizedLevenshteinDifference().calculate(dmX, dmY);
	}
	
}
