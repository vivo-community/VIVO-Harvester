/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.vivoweb.harvester.score.algorithm.Algorithm;

/**
 * Equality Test Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class EqualityIgnoreCaseTest implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		if(itemX == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(itemY == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		
		if(itemX.length() == 0 && itemY.length() == 0){
			return 0f;
		}
		
		if (compareToIgnoreCase(itemX, itemY) == 0){
			return 1f;
		}
		return 0f;
	}

	@Override
	public float calculate(CharSequence itemX, CharSequence itemY, String commonNames) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	   * See {@link String#compareToIgnoreCase(String)}
	   * 
	   * @param s
	   * @param t
	   * @return See {@link String#compareToIgnoreCase(String)}
	   */
	  public static int compareToIgnoreCase(CharSequence s, CharSequence t) {
	    int i = 0;

	    while (i < s.length() && i < t.length()) {
	      char a = Character.toLowerCase(s.charAt(i));
	      char b = Character.toLowerCase(t.charAt(i));

	      int diff = a - b;

	      if (diff != 0) {
	        return diff;
	      }

	      i++;
	    }

	    return s.length() - t.length();
	  }
	
}
