/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util;

/**
 * Set of math helper methods
 * @author Christopher Haines hainesc@ctrip.ufl.edu
 */
public class MathHelper {
	/**
	 * Find minimum of a set of ints
	 * @param d set of ints
	 * @return the index of the minimum in the set
	 */
	public static int minIntIndex(int... d) {
		if((d == null) || (d.length == 0)) {
			throw new IllegalArgumentException("d cannot be null");
		}
		int index = 0;
		for(int x = 0; x < d.length; x++) {
			if(d[x] < d[index]) {
				index = x;
			}
		}
		return index;
	}
	
	/**
	 * Find minimum of a set of doubles
	 * @param d set of doubles
	 * @return the index of the minimum in the set
	 */
	public static double minDoubleIndex(double... d) {
		if((d == null) || (d.length == 0)) {
			throw new IllegalArgumentException("d cannot be null");
		}
		int index = 0;
		for(int x = 0; x < d.length; x++) {
			if(d[x] < d[index]) {
				index = x;
			}
		}
		return index;
	}
	
	/**
	 * Find minimum of a set of floats
	 * @param d set of floats
	 * @return the index of the minimum in the set
	 */
	public static float minFloatIndex(float... d) {
		if((d == null) || (d.length == 0)) {
			throw new IllegalArgumentException("d cannot be null");
		}
		int index = 0;
		for(int x = 0; x < d.length; x++) {
			if(d[x] < d[index]) {
				index = x;
			}
		}
		return index;
	}
	
	/**
	 * Find minimum of a set of longs
	 * @param d set of longs
	 * @return the index of the minimum in the set
	 */
	public static long minLongIndex(long... d) {
		if((d == null) || (d.length == 0)) {
			throw new IllegalArgumentException("d cannot be null");
		}
		int index = 0;
		for(int x = 0; x < d.length; x++) {
			if(d[x] < d[index]) {
				index = x;
			}
		}
		return index;
	}
}
