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
package org.vivoweb.harvester.qualify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * Find nodes with no name and give them a name
 * @author Michael Barbieri (mbarbier@ufl.edu)
 */
public class RenameBlankNodes {
	
	/**
	 * Rename blank nodes
	 * @param inJC The model to perform rename in
	 * @param outJC The model to write output to
	 * @param namespaceEtc The part of the namespace between the base and the ID number
	 * @param dedupJC deduplication test model
	 * @param pattern pattern
	 * @param property property
	 */
	public static void renameBNodes(JenaConnect inJC, JenaConnect outJC, String namespaceEtc, JenaConnect dedupJC, String pattern, String property) {
		if(inJC == null) {
			throw new IllegalArgumentException("Must provide an input jena model");
		}
		if(outJC == null) {
			throw new IllegalArgumentException("Must provide an output jena model");
		}
		Model inModel = inJC.getJenaModel();
		Model outModel = outJC.getJenaModel();
		Model dedupModel = dedupJC.getJenaModel();
		Property propertyRes = ResourceFactory.createProperty(property);
		OntModel dedupUnionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); // we're not using OWL here, just the OntModel submodel infrastructure
		dedupUnionModel.addSubModel(outModel);
		if (dedupModel != null) {
			dedupUnionModel.addSubModel(dedupModel);
		}
		// the dedupUnionModel is so we can guard against reusing a URI in an 
		// existing model, as well as in the course of running this process
		inModel.enterCriticalSection(Lock.READ);
		Set<String> doneSet = new HashSet<String>();
		
		try {
			outJC.loadRdfFromJC(inJC);
			ClosableIterator<Resource> closeIt = inModel.listSubjects();
			try {
				for (Iterator<Resource> it = closeIt; it.hasNext();) {
					Resource res = it.next();
					if (res.isAnon() && !(doneSet.contains(res.getId()))) {
						// now we do something hacky to get the same resource in the outModel, since there's no getResourceById();
						ClosableIterator<Statement> closfIt = outModel.listStatements(res,propertyRes,(RDFNode)null);
						Statement stmt = null;
						try {
							if (closfIt.hasNext()) {
								stmt = closfIt.next();
							}
						} finally {
							closfIt.close();
						}
						if (stmt != null) {
							Resource outRes = stmt.getSubject();
							if(stmt.getObject().isLiteral()){
								ResourceUtils.renameResource(outRes,namespaceEtc+pattern+"_"+stmt.getObject().toString());
							}
							doneSet.add(res.getId().toString());
						}
					}
				}
			} finally {
				closeIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
		outJC.sync();
	}
}
