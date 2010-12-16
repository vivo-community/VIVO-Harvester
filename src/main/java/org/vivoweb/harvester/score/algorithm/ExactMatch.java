package org.vivoweb.harvester.score.algorithm;

/**
 * Exact Match Algorithm
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class ExactMatch implements Algorithm {
	
	@Override
	public double calculate(String itemX, String itemY) {
		if(itemX.equals(itemY)) {
			return 1;
		}
		return 0;
	}
	
}
