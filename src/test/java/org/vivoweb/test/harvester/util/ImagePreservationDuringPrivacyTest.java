package org.vivoweb.test.harvester.util;

import java.io.IOException;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.ImagePreservationDuringPrivacy;
import org.vivoweb.harvester.util.repo.JenaConnect;
import org.vivoweb.harvester.util.repo.MemJenaConnect;
import org.vivoweb.harvester.util.repo.TDBJenaConnect;

/**
 * @author rziede
 * JUnitTest for ImagePreservationDuringPrivacy.
 */
public class ImagePreservationDuringPrivacyTest extends TestCase {
	
	/**
	 * Logger for logging and tracing important info.
	 */
	private static Logger log = LoggerFactory.getLogger(ImagePreservationDuringPrivacyTest.class);
	
	/**
	 * These RDF/XML strings contain 4 people each, matching entries in the complimentary model (by UFID).
	 * Two of the privacy flags change between vivo->input: 	one Y->N, one N->Y.
	 * Two of the privacy flags stay the same:					one Y->Y, one N->N.
	 */
	protected static final String vivoRDF = ""+
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
	"<rdf:RDF xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" "+
			"xmlns:score=\"http://vivoweb.org/ontology/score#\"	"+	
			"xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\" "+	
	"        xmlns:bibo=\"http://purl.org/ontology/bibo/\"						"+	
	"        xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"				"+	
	"        xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"						"+
	"        xmlns:owl=\"http://www.w3.org/2002/07/owl#\"						"+	
	"        xmlns:public=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\""+
	"        xmlns:ns0=\"http://uf.biztalk.shibperson\"						"+
	"        xmlns:datetime=\"http://exslt.org/dates-and-times\"				"+
	"        xmlns:ufVivo=\"http://vivo.ufl.edu/ontology/vivo-ufl/\"			"+
	"        xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"			"+
	"        xmlns:core=\"http://vivoweb.org/ontology/core#\">					"+
	
	// EMILY RHOADES: Privacy N -> Y
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/06918570\"> "+
	"     <ufVivo:ufid>06918570</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Emily</foaf:firstName> "+
	"     <foaf:lastName>Rhoades</foaf:lastName> "+
	"     <core:middleName>Bisdorf</core:middleName> "+
	"     <rdfs:label>Rhoades,Emily Bisdorf</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">bisdorf2</ufVivo:gatorlink> "+
	"     <core:preferredTitle>GRAD ASST - R</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>	"+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>N</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>bisdorf2@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 392-0502</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/60030000\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/06918570\"/> "+
	"           <ufVivo:deptID>60030000</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/> "+
	"     <public:mainImage> "+
	"     <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/06918570/mainImg/ufid06918570\"> "+
	"     <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/> "+
	"     <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/> "+
	"	  <public:downloadLocation> "+
	"	  	<rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/06918570/fullDirDownload/ufid06918570\"> "+
	"	  		<public:directDownloadUrl>/file/person.thumbnail.jpg</public:directDownloadUrl> "+
	"	  		<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/> "+
	"	 		<vitro:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2011-08-22T15:50:54</vitro:modTime> "+
	"	 	</rdf:Description> "+	
	"	  </public:downloadLocation> "+
	"	  <public:thumbnailImage> "+
	"	  	<rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/06918570/thumbImg/ufid06918570\"> "+
	"	  		<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/> "+
	"	 		<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/> "+
	"			<public:downloadLocation> "+
	"	  			<rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/06918570/thumbDirDownload/ufid06918570\"> "+
	"			  		<public:directDownloadUrl>/file/person.thumbnail.jpg</public:directDownloadUrl> "+
	"			  		<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/> "+
	"			  		<vitro:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2011-08-22T15:50:54</vitro:modTime> "+
	"				</rdf:Description> "+
	"			</public:downloadLocation> "+
	"			<public:filename rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">person.thumbnail.jpg</public:filename> "+
	"			<public:mimeType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">image/jpeg</public:mimeType> "+
	"		</rdf:Description> "+
	"	</public:thumbnailImage> "+
	"	<public:filename rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">person.thumbnail.jpg</public:filename> "+
	"	<public:mimeType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">image/jpeg</public:mimeType> "+
	"	</rdf:Description> "+
	"	</public:mainImage> "+
	"  </rdf:Description> "+
	   
