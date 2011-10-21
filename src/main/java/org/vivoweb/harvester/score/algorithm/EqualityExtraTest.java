/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * Equality Test Algorithm
 * @author Eliza Chan elc2013@med.cornell.edu
 */
public class EqualityExtraTest implements Algorithm {
	
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
		float result = this.calculate(itemX, itemY);
		if (result == 1f) {
			boolean isCommonName = false;
			String[] commonNameList = commonNames.split(",");
			for (String commonName: commonNameList) {
				if (itemX.toString().equals(commonName.trim())) {
					isCommonName = true;
					break;
				}
			}
			if (!isCommonName) { result = 1.1f; } // gets 0.1f bonus if name is not in the list of common names
		} 
		return result;
	}
	
}
