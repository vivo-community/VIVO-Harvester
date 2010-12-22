package org.vivoweb.harvester.score.algorithm;

import java.util.HashMap;
import java.util.Map;
import org.vivoweb.harvester.util.MathHelper;

/**
 * Calculates the difference of two strings and accounts for typos
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class NormalizedTypoDifference implements Algorithm {
	
	@Override
	public float calculate(CharSequence itemX, CharSequence itemY) {
		int score = 0;
		char[] x = itemX.toString().toCharArray();
		char[] y = itemY.toString().toCharArray();
		int lenDiff = x.length - y.length;
		if(lenDiff == 0) {
			//same length
			
		} else if(lenDiff > 0) {
			//x is longer
			
		} else if(lenDiff < 0) {
			//y is longer
			
		}
		for(int a = 0; a < x.length; a++) {
			for(int b = 0; b < y.length; b++) {
				if(x[a] == y[b]) {
					score++;
				}
			}
		}
		return 0f;
	}
	
	/**
	 * US Enlgish Standard Keyboard Proximity Map
	 */
	public static final Map<Character,Map<Character,Float>> USEngKeyboard;
	static {
		 Map<Character, Map<Character, Float>> tmp = new HashMap<Character, Map<Character,Float>>();
		 tmp.put(Character.valueOf('q'), new HashMap<Character, Float>());
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('w'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('a'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('s'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('1'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('2'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('!'), Float.valueOf(.3f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('@'), Float.valueOf(.3f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('3'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('e'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('d'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('x'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('z'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('`'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('~'), Float.valueOf(.1f));
		 tmp.get(Character.valueOf('q')).put(Character.valueOf('#'), Float.valueOf(.1f));
		 tmp.put(Character.valueOf('w'), new HashMap<Character, Float>());
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('q'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('a'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('s'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('d'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('e'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('2'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('3'), Float.valueOf(.5f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('@'), Float.valueOf(.3f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('#'), Float.valueOf(.3f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('r'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('f'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('c'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('x'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('z'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('4'), Float.valueOf(.25f));
		 tmp.get(Character.valueOf('w')).put(Character.valueOf('z'), Float.valueOf(.1f));
		 USEngKeyboard = tmp;
	}
	
	/**
	 * Typo-Accounting Modified Damerau-Levenshtein Distance
	 * @param x a string
	 * @param y another string
	 * @param keyWeights map of charA to charB to float showing how much to decrease distance when a substition of charA to charB happens
	 * @return the distance
	 */
	public static float getTypoDamerauLevenshtein(CharSequence x, CharSequence y, Map<Character,Map<Character,Float>> keyWeights) {
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
		if (bLen == 0) {
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
		int[] prevprev = new int[sLen+1];
		int[] prev = new int[sLen+1];
		int[] dist = new int[sLen+1];
		int[] editCosts = new int[3];
		int[] tmp; // memory holder
		char bigJm1; // big[j-1]
		int sameChar; // is small[i-1] == big[j-1]
		int editTypeIndex; // the edit type
		float decost = 0f;
		for(int i = 0; i <= sLen; i++) {
			prev[i] = i;
			prevprev[i] = i;
		}
		for(int j = 1; j <= bLen; j++) {
			dist[0] = j;
			bigJm1 = big.charAt(j-1);
			for(int i = 1; i <= sLen; i++) {
				sameChar = (small.charAt(i-1) == bigJm1) ? 0 : 1;
				editCosts[0] = dist[i-1] + 1; // deletion
				editCosts[1] = prev[i] + 1; // addition
				editCosts[2] = prev[i-1] + sameChar; //substitution
				editTypeIndex = MathHelper.minIntIndex(editCosts);
				if(editTypeIndex == 2) {
					try {
						System.out.println(small.charAt(i)+" => "+big.charAt(j));
						decost += keyWeights.get(Character.valueOf(small.charAt(i))).get(Character.valueOf(big.charAt(j))).floatValue();
					} catch(NullPointerException e) {
						// mapping must for small[i] to big[j] must not exist - ignore
					}
				}
				dist[i] = editCosts[editTypeIndex];
				if(i > 1 && j > 1 && small.charAt(i) == big.charAt(j-1) && small.charAt(i-1) == big.charAt(j)) {
					dist[i] = MathHelper.minIntIndex(dist[i], prevprev[i-2] + sameChar); // transposition
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