	//JUSTIN LAUFER: Privacy Y -> N
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691\"> "+
	"     <ufVivo:ufid>14412691</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Justin</foaf:firstName> "+
	"     <foaf:lastName>Laufer</foaf:lastName> "+
	"     <core:middleName>Alexander</core:middleName> "+
	"     <rdfs:label>Laufer,Justin Alexander</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">jlaufer</ufVivo:gatorlink> "+
	"     <core:preferredTitle>IT Expert</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/> "+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>Y</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>jlaufer@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 273-4717</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/57140100\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/14412691\"/> "+
	"           <ufVivo:deptID>57140100</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <Ignore/> "+
	"  </rdf:Description> "+
	
	// THEODORE LOGAN: Privacy NULL -> Y
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412142\"> "+
	"     <ufVivo:ufid>14412142</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Theodore</foaf:firstName> "+
	"     <foaf:lastName>Logan</foaf:lastName> "+
	"     <core:middleName>J</core:middleName> "+
	"     <rdfs:label>Logan,Theodore J</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">tedLogan</ufVivo:gatorlink> "+
	"     <core:preferredTitle>IT Expert</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/> "+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	//"     <ufVivo:privacyFlag>Y</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>tedLogan@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 273-4742</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/57140100\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/14412142\"/> "+
	"           <ufVivo:deptID>57140100</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <Ignore/> "+
	"  </rdf:Description> "+
	
	// BILL PRESTON: Privacy N -> N
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/44839862\"> "+
	"     <ufVivo:ufid>44839862</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Bill</foaf:firstName> "+
	"     <foaf:lastName>Preston</foaf:lastName> "+
	"     <core:middleName>S</core:middleName> "+
	"     <rdfs:label>Preston, Bill</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">tedLogan</ufVivo:gatorlink> "+
	"     <core:preferredTitle>IT Expert</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/> "+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>N</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>bPreston@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 273-2222</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/57140100\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/44839862\"/> "+
	"           <ufVivo:deptID>57140100</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <Ignore/> "+
	"  </rdf:Description> "+
	
	"</rdf:RDF> ";

	@SuppressWarnings("javadoc")
	protected static final String inputRDF = ""+
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?> 								"+
	"<rdf:RDF xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"						"+
	"        xmlns:score=\"http://vivoweb.org/ontology/score#\"				"+	
	"        xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\"	"+	
	"        xmlns:bibo=\"http://purl.org/ontology/bibo/\"						"+	
	"        xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"				"+	
	"        xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"						"+
	"        xmlns:owl=\"http://www.w3.org/2002/07/owl#\"						"+	
	"        xmlns:public=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\""+
	"        xmlns:ns0=\"http://uf.biztalk.shibperson\"						"+
	"        xmlns:datetime=\"http://exslt.org/dates-and-times\"				"+
	"        xmlns:ufVivo=\"http://vivo.ufl.edu/ontology/vivo-ufl/\"			"+
	"        xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"			"+
	"        xmlns:core=\"http://vivoweb.org/ontology/core#\">					"+
	
	// EMILY RHOADES: Privacy N -> Y
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/06918570\"> "+
	"     <ufVivo:ufid>06918570</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Emily</foaf:firstName> "+
	"     <foaf:lastName>Rhoades</foaf:lastName> "+
	"     <core:middleName>Bisdorf</core:middleName> "+
	"     <rdfs:label>Rhoades,Emily Bisdorf</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">bisdorf2</ufVivo:gatorlink> "+
	"     <core:preferredTitle>GRAD ASST - R</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/>	"+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>Y</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>bisdorf2@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 392-0502</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/60030000\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/06918570\"/> "+
	"           <ufVivo:deptID>60030000</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#FacultyMember\"/> "+
	"  </rdf:Description> "+
	   
