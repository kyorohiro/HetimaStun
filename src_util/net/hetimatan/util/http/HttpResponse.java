package net.hetimatan.util.http;


import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.MarkableReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.io.filen.CashKyoroFile;

// http://www.studyinghttp.net/cgi-bin/rfc.cgi?2616
//http://www.w3.org/Protocols/rfc2616/rfc2616.html
//Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
public class HttpResponse extends HttpObject {

	public static final String STATUS_CODE_301_MOVE_PERMANENTLY = "301 Moved Permanently";
	public static final String STATUS_CODE_302_Found = "302 Found";
	public static final String STATUS_CODE_303_SEE_OTHER= "303 See Other";
	public static final String STATUS_CODE_304_NOT_MODIFIED= "304 Not Modified";
	public static final String STATUS_CODE_305_USE_PROXY= "305 Use Proxy";
	public static final String STATUS_CODE_307_TEMPORARY_REDIRECT= "307 Temporary Redirect";
	public static final String[] REDIRECT_STATUSCODE = {
		"301","302","303","307"
	};

	public static final byte[] available = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
		'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z',
		'.', '-', '/', '_', 
	};
	public static final byte[] available_pharse = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
		'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
		'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z',
		'.', '-', '/', '_', ' '
	};
	private LinkedList<HttpRequestHeader> mHeaders = new LinkedList<HttpRequestHeader>();
	private String mHttpVersion = null;
	private String mStatusCode = null;
	private String mReasonPharse = null;
	private KyoroFile mContent = null;
	private HttpRequestHeader mContentLength = null;

	public HttpResponse(String httpVersion, String statusCode,
			String reasonPhrase) {
		mHttpVersion = httpVersion;
		mStatusCode = statusCode;
		mReasonPharse = reasonPhrase;
	}

	public String getHttpVersion() {
		return mHttpVersion;
	}

	public String getStatusCode() {
		return mStatusCode;
	}

	public String getReasonPharse() {
		return mReasonPharse;
	}

	public KyoroFile getContent() {
		return mContent;
	}


	public void setContent(byte[] content) throws IOException {
		setContent(new CashKyoroFile(content));
	}

	public void setContent(KyoroFile content) {
		mContent = content;
	}

	public LinkedList<HttpRequestHeader> getHeader() {
		return mHeaders;
	}

	public String getHeader(String key) {
		for(HttpRequestHeader header : mHeaders) {
			if(key.replaceAll(" ", "").toLowerCase().equals(header.getKey().replaceAll(" ", "").toLowerCase())){
				return header.getValue();
			}
		}
		return "";
	}
	public HttpResponse addHeader(String key, String value) {
		return addHeader(new HttpRequestHeader(key, value));
	}

	public HttpResponse addHeader(HttpRequestHeader header) {
//		System.out.println("key="+header.getKey()+","+header.getValue());
		if (header.getKey().toLowerCase()
				.equals(HttpRequestHeader.HEADER_CONTENT_LENGTH.toLowerCase())) {
			mContentLength = header;
		}
		mHeaders.add(header);
		return this;
	}

	public long getContentSizeFromHeader() {
		if(mContentLength == null) {
			return Integer.MAX_VALUE;
		} 
		String value = mContentLength.getValue();
		try {
			return Integer.parseInt(value.replaceAll(" ", ""));
		} catch(Exception e) {
			return 0;
		}
	}

	@Override
	public void encode(OutputStream output) throws IOException {
		output.write((mHttpVersion + SP + mStatusCode + SP + mReasonPharse + CRLF)
				.getBytes());
		long contentLength = 0;
		if(mContent != null) {
			contentLength = mContent.length();
		}
		output.write(("" + HttpRequestHeader.HEADER_CONTENT_LENGTH + ": "
				+ contentLength + CRLF).getBytes());
		for (HttpRequestHeader header : mHeaders) {
			header.encode(output);
		}
		output.write(CRLF.getBytes());

		byte[] buffer = new byte[100];
		while (contentLength!=0) {
			int len = mContent.read(buffer);
			if (len < 0) {
				break;
			}
			output.write(buffer, 0, len);
		}
		output.flush();
	}

	// Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
	public static HttpResponse decode(MarkableReader reader, boolean isReadBody) throws IOException {
		String httpVersion = "";
		String statusCode = "";
		String reasonPhrase = "";

		httpVersion = _httpVersion(reader);
		_sp(reader);
		statusCode = _statusCode(reader);
		_sp(reader);
		reasonPhrase = _reasonPhrase(reader);
		_crlf(reader);
		HttpResponse ret = new HttpResponse(httpVersion, statusCode, reasonPhrase);

		
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

		long size = ret.getContentSizeFromHeader();
		if(size <0) {size = Integer.MAX_VALUE;}
		// 
		// todo MarkableReader内の VFを使用してもよいかも
		CashKyoroFile vf = new CashKyoroFile(512, 2);
		if(isReadBody) {
			int added = 0;
			byte[] buffer = new byte[1];
			while(true) {
				int value = reader.read();
				if(value<0||added>=size){
					break;
				}
				buffer[0] = (byte)(0xFF&value);
				vf.addChunk(buffer);
				added++;
			}
		}
		ret.setContent(vf);
		return ret;
	}

	public static String _httpVersion(MarkableReader reader) throws IOException {
		try {
			return new String(MarkableReaderHelper.jumpAndGet(reader, available, 256));
		} catch(IOException e) {
			throw e;
		}
	}

	public static String _statusCode(MarkableReader reader) throws IOException {
		try {
			return new String(MarkableReaderHelper.jumpAndGet(reader, available, 256));
		} catch(IOException e) {
			throw e;
		}
	}

	public static String _reasonPhrase(MarkableReader reader) throws IOException {
		try {
			return new String(MarkableReaderHelper.jumpAndGet(reader, available_pharse, 256));
		} catch(IOException e) {
			throw e;
		}
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
