package org.vivoweb.dspacevivo.model;

import org.apache.jena.vocabulary.XSD;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class TestCollectionTaxonomy {

	public static void main(String[] args) throws JsonProcessingException {
		/*
		 * Entity declaration
		 */
		Repository rep0 = new Repository();
		rep0.setId("123456789_0");
		rep0.setUri("http://localhost:8080/server/rdf/resource/123456789/59");
		Community c1 = new Community();
		Community c2 = new Community();
		Community c58 = new Community();
		Community c59 = new Community();
		Community c60 = new Community();
		Community c65 = new Community();
		c1.setId("123456789_1");
		c2.setId("123456789_2");
		c58.setId("123456789_58");
		c59.setId("123456789_59");
		c60.setId("123456789_60");
		c65.setId("123456789_65");
		c1.setUri("http://localhost:8080/server/rdf/resource/123456789/1");
		c2.setUri("http://localhost:8080/server/rdf/resource/123456789/2");
		c58.setUri("http://localhost:8080/server/rdf/resource/123456789/58");
		c59.setUri("http://localhost:8080/server/rdf/resource/123456789/59");
		c60.setUri("http://localhost:8080/server/rdf/resource/123456789/60");
		c65.setUri("http://localhost:8080/server/rdf/resource/123456789/65");
		Collection col61 = new Collection();
		Collection col62 = new Collection();
		Collection col63 = new Collection();
		Collection col64 = new Collection();
		col61.setId("123456789_61");
		col62.setId("123456789_62");
		col63.setId("123456789_63");
		col64.setId("123456789_64");
		col61.setUri("http://localhost:8080/server/rdf/resource/123456789/61");
		col62.setUri("http://localhost:8080/server/rdf/resource/123456789/62");
		col63.setUri("http://localhost:8080/server/rdf/resource/123456789/63");
		col64.setUri("http://localhost:8080/server/rdf/resource/123456789/64");
		Item item66 = new Item();
		Item item68 = new Item();
		item66.setId("123456789_66");
		item68.setId("123456789_68");
		item66.setUri("http://localhost:8080/server/rdf/resource/123456789/56");
		item68.setUri("http://localhost:8080/server/rdf/resource/123456789/68");
		StatementLiteral item66Title = new StatementLiteral();
		item66Title.setSubjectUri(item66.getUri());
		item66Title.setPredicateUri("dcterm:title");
		item66Title.setObjectLiteral("Test paper");
		item66Title.setLiteralType(XSD.xstring.getURI());
		StatementLiteral item68Title = new StatementLiteral();
		item68Title.setSubjectUri(item68.getUri());
		item68Title.setPredicateUri("dcterm:title");
		item68Title.setObjectLiteral("Test thesis");
		item68Title.setLiteralType(XSD.xstring.getURI());
		/*
		 * Link Entities
		 */
		rep0.addHasCommunityItem(c1);
		rep0.addHasCommunityItem(c2);
		rep0.addHasCommunityItem(c58);
		/*******
		 * <http://localhost:8080/server/rdf/resource/123456789/58>
		 * dspace:hasCollection       
		 *          <http://localhost:8080/server/rdf/resource/123456789/61> , 
		 *          <http://localhost:8080/server/rdf/resource/123456789/62> , 
		 *          <http://localhost:8080/server/rdf/resource/123456789/63> ,
		 *          <http://localhost:8080/server/rdf/resource/123456789/64> ;
		 * dspace:hasSubcommunity     
		 *          <http://localhost:8080/server/rdf/resource/123456789/60> , 
		 *          <http://localhost:8080/server/rdf/resource/123456789/59> ;
		 * dspace:isPartOfRepository  
		 *          <http://localhost:8080/server/rdf/resource/123456789/0> ;
		 */
		c58.addHasCollectionItem(col61);
		c58.addHasCollectionItem(col62);
		c58.addHasCollectionItem(col63);
		c58.addHasCollectionItem(col64);
		c58.addHasSubCommunityItem(c60);
		c58.addHasSubCommunityItem(c59);
		c58.addIsPartOfRepositoryIDItem(rep0.getId());
		
		/*****
		 * <http://localhost:8080/server/rdf/resource/123456789/59>
		 * dspace:hasCollection     <http://localhost:8080/server/rdf/resource/123456789/61> , <http://localhost:8080/server/rdf/resource/123456789/62> ;
         * dspace:hasSubcommunity   <http://localhost:8080/server/rdf/resource/123456789/65> ;
         * dspace:isSubcommunityOf  <http://localhost:8080/server/rdf/resource/123456789/58> ;
		 */
		c59.addHasCollectionItem(col61);
		c59.addHasCollectionItem(col62);
		c59.addHasSubCommunityItem(c65);
		c59.addIsSubcommunityOfIDItem(c58.getId());
		/*******
		 * <http://localhost:8080/server/rdf/resource/123456789/60>
		 * dspace:hasCollection     
		 * 		<http://localhost:8080/server/rdf/resource/123456789/63> ,
		 * 		<http://localhost:8080/server/rdf/resource/123456789/64> ;
		 * dspace:isSubcommunityOf  
		 * 		<http://localhost:8080/server/rdf/resource/123456789/58> ;
		 */
		c60.addHasCollectionItem(col63);
		c60.addHasCollectionItem(col64);
		c60.addIsSubcommunityOfIDItem(c58.getId());
		/***************
		 * <http://localhost:8080/server/rdf/resource/123456789/61>
		 * 	dspace:hasItem <http://localhost:8080/server/rdf/resource/123456789/66> ;
		 * 	dspace:isPartOfCommunity  
		 * 		<http://localhost:8080/server/rdf/resource/123456789/59> , 
		 * 		<http://localhost:8080/server/rdf/resource/123456789/58> ;
		 */
		col61.addIsPartOfCommunityIDItem(c58.getId());
		col61.addIsPartOfCommunityIDItem(c59.getId());
		col61.addHasItemItem(item66);
		/********************
		 * <http://localhost:8080/server/rdf/resource/123456789/62>
		 * dspace:hasItem <http://localhost:8080/server/rdf/resource/123456789/68> ;
		 * dspace:isPartOfCommunity  
		 * 		<http://localhost:8080/server/rdf/resource/123456789/59> , 
		 * 		<http://localhost:8080/server/rdf/resource/123456789/58> ;
		 */
		col62.addHasItemItem(item68);
		col62.addIsPartOfCommunityIDItem(c59.getId());
		col62.addIsPartOfCommunityIDItem(c58.getId());
		/**********************
		 * <http://localhost:8080/server/rdf/resource/123456789/66>
		 * dspace:hasBitstream        <http://localhost:4000/bitstream/123456789/66/1/bubble-chart-line.png> ;
		 * dspace:isPartOfCollection  <http://localhost:8080/server/rdf/resource/123456789/61> ;
		 * dcterms:title              "Test paper" ;
		 */
		item66.setDspaceBitstreamURLs(Lists.newArrayList("http://localhost:4000/bitstream/123456789/66/1/bubble-chart-line.png"));
		item66.addDspaceIsPartOfCollectionIDItem(col61.getId());
		item66.addListOfStatementLiteralsItem(item66Title);
		/*****************
		 * <http://localhost:8080/server/rdf/resource/123456789/68>        
		 * dspace:hasBitstream        <http://localhost:4000/bitstream/123456789/68/1/bubble-chart-line.png> ;
		 * dspace:isPartOfCollection  <http://localhost:8080/server/rdf/resource/123456789/62> ;
		 * dcterms:title              "Test thesis" ;
		 */
		item68.setDspaceBitstreamURLs(Lists.newArrayList("http://localhost:4000/bitstream/123456789/66/1/bubble-chart-line.png"));
		item68.addDspaceIsPartOfCollectionIDItem(col62.getId());
		item68.addListOfStatementLiteralsItem(item68Title);
		/********
		 * DUMP Data and exit		
		 */
		TestCollectionTaxonomy.dump(rep0);

		
	}

	private static void dump(Repository rep0) throws JsonProcessingException {
		/***********
		 * Dump model
		 */
		ObjectMapper mapper = new ObjectMapper();
        String prettyStaff1 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rep0);

        System.out.println(prettyStaff1);//
        System.exit(0);

		
	}

}
