package net.hetimatan.net.http.request;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.filen.ByteKyoroFile;
import net.hetimatan.util.http.HttpRequest;
import net.hetimatan.util.http.HttpRequestHeader;
import net.hetimatan.util.http.HttpRequestLine;
import net.hetimatan.util.http.HttpGetRequestUri;

public class HttpRequestBuilder {
	public static final String REQUEST_METHOD_GET = "GET";
	public static final String REQUEST_METHOD_POST = "POST";
	public static final String SCHEME_HTTP = "http";
	public static final String HTTP10 = "HTTP/1.0";
	public static final String HTTP11 = "HTTP/1.1";
	public static final String HEADER_HOST = "Host";
	public static final String USER_AGENT = "User-Agent";
	public static final String CONTENT_LENGTH = "Content-Length";

	private LinkedHashMap<String, String> mHeader = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> mValues = new LinkedHashMap<String, String>();

	private String mPath = "/";
	private int mPort = 80;
	private String mHost = "127.0.0.1";
	private String mHttpVersion = HTTP10;
	private KyoroFile mBody = null;

	
	public String getHost() {
		return mHost;
	}

	public int getPort() {
		return mPort;
	}

	public HttpRequestBuilder setPort(int port) {
		mPort = port;
		return this;
	}

	public HttpRequestBuilder setHttpVersion(String httpVersion) {
		mHttpVersion = httpVersion;
		return this;
	}
	public HttpRequestBuilder setHost(String host) {
		mHost = host;
		return this;
	}

	public HttpRequestBuilder setPath(String path) {
		mPath = path;
		return this;
	}

	public HttpRequestBuilder putValue(String key, String value) {
		mValues.put(key, value);
		return this;		
	}

	public HttpRequestBuilder putHeader(String key, String value) {
		mHeader.put(key, value);
		return this;
	}

	public HttpRequestBuilder putBody(KyoroFile body) {
		mBody = body;
		return this;
	}

	public synchronized HttpRequest createHttpGetRequest() throws IOException {
		HttpRequest request = HttpRequest
		.newInstance(REQUEST_METHOD_GET, mPath, HttpRequestLine.HTTP10);
		for (String key : mValues.keySet()) {
			request.putValue(key, mValues.get(key));
		}
		return createHttpRequest(request);
	}

	public synchronized HttpRequest createHttpPostRequest() throws IOException {
		HttpRequest request = HttpRequest
		.newInstance(REQUEST_METHOD_POST, mPath, HttpRequestLine.HTTP10);
		if(mBody == null) {
			ByteKyoroFile value = new ByteKyoroFile();
			for (String key : mValues.keySet()) {
				value.addChunk((""+key+": "+mValues.get(key)).getBytes());
			}
			request.setBody(value);
			mHeader.put(HttpRequestHeader.HEADER_CONTENT_LENGTH, ""+value.length());
		} else {
			request.setBody(mBody);
			mHeader.put(HttpRequestHeader.HEADER_CONTENT_LENGTH, ""+mBody.length());
		}
		return createHttpRequest(request);
	}

	private synchronized HttpRequest createHttpRequest(HttpRequest request) throws IOException {
		boolean haveHost = false;
		for (String key : mHeader.keySet()) {
			if(key.matches("[Hh][Oo][Ss][Tt]")){haveHost=true;}
			request.addHeader(key, mHeader.get(key));
		}
		request.getLine().setHttpVersion(mHttpVersion);
		HttpGetRequestUri uri = request.getLine().getRequestURI();
		uri.setHost(mHost);
		uri.setPort(mPort);
		if(!haveHost) {
			request.addHeader(HEADER_HOST, mHost);
		}
		return request;
	}

	public synchronized HttpGetRequestUri createHttpGetRequestUri() throws IOException {
		HttpGetRequestUri uri = HttpGetRequestUri.crateHttpGetRequestUri(mPath);
		for (String key : mValues.keySet()) {
			uri.putVale(key, mValues.get(key));
		}
		uri.setHost(mHost);
		uri.setPort(mPort);
		return uri;
	}
}
