/*******************************************************************************
 * Copyright (c) 2010-2011 VIVO Harvester Team. For full list of contributors, please see the AUTHORS file provided.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 ******************************************************************************/
package org.vivoweb.harvester.util;
 

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.args.ArgDef;
import org.vivoweb.harvester.util.args.ArgList;
import org.vivoweb.harvester.util.args.ArgParser;
import org.vivoweb.harvester.util.args.UsageException;
import org.vivoweb.harvester.util.repo.JenaConnect;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
 

/**
 * Merge multiple rdf files into one
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class MergeUriTool {
	/**
	 * SLF4J Logger
	 */
	protected static Logger log = LoggerFactory.getLogger(MergeUriTool.class);
	/**
	 * PrimaryUri 
	 */
	private String primaryUri;
	/**
	 * Duplicate Uri
	 */
	private String duplicateUri;
	/**
	 * The jena model
	 */
	private JenaConnect model;
	
	public static final String TBOX_UNION = "vitro:tboxOntModel";

	public static final String FULL_ASSERTIONS = "vitro:baseOntModel";
	 
	
	/**
	 * Constructor
	 * @param input input recordhandler
	 * @param output output recordhandler
	 * @param regex regex for finding primary records (with a grouping for the subsection to use to find sub-records)
	 */
	public MergeUriTool(JenaConnect model, String primaryUri, String duplicateUri) {
		this.model = model;
		if (this.model == null) {
			throw new IllegalArgumentException("No input model provided! Must provide an input model");
		}
		this.primaryUri = primaryUri;
		if (this.primaryUri == null) {
			throw new IllegalArgumentException("Must provide a primaryUri");
		}
		this.duplicateUri = duplicateUri;
		if (this.duplicateUri == null) {
			throw new IllegalArgumentException("Must provide an duplicateUri recordhandler");
		} 
	}
	
	/**
	 * Constructor
	 * @param args commandline arguments
	 * @throws IOException error creating task
	 * @throws UsageException user requested usage message
	 */
	public MergeUriTool(String[] args) throws IOException, UsageException {
		this(getParser().parse(args));
	}
	
	/**
	 * Constructor
	 * @param argList arguments
	 * @throws IOException error connecting to record handler
	 */
	public MergeUriTool(ArgList argList) throws IOException {
		this (
			JenaConnect.parseConfig(argList.get("m"), argList.getValueMap("M")),	
			argList.get("p"), // primaryUri
			argList.get("d")  // duplicateUri
		);
	}
	
	/**
	 * Runs the merge
	 * @throws IOException error executing
	 */
	public void execute() throws IOException {
		System.out.println("modelName: "+ model.getModelName()); 
		 
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model.getJenaModel());
		OntModel tboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, model.getJenaModel());
		
		MergeResult result = doMerge(this.primaryUri, this.duplicateUri, ontModel, tboxOntModel, true);
		System.out.println(result.getResultText());
		Model lmodel = result.getLeftoverModel();
		if (lmodel != null) { 
			try    {
	            //OutputStream outStream = System.out;
	            OutputStream outStream = new FileOutputStream("/tmp/sysout.txt");
	            outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
	            lmodel.write( outStream,"RDF/XML-ABBREV");
	            outStream.flush();
	            outStream.close();
	        }
	        catch(IOException ioe){
	            ioe.printStackTrace();
	        }
		}
		 
	}
	
	/**
	 * Get the ArgParser for this task
	 * @return the ArgParser
	 */
	private static ArgParser getParser() {
		ArgParser parser = new ArgParser("MergeUriTool");
		parser.addArgument(new ArgDef().setShortOption('m').setLongOpt("model").withParameter(true, "CONFIG_FILE").setDescription("config file for jena model to load into output model").setRequired(false));
		parser.addArgument(new ArgDef().setShortOption('M').setLongOpt("modelOverride").withParameterValueMap("JENA_PARAM", "VALUE").setDescription("override the JENA_PARAM of jena model config using VALUE").setRequired(false));
		// Inputs
		parser.addArgument(new ArgDef().setShortOption('p').setLongOpt("primaryUri").withParameter(true, "PRIMARYURI").setDescription("primaryUri").setRequired(true));
		parser.addArgument(new ArgDef().setShortOption('d').setLongOpt("duplicateUri").withParameter(true, "DUPLICATEURI").setDescription("duplicateUri").setRequired(true)); 
		 
		return parser;
	}
	
	/**
	 * Main method
	 * @param args commandline arguments
	 */
	public static void main(String... args) {
		Exception error = null;
		try {
			InitLog.initLogger(args, getParser());
			log.info(getParser().getAppName() + ": Start");
			new MergeUriTool(args).execute();
		} catch(IllegalArgumentException e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			System.out.println(getParser().getUsage());
			error = e;
		} catch(UsageException e) {
			log.info("Printing Usage:");
			System.out.println(getParser().getUsage());
			error = e;
		} catch(Exception e) {
			log.error(e.getMessage());
			log.debug("Stacktrace:",e);
			error = e;
		} finally {
			log.info(getParser().getAppName() + ": End");
			if(error != null) {
				System.exit(1);
			}
		}
	}
	
	/**
     * Merges statements about resource uri2 into resource uri1 and delete uri2.
     * @param uri1 The resource to merge to
     * @param uri2 The resource to merge from
     * @param baseOntModel The model containing the relevant statements
     * @param tboxOntModel The model containing class and property data
     * @param usePrimaryLabelOnly If true, discard rdfs:labels from uri2.  Otherwise retain.
     * @return
     */
    public MergeResult doMerge(String uri1, String uri2,  OntModel baseOntModel,
            OntModel tboxOntModel, boolean usePrimaryLabelOnly){

        boolean functionalPresent = false;

        Resource res1 = baseOntModel.getResource(uri1); // primary resource
        if (res1 == null) {
        	return new MergeResult("primary uri not found: "+ uri1, null);	
        }
        Model res1Model = ModelFactory.createDefaultModel();        
        Resource res2 = baseOntModel.getResource(uri1); // secondary resource
        if (res2 == null) {
        	return new MergeResult("duplicate uri not found: "+ uri2, null);	
        }
        Model res2Model = ModelFactory.createDefaultModel();

        // get statements of both the resources
        baseOntModel.enterCriticalSection(Lock.READ);
        try {        
            res1Model.add(baseOntModel.listStatements(res1, (Property) null, (RDFNode) null));
            res2Model.add(baseOntModel.listStatements(res2, (Property) null, (RDFNode) null));
            res2Model.add(baseOntModel.listStatements((Resource)null, (Property)null, (RDFNode)res2));
        } finally {
            baseOntModel.leaveCriticalSection();
        }

        // if primary resource has no statements, return
        if (res1Model.isEmpty()){
            return new MergeResult("resource 1 not present", null);
        } else if(res2Model.isEmpty()){
            return new MergeResult("resource 2 not present", null);
        }

        int counter = 0;
        Model leftoverModel = ModelFactory.createDefaultModel();

        // Iterate through statements of secondary resource
        StmtIterator stmtItr2 = res2Model.listStatements(
                res2, (Property) null, (RDFNode) null);            
        while (stmtItr2.hasNext()) {
            Statement stmt = stmtItr2.nextStatement();            
            if (isFunctional(stmt.getPredicate(), tboxOntModel)) {
                // if the property is null or functional then dump the statement into 
                // the leftover model, else add it to base, ont and inf models as a 
                // part of the primary resource.
                leftoverModel.add(res2, stmt.getPredicate(), stmt.getObject());
                functionalPresent = true;
            } else if (stmt.getPredicate().equals(RDFS.label) && usePrimaryLabelOnly) {
                // if the checkbox is checked, use primary resource rdfs:labels only  
                // and dump secondary resource rdfs:labels into leftoverModel
                leftoverModel.add(res2, stmt.getPredicate(), stmt.getObject());
                functionalPresent = true;        
            } else {
                baseOntModel.enterCriticalSection(Lock.WRITE);
                try {
                    baseOntModel.add(res1, stmt.getPredicate(), stmt.getObject());
                    counter++;
                } finally {
                    baseOntModel.leaveCriticalSection();
                }
            }
        }

        // replace secondary resource with primary resource in all the statements 
        // where secondary resource is present as an object.
        StmtIterator stmtItr3 = res2Model.listStatements(
                (Resource) null, (Property) null, res2);
        while (stmtItr3.hasNext()){
            Statement stmt = stmtItr3.nextStatement();
            Resource sRes = stmt.getSubject();
            Property sProp = stmt.getPredicate();
            baseOntModel.enterCriticalSection(Lock.WRITE);
            try {
                baseOntModel.add(sRes, sProp, res1);
                counter++;
            } finally {
                baseOntModel.leaveCriticalSection();
            }
        }

        // Remove all the statements of secondary resource 
        baseOntModel.enterCriticalSection(Lock.WRITE);
        try {
            baseOntModel.remove(res2Model);
        } finally {
            baseOntModel.leaveCriticalSection();
        }

        MergeResult result = new MergeResult();
        if (!leftoverModel.isEmpty()) {
            result.setLeftoverModel(leftoverModel);   
        }

        if (counter > 0 && functionalPresent) {
            result.setResultText("merged " + counter + 
                    " statements. Some statements could not be merged.");
        } else if(counter>0 && !functionalPresent) {
            result.setResultText("merged " + counter + " statements.");    
        } else if (counter==0) {
            result.setResultText("No statements merged");
        }
        return result;

    }
    
    private boolean isFunctional(Property property, OntModel tboxOntModel) {
        tboxOntModel.enterCriticalSection(Lock.READ);
        try {
            return (tboxOntModel.contains(
                    property, RDF.type, OWL.FunctionalProperty));
        } finally {
            tboxOntModel.leaveCriticalSection();
        }
    }
    
    public OntModel generateTBox(Model abox) {
        OntModel tboxOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        StmtIterator sit = abox.listStatements();
        while (sit.hasNext()) {
            Statement stmt = sit.nextStatement();
            if (RDF.type.equals(stmt.getPredicate())) {
                makeClass(stmt.getObject(), tboxOntModel);
            } else if (stmt.getObject().isResource()) {
                makeObjectProperty(stmt.getPredicate(), tboxOntModel);
            } else if (stmt.getObject().isLiteral()) {
                makeDatatypeProperty(stmt.getPredicate(), tboxOntModel);
            }
        }
        return tboxOntModel;
    }
    
    private void makeClass(RDFNode node, OntModel tboxOntModel) {
        if (!node.isResource() || node.isAnon()) {
            return;
        }
        Resource typeRes = (Resource) node;
        if (tboxOntModel.getOntClass(typeRes.getURI()) == null) {
            tboxOntModel.createClass(typeRes.getURI());
        }
    }

    private void makeObjectProperty(Property property, OntModel tboxOntModel) {
        if (tboxOntModel.getObjectProperty(property.getURI()) == null) {
            tboxOntModel.createObjectProperty(property.getURI());
        }
    }

    private void makeDatatypeProperty(Property property, OntModel tboxOntModel) {
        if (tboxOntModel.getDatatypeProperty(property.getURI()) == null) {
            tboxOntModel.createDatatypeProperty(property.getURI());
        }
    }
	
	
	
	public class MergeResult {
        private String resultText;
        private Model leftoverModel;

        public MergeResult() {}

        public MergeResult(String resultText, Model leftoverModel) {
            this.resultText = resultText;
            this.leftoverModel = leftoverModel;
        }

        public void setResultText(String resultText) {
            this.resultText = resultText;
        }

        public String getResultText() {
            return this.resultText;
        }

        public void setLeftoverModel(Model leftoverModel) {
            this.leftoverModel = leftoverModel;
        }

        public Model getLeftoverModel() {
            return this.leftoverModel;
        }    
    }
}
