package org.vivoweb.harvester.score.algorithm;

/**
 * @author Christopher Haines <hainesc@ufl.edu>
 */
public class NameCompare implements Algorithm {
	
	@Override
	public float calculate(CharSequence x, CharSequence y) {
		if(x == null) {
			throw new IllegalArgumentException("x cannot be null");
		}
		if(y == null) {
			throw new IllegalArgumentException("y cannot be null");
		}
		if(x.length() > 1 && y.length() > 1) {
			return new NormalizedDamerauLevenshteinDifference().calculate(x, y);
		}
		return new CaseInsensitiveInitialTest().calculate(x, y);
	}
	
}
