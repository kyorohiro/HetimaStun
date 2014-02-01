package net.hetimatan.util.http;


import java.io.IOException;
import java.io.OutputStream;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.file.MarkableReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.io.filen.CashKyoroFile;

// KEY ":" VALUE CRLF
public class HttpRequestHeader extends HttpObject {

	private String mKey = "";
	private String mValue = "";
	public static final String SEPARATOR = ":";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_CONTENT_TyPE = "Content-Type";
	public static final String HEADER_LOCATION = "Location";
	public static final String HEADER_HOST = "Host";

	public HttpRequestHeader(String key, String value) {
		mKey = key;
		mValue = value;
	}

	@Override
	public void encode(OutputStream output) throws IOException {
		output.write(mKey.getBytes());
		output.write(SEPARATOR.getBytes());
		output.write(" ".getBytes());
		output.write(mValue.getBytes());
		output.write(CRLF.getBytes());
	}

	public String getKey() {
		return mKey;
	}

	public String getValue() {
		return mValue;
	}

	public static HttpRequestHeader decode(String path) throws IOException {
		CashKyoroFile vFile = new CashKyoroFile(512, 2);
		vFile.addChunk(path.getBytes());
		MarkableReader reader = new MarkableFileReader(vFile, 256);
		return HttpRequestHeader.decode(reader);
	}

	public static HttpRequestHeader decode(MarkableReader reader) throws IOException {
		String key = _key(reader);
		_spa(reader); _sep(reader);_spa(reader);
		String value = _val(reader);
		_crlf(reader);
		return new HttpRequestHeader(key, value);
	}

	private static String _key(MarkableReader reader) throws IOException {
		try {
			return new String(MarkableReaderHelper.asciiAndGet(reader,":".getBytes(), Integer.MAX_VALUE));
		} catch(IOException e) {
			throw e;
		}
	}

	private static String _val(MarkableReader reader) throws IOException {
		try {
			return new String(MarkableReaderHelper.asciiAndGet(reader, "".getBytes(), Integer.MAX_VALUE));
		} catch(IOException e) {
			throw e;
		}
	}

	private static void _spa(MarkableReader reader) throws IOException {
		MarkableReaderHelper.jumpPattern(reader, " ".getBytes(), 256);
	}
	private static void _sep(MarkableReader reader) throws IOException {
		if(reader.read() != ':' ) {
			throw new IOException();
		}
	}
	
}
