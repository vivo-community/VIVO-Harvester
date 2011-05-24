/******************************************************************************************************************************
 * Harvester Tool Copyright (c) 2011 Christopher Haines, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 ******************************************************************************************************************************
 * Algorithm Copyright (c) 2011, Cornell University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of Cornell University nor the names of its contributors
 *       may be used to endorse or promote products derived from this software 
 *       without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util.workflow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

/**
 * Process an ingest workflow
 * @author Cornell University VIVO Team
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class JenaIngestWorkflowProcessor {
	/**
	 * SLF4J Logger
	 */
	static Logger log = LoggerFactory.getLogger(JenaIngestWorkflowProcessor.class);
	/**
	 * 
	 */
	private ModelMaker vitroJenaModelMaker;
	/**
	 * the individual that defines the workflow
	 */
	private Individual workflowInd;
	/**
	 * local variable value map
	 */
	private Map<String,Literal> varMap;
	/**
	 * list of defined action handlers
	 */
	private List<ActionHandler> actionHandlerList;
	
	/**
	 * Constructor
	 * @param workflowInd the individual that defines the workflow
	 */
	@SuppressWarnings("synthetic-access")
	public JenaIngestWorkflowProcessor(Individual workflowInd) {
		this.varMap = new HashMap<String,Literal>();
		this.workflowInd = workflowInd;
		this.actionHandlerList = new LinkedList<ActionHandler>();
		this.actionHandlerList.add(new ClearModelAction());
		this.actionHandlerList.add(new AddModelsAction());
		this.actionHandlerList.add(new SubtractModelsAction());
		this.actionHandlerList.add(new ExecuteSparqlConstructAction());
		this.actionHandlerList.add(new SplitPropertyValuesAction());
		this.actionHandlerList.add(new ProcessPropertyValueStringsAction());
		this.actionHandlerList.add(new SmushResourcesAction());
		this.actionHandlerList.add(new NameBlankNodesAction());
	}
	
	/**
	 * Runs the workflow
	 */
	public void run() {
		run(null);
	}
	
	/**
	 * Runs the workflow
	 * @param startingWorkflowStep step of worflow to start on (null = beginning)
	 */
	public void run(Individual startingWorkflowStep) {
		for (Individual step : getWorkflowSteps(startingWorkflowStep)) {
			Individual action = getAction(step);
			log.debug("Executing workflow action "+action.getURI());
			for (ActionHandler handler : this.actionHandlerList) {
				ActionResult result = handler.handleAction(action);
				if (result != null) {
					break;
				}
			}
		}
	}
	
	/**
	 * Get the supplied steps action
	 * @param stepInd the step
	 * @return the Action related to the supplied WorkflowStep
	 */
	private Individual getAction(Individual stepInd) {
		log.debug("Workflow step: "+stepInd.getURI());
		RDFNode actionNode = stepInd.getPropertyValue(WorkflowOntology.action);
		if (actionNode != null && actionNode.canAs(Individual.class)) {
			return actionNode.as(Individual.class);
		}
		return null;
	}
	
	/**
	 * Get all the steps of this workflow starting at the given step
	 * @param startingWorkflowStep the step to start at
	 * @return the steps list
	 */
	public List<Individual> getWorkflowSteps(Individual startingWorkflowStep) {
		List<Individual> workflowSteps = new LinkedList<Individual>();
		Individual currentInd = (startingWorkflowStep == null) ?
			getWorkflowStep(this.workflowInd.getPropertyValue(WorkflowOntology.firstStep)) :
				startingWorkflowStep;
		while (currentInd != null) {
			workflowSteps.add(currentInd);
			currentInd = getWorkflowStep(currentInd.getPropertyValue(WorkflowOntology.nextStep));
		}
		return workflowSteps;
	}
	
	/**
	 * Get the indiviual that this node points to
	 * @param stepNode the node
	 * @return the individual
	 */
	private Individual getWorkflowStep(RDFNode stepNode) {
		if (stepNode == null) {
			return null;
		}
		if (stepNode.canAs(Individual.class)) {
			Individual nextStepInd = stepNode.as(Individual.class);
			if (instanceOf(nextStepInd,WorkflowOntology.WorkflowStep)) {
				return nextStepInd;
			} 
		}
		return null;
	}
	
	/**
	 * Is this individual an instance of the given type
	 * @param ind the individual
	 * @param type the type
	 * @return true if the individual has that uri
	 */
	boolean instanceOf(Individual ind, Resource type) {
		for ( Resource typeRes : ind.listRDFTypes(false).toList() ) {
			if (!typeRes.isAnon() && typeRes.getURI().equals(type.getURI())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * gets the appropriate Jena Literal for a Value individual in the model,
	 * depending on whether the Value is a Variable or a Literal
	 * At some point 
	 * @param valueIndNode the node to get value for
	 * @return the value
	 */
	Literal getValue(RDFNode valueIndNode) {
		Individual valueInd = valueIndNode.as(Individual.class);
		if (instanceOf(valueInd,WorkflowOntology.Literal)) {
			RDFNode valueNode = valueInd.getPropertyValue(WorkflowOntology.literalValue);
			if ( (valueNode != null) && (valueNode.isLiteral()) ) {
				return valueNode.as(Literal.class);
			}
		} else if (instanceOf(valueInd,WorkflowOntology.Variable)) {
			RDFNode variableNameNode = valueInd.getPropertyValue(WorkflowOntology.variableName);
			if ( (variableNameNode != null) && (variableNameNode.isLiteral())) {
				return this.varMap.get( (variableNameNode.as(Literal.class)).getLexicalForm() );
			}
		}
		return null;
	}
	
	/**
	 * Get the model this node represents
	 * @param modelNode the node reoresentign a model
	 * @return the model represented by the given Node, which is expected to be an Individual of type Model
	 */
	Model getJenaConnect(RDFNode modelNode) {
	    if (modelNode == null) {// || modelNode.canAs(WorkflowOntology.Model)) {
	        return null;
	    }
		Individual modelInd = modelNode.as(Individual.class);
		String modelNameStr = (modelInd.getPropertyValue(WorkflowOntology.jenaConnectConfig).as(Literal.class)).getLexicalForm();
		// false = strict mode off, i.e., 
		// if a model already exists of the given name, return it.  Otherwise, create a new one.
		return this.vitroJenaModelMaker.createModel(modelNameStr,false);
	}
	
	/**
	 * ActionResult interface
	 */
	private interface ActionResult {
		//Nothing needed
	}
	
	/**
	 * Null implementation of ActionResult
	 */
	private class ActionResultImpl implements ActionResult {
		
		/**
		 * Default Constructor
		 */
		public ActionResultImpl() {
			// Nothing needed
		}
	}
	
	/**
	 * ActionHandler interface
	 */
	private interface ActionHandler {
		/**
		 * Handle the action represented by the given individual
		 * @param actionInd the individual
		 * @return the result of the action
		 */
		public ActionResult handleAction(Individual actionInd);
	}
	
	// ALL THE DIFFERENT ACTION HANDLERS
	
	/**
	 * Clear a model
	 */
	private class ClearModelAction implements ActionHandler { 
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.ClearModelAction)) {
				Model sourceModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.sourceModel)); 
				sourceModel.enterCriticalSection(Lock.WRITE);
				try{
					// this method is used so that any listeners can see each statement removed
					sourceModel.removeAll((Resource)null,(Property)null,(RDFNode)null);
				} finally {
					sourceModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;		
		}
	}
	
	/**
	 * Add one model to another
	 */
	private class AddModelsAction implements ActionHandler { 
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.AddModelAction)) {
				Model sourceModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.sourceModel)); 
				Model modelToAdd = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.modelToAdd));
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel)); 
				Boolean applyChangesDirectlyToSource = Boolean.FALSE;
				RDFNode valueNode = actionInd.getPropertyValue(WorkflowOntology.applyChangesDirectlyToSource);
				if ((valueNode != null) && (valueNode.isLiteral())) {
				    applyChangesDirectlyToSource = Boolean.valueOf((valueNode.as(Literal.class)).getBoolean());
				}

				sourceModel.enterCriticalSection(Lock.WRITE);
				try {
					modelToAdd.enterCriticalSection(Lock.READ);
					try {
					    if (applyChangesDirectlyToSource.booleanValue()) {
					        // TODO: are all listeners notified this way?
					        sourceModel.add(modelToAdd);
					    } else {
    						destinationModel.enterCriticalSection(Lock.WRITE);
    						try{
    							destinationModel.add(modelToAdd);
    						} finally {
    							destinationModel.leaveCriticalSection();
    						}
					    }
					} finally {
						modelToAdd.leaveCriticalSection();
					}
				} finally {
					sourceModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;		
		}
	}
	
	/**
	 * Subtract one model from another
	 */
	private class SubtractModelsAction implements ActionHandler { 
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SubtractModelAction)) {
				Model sourceModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.sourceModel)); 
				Model modelToSubtract = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.modelToSubtract));
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel)); 
				Boolean applyChangesDirectlyToSource = Boolean.FALSE;
				RDFNode valueNode = actionInd.getPropertyValue(WorkflowOntology.applyChangesDirectlyToSource);
				if ((valueNode != null) && (valueNode.isLiteral())) {
				    applyChangesDirectlyToSource = Boolean.valueOf((valueNode.as(Literal.class)).getBoolean());
				}
				sourceModel.enterCriticalSection(Lock.WRITE);
				try {
					modelToSubtract.enterCriticalSection(Lock.READ);
					try {
    					if (applyChangesDirectlyToSource.booleanValue()) {
                            // TODO: are all listeners notified this way?
    					    sourceModel.remove(modelToSubtract);
    					} else {
    						destinationModel.enterCriticalSection(Lock.WRITE);
    						try{
    							destinationModel.add(sourceModel.difference(modelToSubtract));
    						} finally {
    							destinationModel.leaveCriticalSection();
    						}
    					}
					} finally {
						modelToSubtract.leaveCriticalSection();
					}
				} finally {
					sourceModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;		
		}
	}
	
	/**
	 * Execute a SPARQL Construct
	 */
	private class ExecuteSparqlConstructAction implements ActionHandler {
		
		/**
		 * Query String Property
		 */
		private static final String QUERY_STR_PROPERTY = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#queryStr";
			
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SPARQLCONSTRUCTAction)) {
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					log.debug("SPARQL: adding submodel ");
					sourceModel.addSubModel(getJenaConnect(node));
				}
				if (actionInd.getPropertyValue(WorkflowOntology.destinationModel) == null) {
				    log.debug("Error: destination model for SPARQL Construct action not specified for this action");
				    return null;
				}
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				Model tempModel = ModelFactory.createDefaultModel();
				OntResource sparqlQuery = (OntResource) actionInd.getPropertyValue(WorkflowOntology.sparqlQuery);
				String queryStr = ((Literal)sparqlQuery.getPropertyValue(ResourceFactory.createProperty(QUERY_STR_PROPERTY))).getLexicalForm();
				log.debug("SPARQL query: \n" + queryStr);
				Query query = QueryFactory.create(queryStr,Syntax.syntaxARQ);
		        QueryExecution qexec = QueryExecutionFactory.create(query,sourceModel);
		        qexec.execConstruct(tempModel);
		        destinationModel.add(tempModel);
				return new ActionResultImpl();
			}
			return null;
		}
	}
	
	/**
	 * Smush Resources based on a given property
	 */
	private class SmushResourcesAction implements ActionHandler {
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SmushResourcesAction)) {
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getJenaConnect(node));
				}
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				@SuppressWarnings("unused")
				String smushPropertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.smushOnProperty)).getLexicalForm();
				destinationModel.enterCriticalSection(Lock.WRITE);
				try {
//					destinationModel.add(Smush.smushResources(sourceModel, ResourceFactory.createProperty(smushPropertyURI), null));
					//FIXME cah: fix smushResources() call
				} finally {
					destinationModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;
		}
	}
	
	/**
	 * Name blank nodes
	 */
	private class NameBlankNodesAction implements ActionHandler {
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.NameBlankNodesAction)) {
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getJenaConnect(node));
				}
				@SuppressWarnings("unused")
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				@SuppressWarnings("unused")
				String uriPrefix = getValue(actionInd.getPropertyValue(WorkflowOntology.uriPrefix)).getLexicalForm();
