/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util.workflow;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*; 
/**
 * Vocabulary definitions
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class WorkflowOntology {
    /** <p>The ontology model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://vivoweb.org/ontology/harvesterWorkflow#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     * @return {@link #NS}
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>retractionsModel</p> */
    public static final ObjectProperty retractionsModel = m_model.createObjectProperty( NS + "retractionsModel" );
    
    /** <p>processorMethod</p> */
    public static final ObjectProperty processorMethod = m_model.createObjectProperty( NS + "processorMethod" );
    
    /** <p>originalPropertyOf</p> */
    public static final ObjectProperty originalPropertyOf = m_model.createObjectProperty( NS + "originalPropertyOf" );
    
    /** <p>firstStepOf</p> */
    public static final ObjectProperty firstStepOf = m_model.createObjectProperty( NS + "firstStepOf" );
    
    /** <p>additionsModel</p> */
    public static final ObjectProperty additionsModel = m_model.createObjectProperty( NS + "additionsModel" );
    
    /** <p>originalProperty</p> */
    public static final ObjectProperty originalProperty = m_model.createObjectProperty( NS + "originalProperty" );
    
    /** <p>newPropertyOf</p> */
    public static final ObjectProperty newPropertyOf = m_model.createObjectProperty( NS + "newPropertyOf" );
    
    /** <p>processorClassOf</p> */
    public static final ObjectProperty processorClassOf = m_model.createObjectProperty( NS + "processorClassOf" );
    
    /** <p>retractionsModelOf</p> */
    public static final ObjectProperty retractionsModelOf = m_model.createObjectProperty( NS + "retractionsModelOf" );
    
    /** <p>firstStep</p> */
    public static final ObjectProperty firstStep = m_model.createObjectProperty( NS + "firstStep" );
    
    /** <p>modelToAdd</p> */
    public static final ObjectProperty modelToAdd = m_model.createObjectProperty( NS + "modelToAdd" );
    
    /** <p>modelToSubtract</p> */
    public static final ObjectProperty modelToSubtract = m_model.createObjectProperty( NS + "modelToSubtract" );
    
    /** <p>smushedOnInAction</p> */
    public static final ObjectProperty smushedOnInAction = m_model.createObjectProperty( NS + "smushedOnInAction" );
    
    /** <p>actionOf</p> */
    public static final ObjectProperty actionOf = m_model.createObjectProperty( NS + "actionOf" );
    
    /** <p>newProperty</p> */
    public static final ObjectProperty newProperty = m_model.createObjectProperty( NS + "newProperty" );
    
    /** <p>splitRegex</p> */
    public static final ObjectProperty splitRegex = m_model.createObjectProperty( NS + "splitRegex" );
    
    /** <p>processorClass</p> */
    public static final ObjectProperty processorClass = m_model.createObjectProperty( NS + "processorClass" );
    
    /** <p>sourceModelFor</p> */
    public static final ObjectProperty sourceModelFor = m_model.createObjectProperty( NS + "sourceModelFor" );
    
    /** <p>destinationModelFor</p> */
    public static final ObjectProperty destinationModelFor = m_model.createObjectProperty( NS + "destinationModelFor" );
    
    /** <p>splitRegexOf</p> */
    public static final ObjectProperty splitRegexOf = m_model.createObjectProperty( NS + "splitRegexOf" );
    
    /** <p>trimValueFor</p> */
    public static final ObjectProperty trimValueFor = m_model.createObjectProperty( NS + "trimValueFor" );
    
    /** <p>uriPrefix</p> */
    public static final ObjectProperty uriPrefix = m_model.createObjectProperty( NS + "uriPrefix" );
    
    /** <p>action</p> */
    public static final ObjectProperty action = m_model.createObjectProperty( NS + "action" );
    
    /** <p>destinationModel</p> */
    public static final ObjectProperty destinationModel = m_model.createObjectProperty( NS + "destinationModel" );
    
    /** <p>processorMethodOf</p> */
    public static final ObjectProperty processorMethodOf = m_model.createObjectProperty( NS + "processorMethodOf" );
    
    /** <p>sourceModel</p> */
    public static final ObjectProperty sourceModel = m_model.createObjectProperty( NS + "sourceModel" );
    
    /** <p>uriPrefixForAction</p> */
    public static final ObjectProperty uriPrefixForAction = m_model.createObjectProperty( NS + "uriPrefixForAction" );
    
    /** <p>smushOnProperty</p> */
    public static final ObjectProperty smushOnProperty = m_model.createObjectProperty( NS + "smushOnProperty" );

    /** <p>sparqlQuery</p> */
    public static final ObjectProperty sparqlQuery = m_model.createObjectProperty( NS + "sparqlQuery" );
    
    /** <p>previousStep</p> */
    public static final ObjectProperty previousStep = m_model.createObjectProperty( NS + "previousStep" );
    
    /** <p>addedInAction</p> */
    public static final ObjectProperty addedInAction = m_model.createObjectProperty( NS + "addedInAction" );
    
    /** <p>subtractedInAction</p> */
    public static final ObjectProperty subtractedInAction = m_model.createObjectProperty( NS + "subtractedInAction" );
    
    /** <p>nextStep</p> */
    public static final ObjectProperty nextStep = m_model.createObjectProperty( NS + "nextStep" );
    
    /** <p>trim</p> */
    public static final ObjectProperty trim = m_model.createObjectProperty( NS + "trim" );
    
    /** <p>additionsModelOf</p> */
    public static final ObjectProperty additionsModelOf = m_model.createObjectProperty( NS + "additionsModelOf" );
    
    /** <p>applyChangesDirectlyToSource</p> */
    public static final DatatypeProperty applyChangesDirectlyToSource = m_model.createDatatypeProperty( NS + "applyChangesDirectlyToSource" );
    
    /** <p>jenaConnectConfig</p> */
    public static final DatatypeProperty jenaConnectConfig = m_model.createDatatypeProperty( NS + "jenaConnectConfig" );
    
    /** <p>literalValue</p> */
    public static final DatatypeProperty literalValue = m_model.createDatatypeProperty( NS + "literalValue" );

    /** <p>variableName</p> */
    public static final DatatypeProperty variableName = m_model.createDatatypeProperty( NS + "variableName" ); 
    
    /** <p>Action</p> */
    public static final OntClass Action = m_model.createClass( NS + "Action" );
    
    /** <p>ProcessPropertyValueStringsAction</p> */
    public static final OntClass ProcessPropertyValueStringsAction = m_model.createClass( NS + "ProcessPropertyValueStringsAction" );
    
    /** <p>Workflow</p> */
    public static final OntClass Workflow = m_model.createClass( NS + "Workflow" );
    
    /** <p>WorkflowStep</p> */
    public static final OntClass WorkflowStep = m_model.createClass( NS + "WorkflowStep" );
    
    /** <p>Value</p> */
    public static final OntClass Value = m_model.createClass( NS + "Value" );
    
    /** <p>SPARQLCONSTRUCTAction</p> */
    public static final OntClass SPARQLCONSTRUCTAction = m_model.createClass( NS + "SPARQLCONSTRUCTAction" );
    
    /** <p>Model</p> */
    public static final OntClass Model = m_model.createClass( NS + "Model" );
    
    /** <p>Literal</p> */
    public static final OntClass Literal = m_model.createClass( NS + "Literal" );
    
    /** <p>CreateModelAction</p> */
    public static final OntClass CreateModelAction = m_model.createClass( NS + "CreateModelAction" );
    
    /** <p>AddModelAction</p> */
    public static final OntClass AddModelAction = m_model.createClass( NS + "AddModelAction" );
    
    /** <p>SubtractModelAction</p> */
    public static final OntClass SubtractModelAction = m_model.createClass( NS + "SubtractModelAction" );
    
    /** <p>SplitPropertyValuesAction</p> */
    public static final OntClass SplitPropertyValuesAction = m_model.createClass( NS + "SplitPropertyValuesAction" );
    
    /** <p>NameBlankNodesAction</p> */
    public static final OntClass NameBlankNodesAction = m_model.createClass( NS + "NameBlankNodesAction" );
    
    /** <p>Variable</p> */
    public static final OntClass Variable = m_model.createClass( NS + "Variable" );
    
    /** <p>ClearModelAction</p> */
    public static final OntClass ClearModelAction = m_model.createClass( NS + "ClearModelAction" );
    
    /** <p>PropertyValueProcessingAction</p> */
    public static final OntClass PropertyValueProcessingAction = m_model.createClass( NS + "PropertyValueProcessingAction" );
    
    /** <p>SmushResourcesAction</p> */
    public static final OntClass SmushResourcesAction = m_model.createClass( NS + "SmushResourcesAction" );
    
}
