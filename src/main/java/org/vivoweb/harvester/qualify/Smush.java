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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.MemJenaConnect;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

/**
 * Smush
 * @author Cornell University VIVO Team (Algorithm)
 * @author James Pence (jrpence@ufl.edu) (Harvester Tool)
 */
public class Smush {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(Smush.class);
	/**
	 * model containing statements to be scored
	 */
	private final JenaConnect inputJC;
	/**
	 * model to hold subtractions
	 */
	private final JenaConnect subsJC;
	/**
	 * model to hold additions
	 */
	private final JenaConnect addsJC;
	
	/**
	 * Constructor
	 * @param inputJC model containing statements to be smushed
	 */
	public Smush(JenaConnect inputJC) {
		if(inputJC == null) {
			throw new IllegalArgumentException("Input model cannot be null");
		}
		this.inputJC = inputJC;
		
		this.addsJC = new MemJenaConnect();
		this.subsJC = new MemJenaConnect();
	}
	
	/**
	 * Get the additions JenaConnect
	 * @return the additions JenaConnect
	 */
	public JenaConnect getAdditionsJC() {
		return this.addsJC;
	}
	
	/**
	 * Get the subtractions JenaConnect
	 * @return the subtractions JenaConnect
	 */
	public JenaConnect getSubtractionsJC() {
		return this.subsJC;
	}
	
	/**
	 * A simple resource smusher based on a supplied inverse-functional property.
	 * @param property - property for smush
	 * @param namespace - filter on resources addressed (if null then applied to whole model)
	 */
	public void findSmushResourceChanges(String property, String namespace) {
		log.debug("Smushing on property <" + property + "> within "+((namespace != null )?"namespace <"+ namespace + ">":"any namespace"));
		Model inModel = this.inputJC.getJenaModel();
		Model subsModel = this.subsJC.getJenaModel();
		Model addsModel = this.addsJC.getJenaModel();
		Property prop = inModel.createProperty(property);
		inModel.enterCriticalSection(Lock.READ);
		try {
			NodeIterator objIt = inModel.listObjectsOfProperty(prop);
			try {
				while(objIt.hasNext()) {
					RDFNode obj = objIt.next();
					ResIterator subjIt = inModel.listSubjectsWithProperty(prop, obj);
					try {
						boolean first = true;
						Resource smushToThisResource = null;
						while (subjIt.hasNext()) {
							Resource subj = subjIt.next();
							if(subj.getNameSpace().equals(namespace) || namespace == null){
								if (first) {
									smushToThisResource = subj;
									first = false;
									log.debug("Smush running for <"+subj+">");
								} else {
									log.trace("Smushing <"+subj+"> into <"+smushToThisResource+">");
									StmtIterator stmtIt = inModel.listStatements(subj,(Property)null,(RDFNode)null);
									try {
										while(stmtIt.hasNext()) {
											Statement stmt = stmtIt.next();
											log.trace("Changing <"+stmt.getPredicate()+"> <"+stmt.getObject()+"> from <"+stmt.getSubject()+"> to <"+smushToThisResource+">");
											subsModel.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
											addsModel.add(smushToThisResource, stmt.getPredicate(), stmt.getObject());
										}
									} finally {
										stmtIt.close();
									}
									stmtIt = inModel.listStatements((Resource) null, (Property)null, subj);
									try {
										while(stmtIt.hasNext()) {
											Statement stmt = stmtIt.next();
											log.trace("Changing <"+stmt.getSubject()+"> <"+stmt.getPredicate()+"> from <"+stmt.getObject()+"> to <"+smushToThisResource+">");
											subsModel.add(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
											addsModel.add(stmt.getSubject(), stmt.getPredicate(), smushToThisResource);
										}
									} finally {
										stmtIt.close();
									}
								}
							}
						}
					} finally {
						subjIt.close();
					}
				}
			} finally {
				objIt.close();
			}
		} finally {
			inModel.leaveCriticalSection();
		}
	}
}