	//JUSTIN LAUFER: Privacy Y -> N
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691\"> "+
	"     <ufVivo:ufid>14412691</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Justin</foaf:firstName> "+
	"     <foaf:lastName>Laufer</foaf:lastName> "+
	"     <core:middleName>Alexander</core:middleName> "+
	"     <rdfs:label>Laufer,Justin Alexander</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">jlaufer</ufVivo:gatorlink> "+
	"     <core:preferredTitle>IT Expert</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/> "+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>N</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>jlaufer@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 273-4717</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/57140100\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/14412691\"/> "+
	"           <ufVivo:deptID>57140100</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <Ignore/> "+
	"  </rdf:Description> "+
	
	// THEODORE LOGAN: Privacy NULL -> Y
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412142\"> "+
	"     <ufVivo:ufid>14412142</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Theodore</foaf:firstName> "+
	"     <foaf:lastName>Logan</foaf:lastName> "+
	"     <core:middleName>J</core:middleName> "+
	"     <rdfs:label>Logan,Theodore J</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">tedLogan</ufVivo:gatorlink> "+
	"     <core:preferredTitle>IT Expert</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/> "+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>Y</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>tedLogan@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 273-4742</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/57140100\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/14412142\"/> "+
	"           <ufVivo:deptID>57140100</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <Ignore/> "+
	"  </rdf:Description> "+
	
	// BILL PRESTON: Privacy N -> N
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/44839862\"> "+
	"     <ufVivo:ufid>44839862</ufVivo:ufid> "+
	"     <ufVivo:harvestedBy>PeopleSoft-BizTalk-Harvester</ufVivo:harvestedBy> "+
	"     <foaf:firstName>Bill</foaf:firstName> "+
	"     <foaf:lastName>Preston</foaf:lastName> "+
	"     <core:middleName>S</core:middleName> "+
	"     <rdfs:label>Esquire,Theodore Logan</rdfs:label> "+
	"     <ufVivo:gatorlink rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">tedLogan</ufVivo:gatorlink> "+
	"     <core:preferredTitle>IT Expert</core:preferredTitle> "+
	"     <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Person\"/> "+
	"     <ufVivo:Deceased>N</ufVivo:Deceased> "+
	"     <ufVivo:privacyFlag>N</ufVivo:privacyFlag> "+
	"     <core:primaryEmail>tedLogan@ufl.edu</core:primaryEmail> "+
	"     <core:primaryPhoneNumber>(352) 273-2222</core:primaryPhoneNumber> "+
	"     <core:faxNumber/> "+
	"     <ufVivo:homeDept> "+
	"        <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/dept/57140100\"> "+
	"           <ufVivo:homeDeptFor rdf:resource=\"http://vivo.ufl.edu/harvested/person/44839862\"/> "+
	"           <ufVivo:deptID>57140100</ufVivo:deptID> "+
	"           <ufVivo:mostSpecificType rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#AcademicDepartment\"/> "+
	"           <rdf:type rdf:resource=\"http://vivoweb.org/ontology/core#Department\"/> "+
	"           <rdf:type rdf:resource=\"http://xmlns.com/foaf/0.1/Organization\"/> "+
	"        </rdf:Description> "+
	"     </ufVivo:homeDept> "+
	"     <Ignore/> "+
	"  </rdf:Description> "+
	
	"</rdf:RDF> ";	
	
	@SuppressWarnings("javadoc")
	protected static final String privateRDF = ""+
	"<?xml version=\"1.0\" encoding=\"UTF-8\"?> 								"+
	"<rdf:RDF xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"						"+
	"        xmlns:score=\"http://vivoweb.org/ontology/score#\"				"+	
	"        xmlns:vitro=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\"	"+	
	"        xmlns:bibo=\"http://purl.org/ontology/bibo/\"						"+	
	"        xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"				"+	
	"        xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"						"+
	"        xmlns:owl=\"http://www.w3.org/2002/07/owl#\"						"+	
	"        xmlns:public=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#\""+
	"        xmlns:ns0=\"http://uf.biztalk.shibperson\"						"+
	"        xmlns:datetime=\"http://exslt.org/dates-and-times\"				"+
	"        xmlns:ufVivo=\"http://vivo.ufl.edu/ontology/vivo-ufl/\"			"+
	"        xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"			"+
	"        xmlns:core=\"http://vivoweb.org/ontology/core#\">					"+
	
