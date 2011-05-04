/******************************************************************************************************************************
 * Copyright (c) 2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the new BSD license which accompanies this
 * distribution, and is available at http://www.opensource.org/licenses/bsd-license.html
 * Contributors:
 * Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri
 * - initial API and implementation
 *****************************************************************************************************************************/
package org.vivoweb.harvester.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Christopher Haines (hainesc@ctrip.ufl.edu)
 */
public class WebAide {

	/**
	 * Get the contents of a url
	 * @param url the url to grab
	 * @return the contents
	 * @throws MalformedURLException invalid url
	 * @throws IOException error reading
	 */
	public static String getURLContents(String url) throws MalformedURLException, IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
		String s;
		while((s = br.readLine()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
}
