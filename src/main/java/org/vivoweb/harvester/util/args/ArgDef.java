/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.args;

/**
 * Defines an Argument
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class ArgDef {
	/**
	 * The short -X type option flag
	 */
	private Character shortOption;
	/**
	 * The long --Foo type option flag
	 */
	private String longOption;
	/**
	 * Is this argument required
	 */
	private boolean req;
	/**
	 * Default String Value
	 */
	private String defaultValue;
	/**
	 * Description of this argument
	 */
	private String desc;
	/**
	 * Number of parameters for this argument (-1 is infinite)
	 */
	private int numParameters;
	/**
	 * Does this argument require a parameter
	 */
	private boolean parameterRequired;
	/**
	 * Description of argument's parameter
	 */
	private String parameterDescription;
	/**
	 * Is this argument a value map
	 */
	private boolean parameterValueMapType;
	
	/**
	 * Default Constructor
	 */
	public ArgDef() {
		this.shortOption = null;
		this.longOption = null;
		this.req = false;
		this.defaultValue = null;
		this.desc = null;
		this.numParameters = 0;
		this.parameterRequired = false;
		this.parameterDescription = null;
		this.parameterValueMapType = false;
	}
	
	/**
	 * Does this argument have at least a parameter
	 * @return true if this argument has at least a parameter
	 */
	public boolean hasParameter() {
		return (numParameters() != 0);
	}
	
	/**
	 * Does this argument have many parameters
	 * @return true if this argument has many paramters
	 */
	public boolean hasParameters() {
		return ((numParameters() > 1) || (numParameters() == -1));
	}
	
	/**
	 * Get the number of parameters for this argument
	 * @return the number of parameters for this argument (-1 is unlimited)
	 */
	public int numParameters() {
		return this.numParameters;
	}
	
	/**
	 * Does this argument require a parameter
	 * @return true if this argument has a parameter and it is required
	 */
	public boolean isParameterRequired() {
		return (hasParameter() && this.parameterRequired);
	}
	
	/**
	 * Get the description for this arguments parameter
	 * @return the description
	 */
	public String getParameterDescription() {
		return this.parameterDescription;
	}
	
	/**
	 * Is this argument a value map
	 * @return true if value map
	 */
	public boolean isParameterValueMap() {
		return this.parameterValueMapType;
	}
	
	/**
	 * Get the short -X type option flag
	 * @return the shortOption (null if not set)
	 */
	public Character getShortOption() {
		return this.shortOption;
	}
	
	/**
	 * Get the long --Foo type option flag
	 * @return the longOption (null if not set)
	 */
	public String getLongOption() {
		return this.longOption;
	}
	
	/**
	 * Is this argument required
	 * @return the required
	 */
	public boolean isRequired() {
		return this.req;
	}
	
	/**
	 * Does this argument have a parameter with a default value
	 * @return true if this argument has a parameter with a default value
	 */
	public boolean hasDefaultValue() {
		return (hasParameter() && (getDefaultValue() != null));
	}
	
	/**
	 * Get the default value
	 * @return the default value (null if never set)
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}
	
	/**
	 * Get the argument description
	 * @return the description
	 */
	public String getDescription() {
		return this.desc;
	}
	
	/**
	 * Sets the short -X type option flag
	 * @param shortOpt short option value
	 * @return this ArgDef
	 */
	public ArgDef setShortOption(char shortOpt) {
		this.shortOption = Character.valueOf(shortOpt);
		return this;
	}
	
	/**
	 * Sets the long --Foo type option flag
	 * @param longOpt long option value
	 * @return this ArgDef
	 */
	public ArgDef setLongOpt(String longOpt) {
		this.longOption = longOpt;
		return this;
	}
	
	/**
	 * Sets the description of this argument
	 * @param description description of this argument
	 * @return this ArgDef
	 */
	public ArgDef setDescription(String description) {
		this.desc = description;
		return this;
	}
	
	/**
	 * Sets this argument as required
	 * @param required is this argument required
	 * @return this ArgDef
	 */
	public ArgDef setRequired(boolean required) {
		this.req = required;
		return this;
	}
	
	/**
	 * Sets the default value
	 * @param valueDefault the default value
	 * @return this ArgDef
	 */
	public ArgDef setDefaultValue(String valueDefault) {
		this.defaultValue = valueDefault;
		return this;
	}
	
	/**
	 * Set this argument to have a parameter
	 * @param required is this parameter required
	 * @param description description of the parameter
	 * @return this ArgDef
	 */
	public ArgDef withParameter(boolean required, String description) {
		return withParameters(required, description, 1);
	}
	
	/**
	 * Set this argument to have any number of parameters
	 * @param required is this parameter required
	 * @param description description of the parameter
	 * @return this ArgDef
	 */
	public ArgDef withParameters(boolean required, String description) {
		return withParameters(required, description, -1);
	}
	
	/**
	 * Set this argument to have a set number of parameters
	 * @param required is this parameter required
	 * @param description description of the parameter
	 * @param numParams number of parameters
	 * @return this ArgDef
	 */
	public ArgDef withParameters(boolean required, String description, int numParams) {
		if(this.parameterDescription != null) {
			throw new IllegalArgumentException("Parameter Already Defined");
		}
		if(description == null) {
			throw new IllegalArgumentException("Parameter Must Have Description");
		}
		this.numParameters = numParams;
		this.parameterRequired = required;
		this.parameterDescription = description;
		return this;
	}
	
	/**
	 * Sets this argument to have a value map
	 * @param propertyName name of property
	 * @param valueName name of value
	 * @return this ArgDef
	 */
	public ArgDef withParameterValueMap(String propertyName, String valueName) {
		this.parameterValueMapType = true;
		return withParameters(false, propertyName + "=" + valueName, -1);
	}
	
	/**
	 * Get the option string in the format '( -p / --param )'
	 * @return the option string
	 */
	public String getOptionString() {
		StringBuilder sb = new StringBuilder("( ");
		if(getShortOption() != null) {
			sb.append(getShortOption());
			sb.append(" / ");
		}
		if(getLongOption() != null) {
			sb.append(getLongOption());
		}
		sb.append(" )");
		return sb.toString();
	}
}
