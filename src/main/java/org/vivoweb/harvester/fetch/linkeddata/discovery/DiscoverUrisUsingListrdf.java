/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.discovery;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vivoweb.harvester.fetch.linkeddata.util.XPathHelper;
import org.vivoweb.harvester.fetch.linkeddata.util.XPathHelper.XpathHelperException;
import org.vivoweb.harvester.fetch.linkeddata.util.http.HttpWorker;
import org.w3c.dom.Document;
import org.w3c.dom.Node; 

/**
 * Do the discovery using [vivo]/listrdf.
 * 
 * This does not work on a large VIVO installations, because listrdf won't bring back more than
 * 30,000 individuals per class.
 */
public class DiscoverUrisUsingListrdf extends BaseDiscoveryWorker {
	private static final Log log = LogFactory
			.getLog(DiscoverUrisUsingListrdf.class);

	private static final String XPATH_TO_INDIVIDUAL_URI = "//rdf:Description/@rdf:about";

	private final HttpWorker http;

	public DiscoverUrisUsingListrdf(Iterable<String> classUris, HttpWorker http) {
		super(classUris);
		this.http = http;
	}

	@Override
	protected Iterable<String> getUrisForClassAtSite(String siteUrl,
			String classUri) throws DiscoveryWorkerException {
		try {
			Document uriList = http.post(siteUrl + "/listrdf")
					.parameter("vclass", classUri).asXML().execute();
			Set<String> uris = parseUriList(uriList);
			if (uris.size() >= 30000) {
				log.error("Site '" + siteUrl + "' maxed out on 30,000 "
						+ "individual URIs for class '" + classUri + "'");
			}
			return uris;
		} catch (Exception e) {
			throw new DiscoveryWorkerException(
					"Can't continue. Failed to read the URLs for class '"
							+ classUri + "' at site '" + siteUrl + "'", e);
		}
	}

	private Set<String> parseUriList(Document uriListDoc)
			throws XpathHelperException {
		Set<String> uris = new HashSet<>();
		XPathHelper xp = XPathHelper.getHelper(XPathHelper.RDF_PREFIX);
		Node rootNode = uriListDoc.getDocumentElement();
	 
		   for (Node node : xp.findNodes(XPATH_TO_INDIVIDUAL_URI, rootNode)) {
			   uris.add(node.getNodeValue());
		   }
	  
		return uris;
	}
}
