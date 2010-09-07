/**
 * 
 */
package org.vivoweb.ingest.test.translate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.translate.XSLTranslator;
import org.vivoweb.ingest.util.repo.RecordHandler;
import org.vivoweb.ingest.util.repo.TextFileRecordHandler;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * @author ICHPDOMAIN\swilliams
 *
 */
public class TranslateTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
//		VFS.getManager().resolveFile(new File("."), "TestVault/Translate").createFolder();
//	   	VFS.getManager().resolveFile(new File("."), "TestVault/Translate/RH/inRH.xml").createFile();
//	   	VFS.getManager().resolveFile(new File("."), "TestVault/Translate/RH/outRH.xml").createFile();
		super.setUp();
	}

   /**
    * I am adding this so you stop killing maven builds >:O
    */
   public void testNothing() {
	   	assertTrue(true);
   }

   /**
    * 
    */
   public void testPubMedXSLT(){
	   	   try {
	   		xslTranslateTest(buildPubMedXML().toString(), buildPubMedOutput().toString(),"config/datamaps/PubMedToVivo.xsl");
	   	} catch (Exception e) {
	   		fail(e.getMessage());
	   	} 
   	}
   
   /**
	* @param xmlFile	The file to Translate
	* @param xmlOutput	The expected Output
	* @param mapFile FIXME
	* @throws IOException	FIXME
	* @throws ClassNotFoundException FIXME
 * @throws SAXException 
 * @throws ParserConfigurationException 
	*/
   private void xslTranslateTest(String xmlFile, String xmlOutput, String mapFile) throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException {
		
	   //grab config file for getting/storing the result
	   	File inFile = File.createTempFile("inputRHConfig", "xml");
		Writer output = new BufferedWriter(new FileWriter(inFile));
	    output.write("<?xml version='1.0' encoding='UTF-8'?>" +
	    		"<RecordHandler type='org.vivoweb.ingest.util.repo.TextFileRecordHandler'>" +
	    		"<Param name='fileDir'>tmp://TranslateTestXML</Param>" +
	    		"</RecordHandler>");
	    output.close();
		
   		File outFile = File.createTempFile("outRHConfig", "xml");
		output = new BufferedWriter(new FileWriter(outFile));
	    output.write("<?xml version='1.0' encoding='UTF-8'?>" +
	    		"<RecordHandler type='org.vivoweb.ingest.util.repo.TextFileRecordHandler'>" +
	    		"<Param name='fileDir'>tmp://TranslateTestRDF</Param>" +
	    		"</RecordHandler>");
	    output.close();
	    
	    //add a record to the record handler
	    RecordHandler inRH = RecordHandler.parseConfig(inFile.getAbsolutePath());
	    inRH.addRecord("1",xmlFile, this.getClass());
	    
		//create the arguments to be passed		
		String[] argsToBePassed = new String[6];
		argsToBePassed[0] = "-x";
		argsToBePassed[1] = mapFile;
		argsToBePassed[2] = "-i";
		argsToBePassed[3] = inFile.getAbsolutePath();
		argsToBePassed[4] = "-o";
		argsToBePassed[5] = outFile.getAbsolutePath();
		
		//call the xlsTranslate
		XSLTranslator.main(argsToBePassed);
		
		//get the file that was translated
	    RecordHandler outRH = RecordHandler.parseConfig(outFile.getAbsolutePath());
	    outRH.getRecord("1").getData();
		
	    VFS.getManager().resolveFile(new File("."),"output.xml").getContent().getOutputStream().write(outRH.getRecordData("1").getBytes());
	    
   		//compare the output
		assertEquals(outRH.getRecordData("1").replace('\"', '\'').replace("\n ",""), xmlOutput);
   }
   
   /**
 * @return String of an example pubmed file
 */
