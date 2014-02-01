package net.hetimatan.util.http;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.http.HttpRequestHeader;
import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.http.HttpRequestLine;
import net.hetimatan.util.http.HttpRequest;
import java.io.IOException;

import junit.framework.TestCase;

public class TestForHttpRequest extends TestCase {

	public void testEncode001() throws IOException {
		HttpRequest uri = new HttpRequest(
				HttpRequestLine.decode("GET /announce HTTP/1.1\r\n"));
		CashKyoroFile output = new CashKyoroFile(512);
		uri.encode(output.getLastOutput());

		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("GET /announce HTTP/1.1\r\n\r\n", tag);
	}

	public void testEncode002() throws IOException {
		HttpRequest uri = new HttpRequest(HttpRequestLine.decode("GET /announce HTTP/1.1\r\n"));
		uri.addHeader(HttpRequestHeader.decode("User-agent: Mozilla/5.0 (Linux; U; Android 2.2.1; ja-jp; Full Android Build/MASTER) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\r\n"));
		CashKyoroFile output = new CashKyoroFile(512);
		uri.encode(output.getLastOutput());

		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("GET /announce HTTP/1.1\r\n"
		+"User-agent: Mozilla/5.0 (Linux; U; Android 2.2.1; ja-jp; Full Android Build/MASTER) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\r\n"
		+"\r\n", tag);
	}

	public void testEncode003() throws IOException {
		HttpRequest uri = new HttpRequest(HttpRequestLine.decode("GET /announce HTTP/1.1\r\n"));
		uri.addHeader(HttpRequestHeader.decode("Accept-encoding: gzip\r\n"));
		uri.addHeader(HttpRequestHeader.decode("Host: 127.0.0.1:6969\r\n"));
		uri.addHeader(HttpRequestHeader.decode("User-agent: BitTorrent/4.0.2\r\n"));
		CashKyoroFile output = new CashKyoroFile(512);
		uri.encode(output.getLastOutput());

		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("GET /announce HTTP/1.1\r\n"
				+"Accept-encoding: gzip\r\n"
				+"Host: 127.0.0.1:6969\r\n"
				+"User-agent: BitTorrent/4.0.2\r\n"
				+"\r\n", tag);
	}


	public void testDecode001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("GET /announce HTTP/1.1\r\n\r\n".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpRequest uri = HttpRequest.decode(reader);
		HttpRequestLine line = uri.getLine();
		assertEquals("GET", line.getMethod());
		assertEquals("/announce", HttpObject.createEncode(line.getRequestURI()));
		assertEquals("HTTP/1.1", line.getHttpVersion());
		assertEquals(0, uri.getHeader().size());
	}

	public void testDecode002() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		String inputUrl = "GET /announce HTTP/1.1\r\n"
				+"Accept-encoding: gzip\r\n"
				+"Host: 127.0.0.1:6969\r\n"
				+"User-agent: BitTorrent/4.0.2\r\n"
				+"\r\n";
		base.addChunk(inputUrl.getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpRequest uri = HttpRequest.decode(reader);
		HttpRequestLine line = uri.getLine();
		assertEquals("GET", line.getMethod());
		assertEquals("/announce", HttpObject.createEncode(line.getRequestURI()));
		assertEquals("HTTP/1.1", line.getHttpVersion());
		assertEquals(3, uri.getHeader().size());
		assertEquals("gzip", uri.getHeader().get(0).getValue());
		assertEquals("127.0.0.1:6969", uri.getHeader().get(1).getValue());
		assertEquals("BitTorrent/4.0.2", uri.getHeader().get(2).getValue());
		assertEquals("Accept-encoding", uri.getHeader().get(0).getKey());
		assertEquals("Host", uri.getHeader().get(1).getKey());
		assertEquals("User-agent", uri.getHeader().get(2).getKey());

	}


	public void testDecodeError001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("GET /announce HTTP/1.1\r\n".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		try {
			HttpRequest.decode(reader);
			assertTrue(false);
		} catch(IOException e) {
		}
	}

}
