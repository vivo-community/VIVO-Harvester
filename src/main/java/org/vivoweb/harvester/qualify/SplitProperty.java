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

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.jenaconnect.JenaConnect;
import org.vivoweb.harvester.util.jenaconnect.MemJenaConnect;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

/**
 * Changes the namespace for all matching uris
 * @author Cornell University VIVO Team (Algorithm)
 * @author Christopher Haines (hainesc@ctrip.ufl.edu) (Harvester Tool)
 */
public class SplitProperty {
	/**
	 * SLF4J Logger
	 */
	private static Logger log = LoggerFactory.getLogger(SplitProperty.class);
	/**
	 * model to split property values in
	 */
	private JenaConnect model;
	/**
	 * model to store new property statements in
	 */
	private JenaConnect outModel;
	
	/**
	 * Constructor
	 * @param model model to split property values in
	 */
	public SplitProperty(JenaConnect model) {
		if(model == null) {
			throw new IllegalArgumentException("No model provided! Must provide a model");
		}
		this.model = model;
		this.outModel = new MemJenaConnect();
	}
	
	/**
	 * Get a JenaConnect containing the new property statements
	 * @return the new property statements in a JenaConnect
	 */
	public JenaConnect getNewPropertyStatements() {
		return this.outModel;
	}
	
	/**
	 * Splits values for a given data property URI on a supplied regex and 
	 * asserts each value using newPropertyURI.  New statements returned in
	 * a Jena Model.  Split values may be optionally trim()'ed.
	 * @param splitRegex regex to split oldPropertyURI value on
	 * @param oldPropertyURI old property uri (to be split)
	 * @param newPropertyURI new property uri (to store split values in)
	 * @param trim trim() the new values
	 */
	public void splitPropertyValues(String oldPropertyURI, String splitRegex, String newPropertyURI, boolean trim) {
		Pattern delimiterPattern = Pattern.compile(splitRegex);
		Property theProp = ResourceFactory.createProperty(oldPropertyURI);
		Property newProp = ResourceFactory.createProperty(newPropertyURI);
		log.info("Spliting <"+theProp+"> on '"+splitRegex+"' into <"+newProp+"> properties");
		this.model.getJenaModel().enterCriticalSection(Lock.READ);
		try {
			StmtIterator stmtIt = this.model.getJenaModel().listStatements((Resource)null, theProp, (RDFNode)null);
			try {
				while(stmtIt.hasNext()) {
					Statement stmt = stmtIt.nextStatement();
					Resource subj = stmt.getSubject();
					RDFNode obj = stmt.getObject();
					if(obj.isLiteral()) {
						Literal lit = obj.asLiteral();
						String unsplitStr = lit.getLexicalForm();
						log.trace("found: "+unsplitStr);
						String[] splitPieces = delimiterPattern.split(unsplitStr);
						for(String newLexicalForm : splitPieces) {
							if(trim) {
								newLexicalForm = newLexicalForm.trim();
							}
							if(newLexicalForm.length() > 0) {
								log.trace("adding: "+newLexicalForm);
								Literal newLiteral = null;
								if(lit.getDatatype() != null) {
									newLiteral = this.outModel.getJenaModel().createTypedLiteral(newLexicalForm, lit.getDatatype());
								} else {
									if (lit.getLanguage() != null) {
										newLiteral = this.outModel.getJenaModel().createLiteral(newLexicalForm, lit.getLanguage());
									} else {
										newLiteral = this.outModel.getJenaModel().createLiteral(newLexicalForm);
									}
								}
								this.outModel.getJenaModel().add(subj,newProp,newLiteral);
							}
						}
					}
				}
			} finally {
				stmtIt.close();
			}
		} finally {
			this.model.getJenaModel().leaveCriticalSection();
		}
	}
}