	"  <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691\"> "+
	"     <ufVivo:ufid>14412691</ufVivo:ufid> "+	
	"     <public:mainImage> "+
	"     <rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691/mainImg/ufid14412691\"> "+
	"     <rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/> "+
	"     <rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/> "+
	"	  <public:downloadLocation> "+
	"	  	<rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691/fullDirDownload/ufid14412691\"> "+
	"	  		<public:directDownloadUrl>/file/person.thumbnail.jpg</public:directDownloadUrl> "+
	"	  		<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/> "+
	"	 		<vitro:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2011-14-22T15:50:54</vitro:modTime> "+
	"	 	</rdf:Description> "+	
	"	  </public:downloadLocation> "+
	"	  <public:thumbnailImage> "+
	"	  	<rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691/thumbImg/ufid14412691\"> "+
	"	  		<rdf:type rdf:resource=\"http://www.w3.org/2002/07/owl#Thing\"/> "+
	"	 		<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#File\"/> "+
	"			<public:downloadLocation> "+
	"	  			<rdf:Description rdf:about=\"http://vivo.ufl.edu/harvested/person/14412691/thumbDirDownload/ufid14412691\"> "+
	"			  		<public:directDownloadUrl>/file/person.thumbnail.jpg</public:directDownloadUrl> "+
	"			  		<rdf:type rdf:resource=\"http://vitro.mannlib.cornell.edu/ns/vitro/public#FileByteStream\"/> "+
	"			  		<vitro:modTime rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">2011-14-22T15:50:54</vitro:modTime> "+
	"				</rdf:Description> "+
	"			</public:downloadLocation> "+
	"			<public:filename rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">person.thumbnail.jpg</public:filename> "+
	"			<public:mimeType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">image/jpeg</public:mimeType> "+
	"		</rdf:Description> "+
	"	</public:thumbnailImage> "+
	"	<public:filename rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">person.thumbnail.jpg</public:filename> "+
	"	<public:mimeType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">image/jpeg</public:mimeType> "+
	"	</rdf:Description> "+
	"	</public:mainImage> "+
	"  </rdf:Description> "+	
	
	"</rdf:RDF> ";
	
	
	/**
	 * Input Model
	 */
	private JenaConnect inputModel;
	/**
	 * Vivo Model
	 */
	private JenaConnect vivoModel;
	/**
	 * Private Model
	 */
	private JenaConnect privModel;
	/**
	 * Temporary Model
	 */
	private TDBJenaConnect temp;
	
	@SuppressWarnings("javadoc")
	public void testExecute() throws IOException 
	{
		log.trace("BEGIN ImagePreservationDuringPrivacy test:");
		ImagePreservationDuringPrivacy ipdp = new ImagePreservationDuringPrivacy(this.inputModel, this.privModel, this.vivoModel, true, true);
		ipdp.execute();
	}
	
	@Override
	protected void setUp() throws Exception
	{
		//TODO: This step may not even be needed, as tempModel unioning is done in ImagePres itself. Clean up later!
		//this.temp = new TDBJenaConnect("PrivTestModel", "test");
		this.temp = new MemJenaConnect("test");
		
		this.inputModel = this.temp.neighborConnectClone("input");
		this.inputModel.loadRdfFromString(inputRDF, null, "RDF/XML");
		
		this.vivoModel = this.temp.neighborConnectClone("vivo");
		this.vivoModel.loadRdfFromString(vivoRDF, null, "RDF/XML");
		
		this.privModel = this.temp.neighborConnectClone("priv");
		this.privModel.loadRdfFromString(privateRDF, null, "RDF/XML"); 
	}
		
	@Override
	protected void tearDown()
	{
		tearDownModel(this.vivoModel);
		this.vivoModel = null;
		
		tearDownModel(this.inputModel);
		this.inputModel = null;
		
		tearDownModel(this.privModel);
		this.privModel = null;
		
		tearDownModel(this.temp);
		this.temp = null;
	}
	
	@SuppressWarnings("javadoc")
	private void tearDownModel(JenaConnect model)
	{
		if(model != null) 
		{
			try 
			{
				model.truncate();
			}
			catch(Exception e) 
			{
				//Ignore
			} 
			finally 
			{
				try 
				{
					model.close();
				} 
				catch(Exception e) 
				{
					//Ignore
				}
			}
		}
	}
}
