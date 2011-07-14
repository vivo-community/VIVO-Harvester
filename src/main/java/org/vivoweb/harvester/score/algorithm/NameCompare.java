/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * @author Christopher Haines <hainesc@ufl.edu>
 */
public class NameCompare implements Algorithm {
	
	@Override
	public float calculate(CharSequence x, CharSequence y) {
		if(x == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(y == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		if(x.length() > 1 && y.length() > 1) {
			return new NormalizedDamerauLevenshteinDifference().calculate(x, y);
		}
		return new CaseInsensitiveInitialTest().calculate(x, y);
	}
	
}
