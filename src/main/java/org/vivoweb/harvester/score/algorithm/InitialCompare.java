/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * This algorithm compares the initial characters in the two strings and returns a  1.0 if they are equal and a 0.0 if they are not.
 *
 */
public class InitialCompare implements Algorithm {
	
	@Override
	public float calculate(CharSequence x, CharSequence y) {
		if(x == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(y == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		if(x.length() > 0 && y.length() > 0) {
			return (x.charAt(0) == y.charAt(0)) ? 1.0f : 0.0f;
		}
		return 0;
	}
	
}
