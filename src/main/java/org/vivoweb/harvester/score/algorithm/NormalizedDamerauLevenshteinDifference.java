package org.vivoweb.harvester.score.algorithm;

import org.vivoweb.harvester.util.MathHelper;

/**
 * Normalized Damerau-Levenshtein Difference Score Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedDamerauLevenshteinDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		float maxSize = Math.max(itemX.length(), itemY.length()) / 1f;
		return ((maxSize - getDamerauLevenshtein(itemX, itemY)) / maxSize);
	}
	
	/**
	 * Damerau-Levenshtein Distance
	 * @param x a string
	 * @param y another string
	 * @return the distance
	 */
	private int getDamerauLevenshtein(CharSequence x, CharSequence y) {
		if(x == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(y == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		int sLen = x.length();
		int bLen = y.length();
		if(sLen == 0) {
			return bLen;
		}
		if(bLen == 0) {
			return sLen;
		}
		CharSequence small;
		CharSequence big;
		if(sLen > bLen) {
			big = x;
			small = y;
			int tmpLen = bLen;
			bLen = sLen;
			sLen = tmpLen;
		} else {
			big = y;
			small = x;
		}
		int[] prevprev = new int[sLen + 1];
		int[] prev = new int[sLen + 1];
		int[] dist = new int[sLen + 1];
		int[] editCosts = new int[3];
		int[] tmp; // memory holder
		char bigJm1; // big[j-1]
		int sameChar; // is small[i-1] == big[j-1]
		int editTypeIndex; // the edit type
		for(int i = 0; i <= sLen; i++) {
			prev[i] = i;
			prevprev[i] = i;
		}
		for(int j = 1; j <= bLen; j++) {
			dist[0] = j;
			bigJm1 = big.charAt(j - 1);
			for(int i = 1; i <= sLen; i++) {
				sameChar = (small.charAt(i - 1) == bigJm1) ? 0 : 1;
				editCosts[0] = dist[i - 1] + 1; // deletion
				editCosts[1] = prev[i] + 1; // addition
				editCosts[2] = prev[i - 1] + sameChar; //substitution
				editTypeIndex = MathHelper.minIntIndex(editCosts);
				dist[i] = editCosts[editTypeIndex];
				if(i > 1 && j > 1 && small.charAt(i) == big.charAt(j - 1) && small.charAt(i - 1) == big.charAt(j)) {
					dist[i] = MathHelper.minIntIndex(dist[i], prevprev[i - 2] + sameChar); // transposition
				}
			}
			tmp = prevprev; // hold existing memory
			prevprev = prev;
			prev = dist;
			dist = tmp; // reuse existing memory
		}
		return prev[sLen];
	}
}