private StringBuilder buildPubMedXML(){
	   StringBuilder outputXML = new StringBuilder();
	   outputXML.append("<?xml version='1.0'?>");
	   outputXML.append("<!DOCTYPE PubmedArticleSet PUBLIC '-//NLM//DTD PubMedArticle, 1st January 2010//EN' ");
	   outputXML.append("'http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd'>");
	   outputXML.append("<PubmedArticleSet>");
	   outputXML.append("<PubmedArticle><MedlineCitation Owner='NLM' Status='MEDLINE'>");
	   outputXML.append("<PMID>19885433</PMID>");
	   outputXML.append("<DateCreated><Year>2009</Year><Month>11</Month><Day>03</Day></DateCreated>");
	   outputXML.append("<DateCompleted><Year>2010</Year><Month>02</Month><Day>02</Day></DateCompleted>");
	   outputXML.append("<Article PubModel='Print'>");
	   outputXML.append("<Journal><ISSN IssnType='Print'>0882-2786</ISSN>");
	   outputXML.append("<JournalIssue CitedMedium='Print'><Volume>24 Suppl</Volume><PubDate><Year>2009</Year></PubDate></JournalIssue>");
	   outputXML.append("<Title>The International journal of oral &amp; maxillofacial implants</Title><ISOAbbreviation>Int J Oral Maxillofac Implants</ISOAbbreviation>");
	   outputXML.append("</Journal>");
	   outputXML.append("<ArticleTitle>Local risk factors for implant therapy.</ArticleTitle>");
	   outputXML.append("<Pagination><MedlinePgn>28-38</MedlinePgn></Pagination>");
	   outputXML.append("<Abstract><AbstractText>PURPOSE: The aim of this review was to determine the effect of several potential local risk factors on implant survival and success (primary outcomes) as well as on mucosal recession, bleeding on probing, and proximal marginal bone loss (secondary outcomes). MATERIALS AND METHODS: A comprehensive review of the literature was conducted. The selection of publications reporting on human clinical studies was based on predetermined inclusion criteria and was agreed upon by three reviewers. After title and abstract screening of 2,681 publications obtained from the search, 19 articles were deemed to be relevant to the topic and the search criteria. RESULTS: Limited data show that when an implant is placed within 3 mm of the neighboring tooth, proximal bone is at risk. The data regarding the placement of implants into infected sites are still insufficient, but studies have shown that this may be possible. Soft tissue thickness has not been shown to be a risk factor in implant survival. There is also no evidence to support a relationship between the width of keratinized tissue and implant survival. No studies were found that directly related bone density to implant survival. Implant stability was also difficult to examine due to the lack of validated stability measures. DISCUSSION AND CONCLUSION: One critical factor that faced the group during the review of the literature and interpretation of the data was the multifactorial nature of implant therapy. This makes isolation of specific risk factors difficult. Conclusions are limited by the current lack of quality clinical trials in this area.</AbstractText></Abstract>");
	   outputXML.append("<Affiliation>Department of Oral and Maxillofacial Surgery, University of Florida College of Dentistry, 1600 SW Archer Road, Room D7-6, Gainesville, FL 32610, USA. wmartin@dental.ufl.edu</Affiliation>");
	   outputXML.append("<AuthorList CompleteYN='Y'>");
	   outputXML.append("<Author ValidYN='Y'><LastName>Martin</LastName><ForeName>William</ForeName><Initials>W</Initials></Author>");
	   outputXML.append("<Author ValidYN='Y'><LastName>Lewis</LastName><ForeName>Emma</ForeName><Initials>E</Initials></Author>");
	   outputXML.append("<Author ValidYN='Y'><LastName>Nicol</LastName><ForeName>Ailsa</ForeName><Initials>A</Initials></Author></AuthorList>");
	   outputXML.append("<Language>eng</Language>");
	   outputXML.append("<PublicationTypeList><PublicationType>Journal Article</PublicationType><PublicationType>Review</PublicationType></PublicationTypeList>");
	   outputXML.append("</Article>");
	   outputXML.append("<MedlineJournalInfo><Country>United States</Country><MedlineTA>Int J Oral Maxillofac Implants</MedlineTA>");
	   outputXML.append("<NlmUniqueID>8611905</NlmUniqueID><ISSNLinking>0882-2786</ISSNLinking></MedlineJournalInfo>");
	   outputXML.append("<ChemicalList><Chemical><RegistryNumber>0</RegistryNumber><NameOfSubstance>Dental Implants</NameOfSubstance></Chemical></ChemicalList>");
	   outputXML.append("<CitationSubset>D</CitationSubset><CitationSubset>IM</CitationSubset>");
	   outputXML.append("<MeshHeadingList>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Bone Density</DescriptorName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Dental Arch</DescriptorName><QualifierName MajorTopicYN='N'>anatomy &amp; histology</QualifierName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Dental Implantation, Endosseous</DescriptorName><QualifierName MajorTopicYN='Y'>contraindications</QualifierName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Dental Implants</DescriptorName><QualifierName MajorTopicYN='Y'>contraindications</QualifierName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Dental Restoration Failure</DescriptorName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Gingiva</DescriptorName><QualifierName MajorTopicYN='N'>anatomy &amp; histology</QualifierName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Humans</DescriptorName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Infection</DescriptorName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Periodontitis</DescriptorName></MeshHeading>");
	   outputXML.append("<MeshHeading><DescriptorName MajorTopicYN='N'>Risk Factors</DescriptorName></MeshHeading>");
	   outputXML.append("</MeshHeadingList>");
	   outputXML.append("<NumberOfReferences>72</NumberOfReferences>");
	   outputXML.append("</MedlineCitation>");
	   outputXML.append("<PubmedData><History><PubMedPubDate PubStatus='entrez'><Year>2009</Year><Month>11</Month><Day>4</Day><Hour>6</Hour><Minute>0</Minute></PubMedPubDate><PubMedPubDate PubStatus='pubmed'><Year>2009</Year><Month>12</Month><Day>4</Day><Hour>6</Hour><Minute>0</Minute></PubMedPubDate><PubMedPubDate PubStatus='medline'><Year>2010</Year><Month>2</Month><Day>3</Day><Hour>6</Hour><Minute>0</Minute></PubMedPubDate></History><PublicationStatus>ppublish</PublicationStatus><ArticleIdList><ArticleId IdType='pubmed'>19885433</ArticleId></ArticleIdList></PubmedData></PubmedArticle>");
	   outputXML.append("</PubmedArticleSet>");
	   return outputXML;
   }

   /**
 * @return String of the expected output from a translate of pubmed
 */
