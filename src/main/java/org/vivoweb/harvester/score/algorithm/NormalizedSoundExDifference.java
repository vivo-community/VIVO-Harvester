/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;

/**
 * Normalized SoundEx Difference Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedSoundExDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		try {
			int diff = Soundex.US_ENGLISH.difference(itemX.toString(), itemY.toString());
			return (diff / 4f);
		} catch(EncoderException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
}
