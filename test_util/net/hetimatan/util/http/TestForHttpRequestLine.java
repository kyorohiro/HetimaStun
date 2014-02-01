package net.hetimatan.util.http;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.http.HttpRequestLine;

import java.io.IOException;

import junit.framework.TestCase;

public class TestForHttpRequestLine extends TestCase {

	public void testHello() {

	}

	public void testEncode001() throws IOException {
		HttpRequestLine line = new HttpRequestLine(HttpRequestLine.GET, "/announce?a=b&b=c", HttpRequestLine.HTTP11);
		CashKyoroFile output = new CashKyoroFile(512);
		line.encode(output.getLastOutput());

		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("GET /announce?a=b&b=c HTTP/1.1\r\n", tag);
	}

	public void testDecode001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("GET /announce?a=b&b=c HTTP/1.1\r\n".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpRequestLine value = HttpRequestLine.decode(reader);
		assertEquals("GET", value.getMethod());
		assertEquals("/announce?a=b&b=c", HttpObject.createEncode(value.getRequestURI()));
		assertEquals("HTTP/1.1", value.getHttpVersion());
	}

}