private StringBuilder buildPubMedOutput(){
	   StringBuilder outputXML = new StringBuilder();
	   outputXML.append("<?xml version='1.0' encoding='UTF-8'?>");
	   outputXML.append("<rdf:RDF xmlns:bibo='http://purl.org/ontology/bibo/'");
	   outputXML.append("xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'");
	   outputXML.append("xmlns:owlPlus='http://www.w3.org/2006/12/owl2-xml#'");
	   outputXML.append("xmlns:xs='http://www.w3.org/2001/XMLSchema#'");
	   outputXML.append("xmlns:skos='http://www.w3.org/2008/05/skos#'");
	   outputXML.append("xmlns:owl='http://www.w3.org/2002/07/owl#'");
	   outputXML.append("xmlns:vocab='http://purl.org/vocab/vann/'");
	   outputXML.append("xmlns:swvocab='http://www.w3.org/2003/06/sw-vocab-status/ns#'");
	   outputXML.append("xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'");
	   outputXML.append("xmlns:dc='http://purl.org/dc/elements/1.1/'");
	   outputXML.append("xmlns:core='http://vivoweb.org/ontology/core#'");
	   outputXML.append("xmlns:vitro='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#'");
	   outputXML.append("xmlns:foaf='http://xmlns.com/foaf/0.1/'");
	   outputXML.append("xmlns:score='http://vivoweb.org/ontology/score#'>");
	   outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/article/pmid19885433'>");
	   outputXML.append("<rdf:type rdf:resource='http://purl.org/ontology/bibo/Document'/>");
	   outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
	   outputXML.append("<bibo:pmid>19885433</bibo:pmid>");
	   outputXML.append("<rdfs:label>Local risk factors for implant therapy.</rdfs:label>");
	   outputXML.append("<core:Title>Local risk factors for implant therapy.</core:Title>");
	   outputXML.append("<score:Affiliation>Department of Oral and Maxillofacial Surgery, University of Florida College of Dentistry, 1600 SW Archer Road, Room D7-6, Gainesville, FL 32610, USA. wmartin@dental.ufl.edu</score:Affiliation>");
	   outputXML.append("<bibo:volume>24 Suppl</bibo:volume>");
	   outputXML.append("<bibo:number/>");
	   outputXML.append("<core:Year>2009</core:Year>");
	   outputXML.append("<score:workEmail>wmartin@dental.ufl.edu</score:workEmail>");
	   outputXML.append("<core:informationResourceInAuthorship rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/authorship1'/>");
	   outputXML.append("<core:informationResourceInAuthorship rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/authorship2'/>");
	   outputXML.append("<core:informationResourceInAuthorship rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/authorship3'/>");
	   outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh1'/>");
	   outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh2'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh3'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh4'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh5'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh6'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh7'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh8'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh9'/>");
		outputXML.append("<core:hasSubjectArea rdf:nodeID='pmid19885433mesh10'/>");
		outputXML.append("<core:hasPublicationVenue rdf:resource='http://vivoweb.org/pubMed/journal/j0882-2786'/>");
		outputXML.append("<score:hasCreateDate rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/dateCreated'/>");
		outputXML.append("<score:hasCompleteDate rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/dateCompleted'/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/article/pmid19885433/authorship1'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Authorship'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#DependentResource'/>");
		outputXML.append("<core:linkedAuthor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/author1'/>");
		outputXML.append("<core:linkedInformationResource rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Authorship for Martin, William</rdfs:label>");
		outputXML.append("<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>1</core:authorRank>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/article/pmid19885433/authorship2'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Authorship'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#DependentResource'/>");
		outputXML.append("<core:linkedAuthor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/author2'/>");
		outputXML.append("<core:linkedInformationResource rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Authorship for Lewis, Emma</rdfs:label>");
		outputXML.append("<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>2</core:authorRank>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/article/pmid19885433/authorship3'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#Authorship'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#DependentResource'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#DependentResource'/>");
		outputXML.append("<core:linkedAuthor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433/author3'/>");
		outputXML.append("<core:linkedInformationResource rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Authorship for Nicol, Ailsa</rdfs:label>");
		outputXML.append("<core:authorRank rdf:datatype='http://www.w3.org/2001/XMLSchema#int'>3</core:authorRank>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh1'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Bone Density</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Bone Density</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier/>");
		outputXML.append("<score:QualifierIsMajorTerm/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh2'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Dental Arch</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Dental Arch</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier>anatomy &amp; histology</score:Qualifier>");
		outputXML.append("<score:QualifierIsMajorTerm>N</score:QualifierIsMajorTerm>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh3'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Dental Implantation, Endosseous</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Dental Implantation, Endosseous</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier>contraindications</score:Qualifier>");
		outputXML.append("<score:QualifierIsMajorTerm>Y</score:QualifierIsMajorTerm>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh4'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Dental Implants</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Dental Implants</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier>contraindications</score:Qualifier>");
		outputXML.append("<score:QualifierIsMajorTerm>Y</score:QualifierIsMajorTerm>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh5'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Dental Restoration Failure</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Dental Restoration Failure</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier/>");
		outputXML.append("<score:QualifierIsMajorTerm/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh6'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Gingiva</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Gingiva</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier>anatomy &amp; histology</score:Qualifier>");
		outputXML.append("<score:QualifierIsMajorTerm>N</score:QualifierIsMajorTerm>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh7'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Humans</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Humans</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier/>");
		outputXML.append("<score:QualifierIsMajorTerm/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh8'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Infection</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Infection</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier/>");
		outputXML.append("<score:QualifierIsMajorTerm/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh9'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Periodontitis</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Periodontitis</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier/>");
		outputXML.append("<score:QualifierIsMajorTerm/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:nodeID='pmid19885433mesh10'>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/score#MeshTerm'/>");
		outputXML.append("<rdf:type rdf:resource='http://vivoweb.org/ontology/core#SubjectArea'/>");
		outputXML.append("<core:SubjectAreaFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<rdfs:label>Risk Factors</rdfs:label>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<score:meshTermOf rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("<score:Descriptor>Risk Factors</score:Descriptor>");
		outputXML.append("<score:DescriptorIsMajorTerm>N</score:DescriptorIsMajorTerm>");
		outputXML.append("<score:Qualifier/>");
		outputXML.append("<score:QualifierIsMajorTerm/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/journal/j0882-2786'>");
		outputXML.append("<rdf:type rdf:resource='http://purl.org/ontology/bibo/Journal'/>");
		outputXML.append("<rdf:type rdf:resource='http://vitro.mannlib.cornell.edu/ns/vitro/0.7#Flag1Value1Thing'/>");
		outputXML.append("<core:Title>The International journal of oral &amp; maxillofacial implants</core:Title>");
		outputXML.append("<rdfs:label>The International journal of oral &amp; maxillofacial implants</rdfs:label>");
		outputXML.append("<bibo:ISSN>0882-2786</bibo:ISSN>");
		outputXML.append("<core:publicationVenueFor rdf:resource='http://vivoweb.org/pubMed/article/pmid19885433'/>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/article/pmid19885433/dateCreated'>");
		outputXML.append("<core:Year>'2009'</core:Year>");
		outputXML.append("<core:Month>'11'</core:Month>");
		outputXML.append("<core:Day>'03'</core:Day>");
		outputXML.append("</rdf:Description>");
		outputXML.append("<rdf:Description rdf:about='http://vivoweb.org/pubMed/article/pmid19885433/dateCompleted'>");
		outputXML.append("<core:Year>2010</core:Year>");
		outputXML.append("<core:Month>02</core:Month>");
		outputXML.append("<core:Day>02</core:Day>");
		outputXML.append("</rdf:Description>");
		outputXML.append("</rdf:RDF>");
	   return outputXML;
   }

	@Override
	protected void tearDown() throws Exception {
//		VFS.getManager().resolveFile(new File("."),"TestVault/Translate").delete(new AllFileSelector());
	}

}
