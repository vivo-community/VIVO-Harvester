package org.vivoweb.harvester.fetch.linkeddata.util.http;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.vivoweb.harvester.fetch.linkeddata.util.RdfUtils;
import org.vivoweb.harvester.fetch.linkeddata.util.xml.XmlUtils;
import org.vivoweb.harvester.fetch.linkeddata.util.xml.XmlUtils.XmlUtilsException;

import com.hp.hpl.jena.rdf.model.Model; 

/**
 * TODO
 */
public abstract class BasicHttpWorkerRequest<T> implements HttpWorkerRequest<T> {
	private static final Charset utf8 = Charset.forName("UTF-8");

	private final BasicHttpWorker worker;
	private final String url;
	private final Method method;

	private List<String> acceptTypes = new ArrayList<>();
	private List<NameValuePair> parameters = new ArrayList<>();
	private String urlWithoutParameters;

	public BasicHttpWorkerRequest(BasicHttpWorker worker, String url,
			Method method) throws HttpWorkerException {
		this.worker = worker;
		this.url = url;
		this.method = method;
		normalizeParameters();
	}

	/** Copy constructor. Base has already been normalized. */
	protected BasicHttpWorkerRequest(BasicHttpWorkerRequest<?> base) {
		this.worker = base.worker;
		this.url = base.url;
		this.method = base.method;
		this.parameters = base.parameters;
		this.urlWithoutParameters = base.urlWithoutParameters;
		this.acceptTypes = base.acceptTypes;
	}

	@Override
	public BasicHttpWorkerRequest<T> parameter(String name, Object value) {
		parameters.add(new BasicNameValuePair(name, String.valueOf(value)));
		return this;
	}

	@Override
	public BasicHttpWorkerRequest<T> accept(Object acceptType) {
		acceptTypes.add(String.valueOf(acceptType));
		return this;
	}

	public String getUrl() {
		return url;
	}

	public Method getMethod() {
		return method;
	}

	public String getUrlWithoutParameters() {
		return urlWithoutParameters;
	}

	public List<NameValuePair> getParameters() {
		return parameters;
	}

	public List<String> getAcceptTypes() {
		return acceptTypes;
	}

	@Override
	public HttpWorkerRequest<String> asString() {
		return new StringHttpWorkerRequest(this);
	}

	@Override
	public HttpWorkerRequest<org.w3c.dom.Document> asXML() {
		return new XmlHttpWorkerRequest(this);
	}

	@Override
	public HttpWorkerRequest<org.jsoup.nodes.Document> asHtml() {
		return new JSoupHttpWorkerRequest(this);
	}

	@Override
	public HttpWorkerRequest<Model> asModel() {
		return new JenaHttpWorkerRequest(this);
	}

	@Override
	public T execute() throws HttpWorkerException {
		return processResponse(worker.executeRequest(this));
	}

	protected abstract T processResponse(String string)
			throws HttpWorkerException;

	/**
	 * Move parameters from the URL, so they will all be in the same place.
	 */
	@SuppressWarnings("unused")
	private void normalizeParameters() throws HttpWorkerException {
		// If it's going to throw an exception, we want to know now.
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			throw new HttpWorkerException("url is invalid: '" + url + "'", e);
		}

		String[] parts = url.split("\\?");
		this.urlWithoutParameters = parts[0];

		if (parts.length == 1) {
			return;
		}

		if (Method.POST == method) {
			throw new HttpWorkerException("URLs for POST methods may not "
					+ "include parameters: '" + url + "'");
		}

		parameters.addAll(URLEncodedUtils.parse(parts[1], utf8));
	}

	@Override
	public String toString() {
		return "BasicHttpWorkerRequest [url=" + url + ", method=" + method
				+ ", acceptTypes=" + acceptTypes + ", parameters=" + parameters
				+ ", urlWithoutParameters=" + urlWithoutParameters + "]";
	}

	// ----------------------------------------------------------------------
	// Specialized sub-classes
	// ----------------------------------------------------------------------

	static class StringHttpWorkerRequest extends BasicHttpWorkerRequest<String> {
		protected StringHttpWorkerRequest(BasicHttpWorkerRequest<?> base) {
			super(base);
		}

		public StringHttpWorkerRequest(BasicHttpWorker worker, String url,
				Method method) throws HttpWorkerException {
			super(worker, url, method);
		}

		@Override
		protected String processResponse(String string) {
			return string;
		}
	}

	static class XmlHttpWorkerRequest extends
			BasicHttpWorkerRequest<org.w3c.dom.Document> {
		protected XmlHttpWorkerRequest(BasicHttpWorkerRequest<?> base) {
			super(base);
		}

		@Override
		protected org.w3c.dom.Document processResponse(String string)
				throws HttpWorkerException {
			try {
				return XmlUtils.parseXml(string);
			} catch (XmlUtilsException e) {
				throw new HttpWorkerException(
						"Couldn't convert the response stream to XML.", e);
			}
		}
	}

	static class JSoupHttpWorkerRequest extends
			BasicHttpWorkerRequest<org.jsoup.nodes.Document> {
		protected JSoupHttpWorkerRequest(BasicHttpWorkerRequest<?> base) {
			super(base);
		}

		@Override
		protected org.jsoup.nodes.Document processResponse(String string) {
			return Jsoup.parse(string, getUrlWithoutParameters());
		}
	}

	static class JenaHttpWorkerRequest extends BasicHttpWorkerRequest<Model> {
		protected JenaHttpWorkerRequest(BasicHttpWorkerRequest<?> base) {
			super(base);
		}

		@Override
		protected Model processResponse(String string)
				throws HttpWorkerException {
			return RdfUtils.toModel(string);
		}
	}

}
