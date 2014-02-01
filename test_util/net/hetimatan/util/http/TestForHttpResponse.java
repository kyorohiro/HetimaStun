package net.hetimatan.util.http;


import java.io.IOException;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.http.HttpRequestLine;
import net.hetimatan.util.http.HttpResponse;
import junit.framework.TestCase;

public class TestForHttpResponse extends TestCase {

	public void testEncode001_emptyBody() throws IOException {
		String httpVersion = HttpRequestLine.HTTP11;
		String statusCode = "200";
		String reasonPhrase = "OK";
		HttpResponse response = new HttpResponse(httpVersion, statusCode,
				reasonPhrase);
		CashKyoroFile output = new CashKyoroFile(512);
		response.encode(output.getLastOutput());
		output.seek(0);
		byte[] buffer = new byte[(int) output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n", tag);
	}

	public void testEncode002_Body() throws IOException {
		String httpVersion = HttpRequestLine.HTTP11;
		String statusCode = "200";
		String reasonPhrase = "OK";
		HttpResponse response = new HttpResponse(httpVersion, statusCode,
				reasonPhrase);
		String content = "hello world\r\n";
		response.setContent(content.getBytes());
		CashKyoroFile output = new CashKyoroFile(512);
		response.encode(output.getLastOutput());
		output.seek(0);
		byte[] buffer = new byte[(int) output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("HTTP/1.1 200 OK\r\nContent-Length: "
				+ content.getBytes().length + "\r\n\r\nhello world\r\n", tag);
	}

	public void testDecode001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpResponse response = HttpResponse.decode(reader, true);
		assertEquals("HTTP/1.1", response.getHttpVersion());
		assertEquals("200", response.getStatusCode());
		assertEquals("OK", response.getReasonPharse());
		assertEquals(0, response.getContent().length());
	}

	public void testDecode002() throws IOException {
		String content = "hello world\r\n";
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk(("HTTP/1.1 200 OK\r\nContent-Length: "
		+content.getBytes().length
		+"\r\n\r\n"+content).getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpResponse response = HttpResponse.decode(reader, true);
		assertEquals("HTTP/1.1", response.getHttpVersion());
		assertEquals("200", response.getStatusCode());
		assertEquals("OK", response.getReasonPharse());
		assertEquals(content.getBytes().length, response.getContent().length());
	}


	public void testDecode003() throws IOException {
		String content = "hello world\r\n";
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk(("HTTP/1.1 200 OK\r\nContent-Length: "
		+content.getBytes().length
		+"\r\n\r\n"+content).getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpResponse response = HttpResponse.decode(reader, false);
		assertEquals("HTTP/1.1", response.getHttpVersion());
		assertEquals("200", response.getStatusCode());
		assertEquals("OK", response.getReasonPharse());
		assertEquals(0, response.getContent().length());
	}

}