//				destinationModel.add(RenameBlankNodes.renameBNodes(sourceModel, uriPrefix));
				//FIXME cah: fix renameBNodes() call
				return new ActionResultImpl();
			}
			return null;
		}
	}
	
	/**
	 * Split Property Values
	 */
	private class SplitPropertyValuesAction implements ActionHandler { 
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SplitPropertyValuesAction)) {
				// We use an OntModel here because this API supports submodels
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getJenaConnect(node));
				}
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				@SuppressWarnings("unused")
				String propertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.originalProperty)).getLexicalForm();
				@SuppressWarnings("unused")
				String newPropertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.newProperty)).getLexicalForm();
				@SuppressWarnings("unused")
				String splitRegex = getValue(actionInd.getPropertyValue(WorkflowOntology.splitRegex)).getLexicalForm();
				@SuppressWarnings("unused")
				boolean trim = true;
				try {
					trim = getValue(actionInd.getPropertyValue(WorkflowOntology.trim)).getBoolean();
				} catch (Exception e) {
					//ignore
				}
				destinationModel.enterCriticalSection(Lock.WRITE);
				try {
//					destinationModel.add(SplitProperty.splitPropertyValues(sourceModel, propertyURI, splitRegex, newPropertyURI, trim));
					//FIXME cah: fix splitPropertyValues() call
				} finally {
					destinationModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;		
		}
	}	
	
	/**
	 * Process Property Value Strings
	 */
	private class ProcessPropertyValueStringsAction implements ActionHandler { 
		@Override
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.ProcessPropertyValueStringsAction)) {
				// We use an OntModel here because this API supports submodels
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getJenaConnect(node));
				}
				Model destinationModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				@SuppressWarnings("unused")
				Model additionsModel = null;
				try {
					additionsModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.additionsModel));
				} catch (Exception e) {
					//ignore
				}
				@SuppressWarnings("unused")
				Model retractionsModel = null;
				try {
					retractionsModel = getJenaConnect(actionInd.getPropertyValue(WorkflowOntology.retractionsModel));
				} catch (Exception e) {
					//ignore
				}
				@SuppressWarnings("unused")
				String processorClass = getValue(actionInd.getPropertyValue(WorkflowOntology.processorClass)).getLexicalForm();
				@SuppressWarnings("unused")
				String processorMethod = getValue(actionInd.getPropertyValue(WorkflowOntology.processorMethod)).getLexicalForm();
				@SuppressWarnings("unused")
				String propertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.originalProperty)).getLexicalForm();
				@SuppressWarnings("unused")
				String newPropertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.newProperty)).getLexicalForm();
				destinationModel.enterCriticalSection(Lock.WRITE);
				try {
				    if (log.isDebugEnabled()) {
				        log.debug("calling processPropertyValueStrings ...");
				    }
//					utils.processPropertyValueStrings(sourceModel, destinationModel, additionsModel, retractionsModel, processorClass, processorMethod, propertyURI, newPropertyURI);
				    //FIXME cah: fix processPropertyValueStrings() call
				} finally {
					destinationModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;		
		}
	}	
	
	
}
