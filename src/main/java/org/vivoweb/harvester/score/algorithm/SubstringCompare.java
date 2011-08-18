/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * This Algorithm returns 1.0 if one of the strings is a substring of the other.
 *
 */
public class SubstringCompare implements Algorithm {
	
	@Override
	public float calculate(CharSequence x, CharSequence y) {
		if(x == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(y == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		if(x.length() > 0 && y.length() > 0) {
			String longString,shortString;
			if(x.length() < y.length()){
				longString = y.toString();
				shortString = x.toString();
			}else{
				longString = x.toString();
				shortString = y.toString();
			}
			if(longString.contains(shortString)){
				return 1.0f;
			}
		}
		return 0;
	}
	
}
