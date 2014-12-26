/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package org.vivoweb.harvester.fetch.linkeddata.util.http;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils; 
import org.vivoweb.harvester.fetch.linkeddata.util.http.BasicHttpWorkerRequest.StringHttpWorkerRequest;
import org.vivoweb.harvester.fetch.linkeddata.util.http.HttpWorkerRequest.Method;

/**
 * The basic implementation of the HttpClient.
 * 
 * Obtain a request object from get(String) or post(String). Use the methods on
 * HttpWorkerRequest to populate the request.
 * 
 * The request object keeps a reference to the HttpWorker that created it, and
 * when you call execute on the request, it will call back to its parent.
 */
public class BasicHttpWorker implements HttpWorker {
	private static final Charset utf8 = Charset.forName("UTF-8");

	private final HttpClient httpClient;

	public BasicHttpWorker(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public HttpWorkerRequest<String> get(String url) throws HttpWorkerException {
		return new StringHttpWorkerRequest(this, url, Method.GET);
	}

	@Override
	public HttpWorkerRequest<String> post(String url)
			throws HttpWorkerException {
		return new StringHttpWorkerRequest(this, url, Method.POST);
	}

	/**
	 * The request calls this method when it is ready for execution.
	 * 
	 * @throws HttpBadStatusException
	 *             if the response status is not 200 OK.
	 * @throws HttpWorkerException
	 *             on any other problem.
	 */
	protected String executeRequest(BasicHttpWorkerRequest<?> request)
			throws HttpWorkerException {
		HttpRequestBase hreq = (request.getMethod() == Method.GET) ? buildGetMethod(request)
				: buildPostMethod(request);

		try {
			HttpResponse hresp = httpClient.execute(hreq);
			try (InputStream stream = hresp.getEntity().getContent()) {
				String responseBody = (stream == null) ? "" : IOUtils.toString(
						stream, "UTF-8");
				if (hresp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					throw new HttpBadStatusException(request,
							hresp.getStatusLine(), responseBody);
				}
				return responseBody;
			}
		} catch (Exception e) {
			throw new HttpWorkerException(e);
		} finally {
			hreq.releaseConnection();
		}
	}

	private HttpPost buildPostMethod(BasicHttpWorkerRequest<?> request) {
		HttpPost post = new HttpPost(request.getUrlWithoutParameters());
		post.setEntity(new UrlEncodedFormEntity(request.getParameters(), utf8));
		applyAcceptTypes(post, request);
		return post;
	}

	private HttpGet buildGetMethod(BasicHttpWorkerRequest<?> request) {
		String qString = URLEncodedUtils.format(request.getParameters(), utf8);
		String bareUrl = request.getUrlWithoutParameters();
		HttpGet get = new HttpGet(bareUrl + '?' + qString);
		applyAcceptTypes(get, request);
		return get;
	}

	private void applyAcceptTypes(HttpRequest hreq,
			BasicHttpWorkerRequest<?> request) {
		List<String> acceptTypes = request.getAcceptTypes();
		if (!acceptTypes.isEmpty()) {
			String acceptable = StringUtils.join(acceptTypes, ", ");
			hreq.addHeader("Accept", acceptable);
		}
	}

}
