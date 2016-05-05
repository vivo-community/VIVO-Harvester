/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * @author Eliza Chan <elc2013@med.cornell.edu>
 */
public class NameExtraCompare implements Algorithm {
	
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
		// initial vs initial or initial vs name are less reliable, hence subtract 0.1f
		// e.g. B vs B or B vs Betty
		float result  = new CaseInsensitiveInitialTest().calculate(x, y);
		if (result - 0.1f >= 0) { return (result - 0.1f); }
		return result;
	}

	@Override
	public float calculate(CharSequence itemX, CharSequence itemY, String commonNames) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
