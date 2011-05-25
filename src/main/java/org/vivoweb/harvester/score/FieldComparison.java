package org.vivoweb.harvester.score;

import org.vivoweb.harvester.score.algorithm.Algorithm;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * A simple bean to hold the parameters for a field comparison
 * @author Christopher Haines hainesc@ufl.edu
 */
public class FieldComparison {
	/**
	 * The name for this comparison
	 */
	private String name;
	/**
	 * The algorithm used to compare these properties
	 */
	private Class<? extends Algorithm> algorithm;
	/**
	 * The property to compare from the reference model
	 */
	private Property referenceProperty;
	/**
	 * The property to compare from the input model
	 */
	private Property inputProperty;
	/**
	 * The weight to give this comparison
	 */
	private float weight;
	
	/**
	 * Constructor
	 * @param name The name for this comparison
	 * @param algorithm The algorithm used to compare these properties
	 * @param referenceProperty The property to compare from the reference model
	 * @param inputProperty The property to compare from the input model
	 * @param weight The weight to give this comparison
	 */
	public FieldComparison(String name, Class<? extends Algorithm> algorithm, Property referenceProperty, Property inputProperty, float weight) {
		this.name = name;
		this.algorithm = algorithm;
		this.referenceProperty = referenceProperty;
		this.inputProperty = inputProperty;
		this.weight = weight;
	}

	/**
	 * Get the name
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the algorithm
	 * @return the algorithm
	 */
	public Class<? extends Algorithm> getAlgorithm() {
		return this.algorithm;
	}
	
	/**
	 * Set the algorithm
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(Class<? extends Algorithm> algorithm) {
		this.algorithm = algorithm;
	}
	
	/**
	 * Get the referenceProperty
	 * @return the referenceProperty
	 */
	public Property getReferenceProperty() {
		return this.referenceProperty;
	}
	
	/**
	 * Set the referenceProperty
	 * @param referenceProperty the referenceProperty to set
	 */
	public void setVivoProperty(Property referenceProperty) {
		this.referenceProperty = referenceProperty;
	}
	
	/**
	 * Get the inputProperty
	 * @return the inputProperty
	 */
	public Property getInputProperty() {
		return this.inputProperty;
	}
	
	/**
	 * Set the inputProperty
	 * @param inputProperty the inputProperty to set
	 */
	public void setInputProperty(Property inputProperty) {
		this.inputProperty = inputProperty;
	}
	
	/**
	 * Get the weight
	 * @return the weight
	 */
	public float getWeight() {
		return this.weight;
	}
	
	/**
	 * Set the weight
	 * @param weight the weight to set
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}
}
