/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * Equality Test Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class EqualityTest implements Algorithm {
	
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
		
		if(itemX.equals(itemY)) {
			return 1f;
		}
		return 0f;
	}

	@Override
	public float calculate(CharSequence itemX, CharSequence itemY, String commonNames) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
