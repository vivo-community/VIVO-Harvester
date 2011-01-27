import java.sql.SQLException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.tdb.TDBFactory;

/** */
public class RegexQueryFailInTDBvsSDB {
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
	
	/**
	 * @param args cmdline args
	 * @throws ClassNotFoundException error
	 * @throws SQLException error
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Model jena;
		boolean tdbMode = true;
		if(tdbMode) {
			jena = TDBFactory.createModel("tempDir");
		} else {
			Class.forName(dbClass);
			Store store = SDBFactory.connectStore(SDBConnectionFactory.create(dbUrl, dbUser, dbPass), new StoreDesc(dbLayout, dbType));
			if(!StoreUtils.isFormatted(store)) {
				store.getTableFormatter().create();
			}
			jena = SDBFactory.connectNamedModel(store, modelName);
		}
		jena.removeAll();
		Property label = jena.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
		Resource res1 = jena.createResource("http://test.my.tld/testRegex/item#1");
		Resource res2 = jena.createResource("http://test.my.tld/testRegex/item#2");
		Resource res3 = jena.createResource("http://test.my.tld/testRegex/item#3");
		Resource res4 = jena.createResource("http://test.my.tld/testRegex/item#4");
		Resource res5 = jena.createResource("http://test.my.tld/testRegex/item#5");
		Resource res6 = jena.createResource("http://test.my.tld/testRegex/item#6");
		Resource res7 = jena.createResource("http://test.my.tld/testRegex/item#7");
		Resource res8 = jena.createResource("http://test.my.tld/testRegex/item#8");
		String searchValue = "IATMRR";
		String replaceWithValue = "I Am Testing Multi Regex Replace";
		jena.add(res1, label, searchValue+" "+searchValue);
		jena.add(res2, label, "woo"+searchValue+"blah"+searchValue+"quak");
		jena.add(res3, label, "I A T M R R and I A T M R R");
		jena.add(res4, label, searchValue+"bob "+searchValue);
		jena.add(res5, label, searchValue+" heh"+searchValue);
		jena.add(res6, label, "hmm "+searchValue+" "+searchValue);
		jena.add(res7, label, "wut ok"+searchValue+" lol "+searchValue);
		jena.add(res8, label, "odd "+searchValue+searchValue+" fun");
		String expectedValue1 = replaceWithValue+" "+replaceWithValue;
		String expectedValue2 = "woo"+replaceWithValue+"blah"+replaceWithValue+"quak";
		String expectedValue3 = "I A T M R R and I A T M R R";
		String expectedValue4 = replaceWithValue+"bob "+replaceWithValue;
		String expectedValue5 = replaceWithValue+" heh"+replaceWithValue;
		String expectedValue6 = "hmm "+replaceWithValue+" "+replaceWithValue;
		String expectedValue7 = "wut ok"+replaceWithValue+" lol "+replaceWithValue;
		String expectedValue8 = "odd "+replaceWithValue+replaceWithValue+" fun";
		// call qualify
		String query = "" +
			"SELECT ?s ?o \n" +
			"WHERE {\n" +
			"  ?s <" + label.getURI() + "> ?o .\n" +
			"  FILTER (regex(str(?o), \"" + searchValue + "\", \"s\")) .\n" +
			"}";
		System.out.println(query);
		ResultSet rs = ResultSetFactory.copyResults(QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), jena).execSelect());
		QuerySolution s;
		while(rs.hasNext()) {
			s = rs.next();
			Literal obj = s.getLiteral("o");
			RDFDatatype datatype = obj.getDatatype();
			String lang = obj.getLanguage();
			String objStr = obj.getValue().toString();
			System.out.println("Replacing record");
			System.out.println("oldValue: " + obj.toString());
			String newStr = objStr.replaceAll(searchValue, replaceWithValue);
			Literal newObj;
			if(datatype != null) {
				newObj = jena.createTypedLiteral(newStr, datatype);
			} else if(!lang.equals("")) {
				newObj = jena.createLiteral(newStr, lang);
			} else {
				newObj = jena.createLiteral(newStr);
			}
			System.out.println("newValue: " + newObj.toString());
			System.out.println();
			jena.remove(s.getResource("s"), label, obj);
			jena.add(s.getResource("s"), label, newObj);
		}
		assertEquals(expectedValue1, jena.getProperty(res1, label).getString());
		assertEquals(expectedValue2, jena.getProperty(res2, label).getString());
		assertEquals(expectedValue3, jena.getProperty(res3, label).getString());
		assertEquals(expectedValue4, jena.getProperty(res4, label).getString());
		assertEquals(expectedValue5, jena.getProperty(res5, label).getString());
		assertEquals(expectedValue6, jena.getProperty(res6, label).getString());
		assertEquals(expectedValue7, jena.getProperty(res7, label).getString());
		assertEquals(expectedValue8, jena.getProperty(res8, label).getString());
	}
	
	/**
	 * @param expectedValue expected 
	 * @param trueValue true
	 */
	private static void assertEquals(String expectedValue, String trueValue) {
		if(!trueValue.equals(expectedValue)) {
			System.out.println("Expected: '"+expectedValue+"', but got '"+trueValue+"'");
		}
	}
	
}
