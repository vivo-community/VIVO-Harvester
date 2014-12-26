package org.vivoweb.harvester.fetch.linkeddata.discovery;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 
/**
 * Get all the URIs for a given site.
 * 
 * INPUT: This mapper expects Text inputs that are URLs of sites. No keys are
 * expected. ex. [ "http://vivo.cornell.edu" ]
 * 
 * OUTPUT: URIs of individuals from the sites that should be added to the index.
 * The key should be the site URL and the value should be the URI of an
 * individual from that site. ex. [ "http://vivo.cornell.edu" :
 * "http://vivo.cornell.edu/indiviudal134" ... ]
 */
public abstract class BaseUriDiscovery {
	Log log = LogFactory.getLog(BaseUriDiscovery.class);

	protected final DiscoveryWorker uriSource;

	public BaseUriDiscovery(DiscoveryWorker uriSource) {
		this.uriSource = uriSource;
	} 
}
