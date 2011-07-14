/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * First Character Equality Test Algorithm
 * @author Michael Barbieri mbarbier@ufl.edu
 */
public class CaseInsensitiveInitialTest implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		float testResult = 0f;
		
		if(itemX.length() > 0 && itemY.length() > 0) {
			char loweredItemXInitial = Character.toLowerCase(itemX.toString().trim().charAt(0)); 
			char loweredItemYInitial = Character.toLowerCase(itemY.toString().trim().charAt(0));
			if(loweredItemXInitial == loweredItemYInitial) {
				testResult = 1f;
			}
		}
		return testResult;
	}
	
}
