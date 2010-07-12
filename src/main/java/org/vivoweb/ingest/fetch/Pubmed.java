package org.vivoweb.ingest.fetch;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.vivoweb.ingest.util.RecordHandler;
import org.vivoweb.ingest.util.Task;
import org.vivoweb.ingest.util.XMLRecordOutputStream;
import org.xml.sax.SAXException;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 *
 */
public class Pubmed extends Task {
	
	private String emailAddress;
	private String location;
	private RecordHandler rh;
	private String searchTerm;
	private String maxRecords;
	private OutputStream os;
	private Integer intBatchSize;
	
	@Override
	protected void acceptParams(Map<String, String> params) throws ParserConfigurationException, SAXException, IOException {
		this.emailAddress = getParam(params, "emailAddress", true);
		this.location = getParam(params, "location", true);
		String repositoryConfig = getParam(params, "repositoryConfig", true);
		this.searchTerm = getParam(params, "searchTerm", true);
		this.maxRecords = getParam(params, "maxRecords", true);
		this.rh = RecordHandler.parseConfig(repositoryConfig);
		this.rh.setOverwriteDefault(true);
		this.os = new XMLRecordOutputStream("PubmedArticle", "<?xml version=\"1.0\"?>\n<!DOCTYPE PubmedArticleSet PUBLIC \"-//NLM//DTD PubMedArticle, 1st January 2010//EN\" \"http://www.ncbi.nlm.nih.gov/corehtml/query/DTD/pubmed_100101.dtd\">\n<PubmedArticleSet>\n", "\n</PubmedArticleSet>", ".*?<PMID>(.*?)</PMID>.*?", this.rh);
		this.intBatchSize = 1000;
	}
	
	@Override
	protected void runTask() throws NumberFormatException {
		PubmedSOAPFetch f = new PubmedSOAPFetch(this.emailAddress, this.location, this.os);
		Integer recToFetch;
		if(this.maxRecords.equalsIgnoreCase("all")) {
			recToFetch = Integer.valueOf(f.getHighestRecordNumber());
		} else {
			recToFetch = Integer.valueOf(this.maxRecords);
		}
		if(recToFetch.intValue() <= intBatchSize) {
			f.fetchPubMedEnv(f.ESearchEnv(this.searchTerm, recToFetch));
		} else {
			String[] envInfo = f.ESearchEnv(this.searchTerm, recToFetch);
			String WebEnv = envInfo[0];
			String QueryKey = envInfo[1];
			String idListLength = envInfo[2];
			Integer.parseInt(idListLength);
			for(int x = recToFetch.intValue(); x > 0; x-=intBatchSize) {
				int maxRec = (x<=intBatchSize) ? x : intBatchSize;
				int startRec = recToFetch.intValue() - x;
				System.out.println("maxRec: "+maxRec);
				System.out.println("startRec: "+startRec);
				f.fetchPubMedEnv(WebEnv, QueryKey, startRec+"", maxRec+"");
			}
		}
	}
	
}
