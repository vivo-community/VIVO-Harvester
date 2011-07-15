/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.score.algorithm;

import org.vivoweb.harvester.util.MathAide;

/**
 * Normalized Damerau-Levenshtein Difference Score Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedDamerauLevenshteinDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		float maxSize = Math.max(itemX.length(), itemY.length()) / 1f;
		if(maxSize == 0f) {
			return 0f;
		}
		float diff = getDamerauLevenshtein(itemX, itemY);
		return ((maxSize - diff) / maxSize);
	}
	
	/**
	 * Damerau-Levenshtein Distance
	 * @param x a string
	 * @param y another string
	 * @return the distance
	 */
	protected float getDamerauLevenshtein(CharSequence x, CharSequence y) {
		if(x == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(y == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		
		int sLen = x.length();
		int tLen = y.length();
		
		if(sLen == 0) {
			return tLen;
		}
		if(tLen == 0){
			return sLen;
		}
		
		char[] source;
		char[] target;
		if(tLen > sLen) {
			int tempSize = tLen;
			tLen = sLen;
			sLen = tempSize;
			source = y.toString().toCharArray();
			target = x.toString().toCharArray();
		} else {
			source = x.toString().toCharArray();
			target = y.toString().toCharArray();
		}
		
		int sLenp1 = sLen + 1;
		int tLenp1 = tLen + 1;
		
		int[] dist = new int[tLenp1 * sLenp1];
		
		// initialize first row to 0..n
		for(int col = 0; col < sLenp1; col++) {
			dist[col] = col;
		}
		
		// initialize first column to 0..m
		int row = sLenp1;
		for(int inc = 1; inc < tLenp1; inc++) {
			dist[row] = inc;
			row += sLenp1;
		}
		
		// throughout these loops, sIm1 = sIndex-1
		int sIm1 = 0;
		for(int sIndex = 1; sIndex < sLenp1; sIndex++) {
			row = sIndex;
			
			// throughout the for tIndex loop, tIm1 = tIndex-1
			int tIm1 = 0;
			for(int tIndex = 1; tIndex < tLenp1; tIndex++) {
				// hold position of previous row
				int prev = row;
				// increment row by sLenp1 to point to the new (this iteration's) row
				row += sLenp1;

				int sameChar = ((source[sIm1] == target[tIm1]) ? 0 : 1);
				int[] editCosts = new int[3];
				editCosts[0] = dist[prev] + 1; // addition
				editCosts[1] = dist[row-1] + 1; // deletion
				editCosts[2] = dist[prev - 1] + sameChar; // substitution
				if(sameChar != 0) {
					/* transposition */
					if(sIndex < sLen && tIndex < tLen) {
						if(source[sIndex] == target[tIm1] && source[sIm1] == target[tIndex]) {
							int tr = dist[(prev) - 1];
							if(tr < editCosts[0]) {
								editCosts[0] = tr;
								}
						}
					}
				}
				int editTypeIndex = MathAide.minIntIndex(editCosts);
				distAugment(editTypeIndex, source[sIm1], target[tIm1]);
				dist[row] = editCosts[editTypeIndex];
				// tIm1 = tIndex := tIndex-1 for the next iteration
				tIm1 = tIndex;
			}
			// sIm1 = sIndex := tIndex-1 for the next iteration
			sIm1 = sIndex;
		}
		float aug = getAugment();
		resetAugment();
		return dist[row] + aug;
	}
	
	/**
	 * Reset the augmentation
	 */
	protected void resetAugment() {
		// do nothing
	}

	/**
	 * Option additional calculation hook on small[i] and big[j] to store an augmentation to the final cost
	 * @param editTypeIndex the index of the edit type
	 * @param si character from small at char (i)
	 * @param bj character from big at char (j)
	 */
	@SuppressWarnings("unused")
	protected void distAugment(int editTypeIndex, char si, char bj) {
		// do nothing
	}
	
	/**
	 * Get the augmentation for the final cost
	 * @return the augment
	 */
	protected float getAugment() {
		return 0f;
	}
}
