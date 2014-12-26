/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.discovery;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

/**
 * Get the Individual URIs for each requested Class URI on the specified Site.
 */
public abstract class BaseDiscoveryWorker implements DiscoveryWorker {
	protected final Iterable<String> classUris;

	protected BaseDiscoveryWorker(Iterable<String> classUris) {
		this.classUris = classUris;
	}

	/**
	 * Discover individual URIs at this site.
	 */
	@Override
	public final Iterable<String> getUrisForSite(String siteUrl)
			throws DiscoveryWorkerException {
		List<Iterable<String>> iterables = new ArrayList<>();

		for (String classUri : classUris) {
			try {
				iterables.add(getUrisForClassAtSite(siteUrl, classUri));
			} catch (Exception e) {
				throw new DiscoveryWorkerException(
						"Failed to discover individuals for class '" + classUri
								+ "' at '" + siteUrl + "'", e);
			}
		}
		return Iterables.concat(iterables);
	}

	/**
	 * Discover individual URIs for each Class URI at this site.
	 */
	protected abstract Iterable<String> getUrisForClassAtSite(String siteUrl,
			String classUri) throws DiscoveryWorkerException;
}
