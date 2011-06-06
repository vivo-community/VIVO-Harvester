/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/

//
//package org.vivoweb.harvester.score.algorithm;
//
//
///**
// * Add co-author 
// * @author Nicholas Skaggs nskaggs@ctrip.ufl.edu
// */
//public class CoAuthor implements Algorithm {
//
//	@Override
//	public float calculate(x,y) {
//			//Pass in 
////			PREFIX foaf: <http://xmlns.com/foaf/0.1/> 
////			PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> 
////			PREFIX core: <http://vivoweb.org/ontology/core#> 
////			PREFIX bibo: <http://purl.org/ontology/bibo/> 
////			PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> 
////			PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> 
////			 
////			 
////			SELECT  DISTINCT  ?coAuthorPerson ?coAuthorPersonLabel ?document
////			 
////			WHERE { 
////				<http://vivo.ufl.edu/individual/n14560> rdf:type foaf:Person .
////				<http://vivo.ufl.edu/individual/n14560> core:authorInAuthorship ?authorshipNode . 
////				?authorshipNode rdf:type core:Authorship .   
////				?authorshipNode core:linkedInformationResource ?document . 
////				?document core:informationResourceInAuthorship ?coAuthorshipNode .  
////				?coAuthorshipNode core:linkedAuthor ?coAuthorPerson .  
////				?coAuthorPerson rdfs:label ?coAuthorPersonLabel . 
////			}  
////			ORDER BY ?document ?coAuthorPerson
//	}
//	
//}

