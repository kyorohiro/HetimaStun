package net.hetimatan.util.http;


import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Set;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.file.MarkableReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.io.filen.CashKyoroFile;

// GET request
// http://www.w3schools.com/tags/ref_httpmethods.asp
// Request-URI    = "*" | absoluteURI | abs_path
public class HttpGetRequestUri extends HttpObject {
	private String mPath = "";
	private String mHost = "";
	private int mPort = 80;

	private LinkedHashMap<String, String> mValues = new LinkedHashMap<String, String>();

	// todo throw IOEXception?
	public static HttpGetRequestUri crateHttpGetRequestUri(String requestPath) {
		try {
			CashKyoroFile base = new CashKyoroFile(requestPath.getBytes());
			MarkableReader reader  = new MarkableFileReader(base, 100);
			return HttpGetRequestUri.decode(reader);
		} catch(Exception e) {
			return new HttpGetRequestUri(requestPath);
		}
	}

	public HttpGetRequestUri(String path) {
		mPath = path;
	}

	@Override
	public String toString() {
		try {
			String ret = HttpObject.createEncode(this);
			return ret;
		} catch (IOException e) {
			return "#Failed toString#";
		}
	}

	public void setHost(String host) {
		mHost = host;
	}

	public void setPort(int port) {
		mPort = port;
	}

	public void putVale(String key, String value) {
		mValues.put(key, value);
	}

	public Set<String> keySet() {
		return mValues.keySet();
	}

	//
	// todo
	public void setPath(String path) {
		if(path.startsWith("/")) {
			mPath = path;
		} else {
			mPath = "/"+path;
		}
	}

	public String getPath() {
		return mPath;
	}

	public String getValue(String key) {
		return mValues.get(key);
	}

	private boolean mAbsPath = true;
	public void IsAbsolutePath(boolean on) {
		mAbsPath = !on;
	}
	//
	// /test/demo_form.asp?name1=value1&name2=value2
	//
	@Override
	public void encode(OutputStream output) throws IOException {
		if(mAbsPath) {
			encodeAbsPath(output);
		} else {
			encodeAbsolutePath(output);
		}
	}

	public void encodeAbsolutePath(OutputStream output) throws IOException {
		output.write(("http://"+mHost+":"+mPort).getBytes());
		encodeAbsPath(output);
	}

	public void encodeAbsPath(OutputStream output) throws IOException {
		output.write(mPath.getBytes());
		Set<String> keys = mValues.keySet();
		boolean isFirst = true;
		for (String key : keys) {
			if (true == isFirst) {
				isFirst = false;
				output.write("?".getBytes());
			} else {
				output.write("&".getBytes());
			}
			output.write(key.getBytes());
			output.write("=".getBytes());
			output.write(mValues.get(key).getBytes());
		}
	}

	public static HttpGetRequestUri decode(String location) throws IOException {
		MarkableFileReader reader = null;
		try {
			reader = new MarkableFileReader(location.getBytes());
			HttpGetRequestUri geturi = HttpGetRequestUri.decode(reader);
			return geturi;
		} finally {
			reader.close();
		}
	}
	public static HttpGetRequestUri decode(MarkableReader reader) throws IOException {
		try {
			return astarisk(reader);
		} catch(IOException e) {
		}
		try {
			return absoluteUri(reader);
		} catch(IOException e) {
		}
		try {
			return absPath(reader);
		} catch(IOException e) {
			throw e;
		}
	}

	
	public String getHost() {
		return mHost;
	}
	public int getPort() {
		return mPort;
	}

