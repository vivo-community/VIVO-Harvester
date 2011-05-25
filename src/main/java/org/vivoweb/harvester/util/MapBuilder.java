package org.vivoweb.harvester.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool to build maps
 * @author Chris Haines hainesc@ufl.edu
 * @param <K> The Key Class
 * @param <V> The Value Class
 */
public class MapBuilder<K, V> {
	/**
	 * The map being built
	 */
	private Map<K, V> map;
	
	/**
	 * Default Constructor
	 */
	public MapBuilder() {
		this(new HashMap<K, V>());
	}
	
	/**
	 * Constructor
	 * @param startmap the map to use as a base 
	 */
	public MapBuilder(Map<K, V> startmap) {
		this.map = startmap;
	}
	
	/**
	 * Add param as map of paramName to paramValue if paramName is not null
	 * @param paramName the name of the parameter
	 * @param paramValue the value for the parameter
	 * @return this MapBuilder with the parameter added if paramName is not null
	 */
	public MapBuilder<K, V> addParam(K paramName, V paramValue) {
		if(paramName != null) {
			this.map.put(paramName, paramValue);
		}
		return this;
	}
	
	/**
	 * Add parameters from the given map
	 * @param params the mapped parameters to add
	 * @return this MapBuilder with the parameters added
	 */
	public MapBuilder<K, V> addParams(Map<? extends K,? extends V> params) {
		for(K o : params.keySet()) {
			this.map.put(o, params.get(o));
		}
		return this;
	}
	
	/**
	 * Get the Map it has been building
	 * @return the built map
	 */
	public Map<K, V> getMap() {
		return this.map;
	}
}
