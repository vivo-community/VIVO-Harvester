/*******************************************************************************
 * Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri. All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which
 * accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence - initial API and implementation Christopher
 * Barnes, Narayan Raum - scoring ideas and algorithim Yang Li - pairwise scoring Algorithm Christopher Barnes - regex
 * scoring algorithim
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

/**
 * Equality Test Algorithm
 * @author Michael Barbieri mbarbier@ufl.edu
 */
public class InsensitiveFirstInitialTest implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		float testResult = 0f;
		
		if(itemX.length() > 0 && itemY.length() > 0) {
			char loweredItemXInitial = Character.toLowerCase(itemX.charAt(0)); 
			char loweredItemYInitial = Character.toLowerCase(itemY.charAt(0));
			if(loweredItemXInitial == loweredItemYInitial) {
				testResult = 1f;
			}
		}
		return testResult;
	}
	
}