	public String getMethod() {
		CashKyoroFile vFile = null;
		try {
			vFile = new CashKyoroFile(512, 2);
			encodeAbsPath(vFile.getLastOutput());
			byte[] buffer = new byte[(int)vFile.length()];
			int len = vFile.read(buffer);
			return new String(buffer, 0, len);
		} catch(IOException e) {
		} finally {
			try {
				vFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return mPath;
	}

	public static HttpGetRequestUri astarisk(MarkableReader reader) throws IOException  {
		MarkableReaderHelper.match(reader, "*".getBytes());
		HttpGetRequestUri ret = new HttpGetRequestUri("*");
		return ret;
	}

	public static HttpGetRequestUri absPath(MarkableReader reader) throws IOException {
		String path = "/";
		try {
			path = path(reader);
		} catch(IOException e) {
			;
		}
		
		HttpGetRequestUri ret = new HttpGetRequestUri(path);

		try {
			MarkableReaderHelper.match(reader, "?".getBytes());
			query(reader, ret);
		} catch(IOException e) {
		}
		
		try {
			fragment(reader);
		} catch(IOException e) {
		}
		
		return ret;
	}

	public static HttpGetRequestUri absoluteUri(MarkableReader reader) throws IOException {
		int port = 80;
		String path = "/";
		scheme(reader); 
		MarkableReaderHelper.match(reader, "://".getBytes());
		String host = host(reader);
		try {
			MarkableReaderHelper.match(reader, ":".getBytes());
			port = port(reader);
		} catch(IOException e) {
		}
		try {
			path = path(reader);
		} catch(IOException e) {
			;
		}
		
		HttpGetRequestUri ret = new HttpGetRequestUri(path);
		ret.mHost = host;
		ret.mPort = port;

		try {
			MarkableReaderHelper.match(reader, "?".getBytes());
			query(reader, ret);
		} catch(IOException e) {
		}
		
		try {
			fragment(reader);
		} catch(IOException e) {
		}
		
		return ret;
	}

	public static String fragment(MarkableReader reader) throws IOException {
		try {
			MarkableReaderHelper.match(reader, "#".getBytes());
			return "#" + new String(MarkableReaderHelper.asciiAndGet(reader, "".getBytes(), 256));
		} catch(IOException e) {
			throw e;
		}
	}

	public static void query(MarkableReader reader, HttpGetRequestUri uri) throws IOException {
		boolean first = true;
		do {
			try {
				reader.pushMark();
				if(!first) {
					MarkableReaderHelper.match(reader, "&".getBytes());
				} else {
					first = false;
				}
				String key = new String(MarkableReaderHelper.asciiAndGet(reader, "=".getBytes(), Integer.MAX_VALUE));
				MarkableReaderHelper.match(reader, "=".getBytes());
				String value = new String(MarkableReaderHelper.asciiAndGet(reader, "& ".getBytes(), Integer.MAX_VALUE));
				uri.putVale(key, value);
			} catch(IOException e) {
				reader.backToMark();
				break;
			} finally {
				reader.popMark();				
			}
		} while(true);
	}

	public static String path(MarkableReader reader) throws IOException {
		try {
			MarkableReaderHelper.match(reader, "/".getBytes());
			return "/"+new String(MarkableReaderHelper.asciiAndGet(reader, "? ".getBytes(), Integer.MAX_VALUE));//jumpAndGet(reader, available, 256));
		} catch(IOException e) {
			throw e;
		}
	}

	public static int port(MarkableReader reader) throws IOException, NumberFormatException {
		final byte[] available= {
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
		};

		try {
			reader.pushMark();
			return Integer.parseInt(new String(MarkableReaderHelper.jumpAndGet(reader, available, 256)));
		} catch(IOException e) {
			reader.backToMark();
			throw e;
		} catch(NumberFormatException e) {
			reader.backToMark();
			throw e;
		} finally  {
			reader.popMark();
		}
	}

	public static String host(MarkableReader reader) throws IOException {
		final byte[] available= {
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
				'u', 'v', 'w', 'x', 'y', 'z',
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
				'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
				'U', 'V', 'W', 'X', 'Y', 'Z',
				'.', '-'
		};

		try {
			return new String(MarkableReaderHelper.jumpAndGet(reader, available, 256));
		} catch(IOException e) {
			throw e;
		}
	}

	public static String scheme(MarkableReader reader) throws IOException {
		try {
			MarkableReaderHelper.match(reader, ("https").getBytes());
			return "https";
		} catch(IOException e) {
		}
		try {
			MarkableReaderHelper.match(reader, ("http").getBytes());
			return "http";
		} catch(IOException e) {
			throw e;
		}
	}


	public static HttpGetRequestUri createHttpRequestUri(String location) throws IOException {
		MarkableFileReader reader = null;
		try {
			reader = new MarkableFileReader(location.getBytes());
			HttpGetRequestUri geturi = HttpGetRequestUri.decode(reader);
			return geturi;
		} finally {
			reader.close();
		}
	}
}