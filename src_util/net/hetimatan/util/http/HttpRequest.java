package net.hetimatan.util.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.MarkableReader;
import net.hetimatan.io.filen.CashKyoroFileHelper;

//http://www.w3.org/Protocols/rfc2616/rfc2616.html
//Request-URI    = "*" | absoluteURI | abs_path | authority
//Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
public class HttpRequest extends HttpObject {

	private HttpRequestLine mLine = null;
	private LinkedList<HttpRequestHeader> mHeaders = new LinkedList<HttpRequestHeader>();
	private KyoroFile mBody = null;

	public static HttpRequest newInstance(String method, String requestUri, String httpVersion) {
		HttpRequestLine line = new HttpRequestLine(method, requestUri, httpVersion);
		return new HttpRequest(line);
	}

	public HttpRequest(HttpRequestLine line) {
		mLine = line;
	}

	public HttpRequestLine getLine() {
		return mLine;
	}

	public LinkedList<HttpRequestHeader> getHeader() {
		return mHeaders;
	}

	public HttpRequest putValue(String key, String value) {
		mLine.putValue(key, value);
		return this;
	}

	public String getValue(String key) {
		return mLine.getRequestURI().getValue(key);
	}

	public String getHeaderValue(String key) {
		for(HttpRequestHeader h:mHeaders) {
			if (key.equals(h.getKey())) {
				return h.getValue();
			}
		}
		return "";
	}

	public HttpRequest addHeader(String key, String value) {
		return addHeader(new HttpRequestHeader(key, value));
	}

	public HttpRequest addHeader(HttpRequestHeader header) {
		mHeaders.add(header);
		return this;
	}

	public void setBody(KyoroFile body) {
		mBody = body;
	}

	public KyoroFile getBody() {
		return mBody;
	}

	@Override
	public void encode(OutputStream output) throws IOException {
		mLine.encode(output);
		for (HttpRequestHeader header : mHeaders) {
			header.encode(output);
		}
		output.write(CRLF.getBytes());
		if(mBody != null) {
			CashKyoroFileHelper.write(output, mBody);
		}
	}

	@Override
	public String toString() {
		try {
			return HttpObject.createEncode(this);
		} catch (IOException e) {
			return "httprequest: failed";
		}
	}

	public static HttpRequest decode(MarkableReader reader) throws IOException {
		HttpRequestLine line = HttpRequestLine.decode(reader);
		HttpRequest ret = new HttpRequest(line);
		try {
			while (true) {
				if (isCrlf(reader)) {
					break;
				}
				ret.addHeader(HttpRequestHeader.decode(reader));
			}
		} catch (IOException e) {
		}
		_crlf(reader);
		return ret;
	}

	public static boolean isCrlf(MarkableReader reader) {
		reader.pushMark();
		try {
			_crlf(reader);
			return true;
		} catch (IOException e) {
		} finally {
			reader.backToMark();
			reader.popMark();
		}
		return false;
	}

}
