import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.util.StoreUtils;

/**
 * Test to verify that SDB h2 delete works
 */
public class FailingToDeleteFromSDB {
	
	/** */
	private static String dbClass = "org.h2.Driver";
	/** */
	private static String dbUrl = "jdbc:h2:mem:test";
	/** */
	private static String dbUser = "sa";
	/** */
	private static String dbPass = "";
	/** */
	private static String dbLayout = "layout2";
	/** */
	private static String dbType = "H2";
	/** */
	private static String modelName = "testing123";
	/** */
	private static String previousRDF = ""+
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<rdf:RDF " +
					"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
					"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " +
					"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
				"<rdf:Description rdf:about=\"http://random.com/uris/uri1234\">" +
					"<foaf:firstName>Guys</foaf:firstName>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
					"<rdfs:label xml:lang=\"en-US\">Fawkes, Guy</rdfs:label>" +
				"</rdf:Description>" +
			"</rdf:RDF>";
	/**
	 * firstName: Guys -> Guy
	 * label: remove lang 
	 */
	private static String incomingRDF = ""+
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<rdf:RDF " +
					"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
					"xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" " +
					"xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
				"<rdf:Description rdf:about=\"http://random.com/uris/uri1234\">" +
					"<foaf:firstName>Guy</foaf:firstName>" +
					"<foaf:lastName>Fawkes</foaf:lastName>" +
					"<rdfs:label>Fawkes, Guy</rdfs:label>" +
				"</rdf:Description>" +
			"</rdf:RDF>";
	
	/**
	 * Export all RDF from a model as RDF/XML
	 * @param m the model
	 * @return the rdf xml
	 * @throws IOException error writing to stream
	 */
	public static String exportRdfToString(Model m) throws IOException {
		RDFWriter fasterWriter = m.getWriter("RDF/XML");
		fasterWriter.setProperty("showXmlDeclaration", "true");
		fasterWriter.setProperty("allowBadURIs", "true");
		fasterWriter.setProperty("relativeURIs", "");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(baos, Charset.availableCharsets().get("UTF-8"));
		fasterWriter.write(m, osw, "");
		osw.flush();
		baos.flush();
		return baos.toString();
	}
	
	/**
	 * @param args cmd args
	 */
	public static void main(String[] args) {
		try {
			Class.forName(dbClass);
			Store store = SDBFactory.connectStore(SDBConnectionFactory.create(dbUrl, dbUser, dbPass), new StoreDesc(dbLayout, dbType));
			if(!StoreUtils.isFormatted(store)) {
				store.getTableFormatter().create();
			}
			Model jenaModel = SDBFactory.connectNamedModel(store, modelName);
			jenaModel.read(new ByteArrayInputStream(previousRDF.getBytes()), null, null);
			System.out.println("jenaModel:\n" + exportRdfToString(jenaModel) + "\n");
			
			Model incoming = ModelFactory.createMemModelMaker().createDefaultModel();
			incoming.read(new ByteArrayInputStream(incomingRDF.getBytes()), null, null);
			System.out.println("incoming:\n" + exportRdfToString(incoming) + "\n");
			
			Model subs = jenaModel.difference(incoming);
			System.out.println("subs:\n" + exportRdfToString(subs) + "\n");
			
			Model adds = incoming.difference(jenaModel);
			System.out.println("adds:\n" + exportRdfToString(adds) + "\n");
			
			jenaModel.remove(subs);
			System.out.println("jenaModel - post remove 'subs':\n" + exportRdfToString(jenaModel) + "\n");
			
			jenaModel.add(adds);
			System.out.println("jenaModel - post add 'adds':\n" + exportRdfToString(jenaModel) + "\n");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
