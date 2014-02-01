package net.hetimatan.util.http;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.http.HttpGetRequestUri;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.TestCase;

public class TestForHttpRequestUri  extends TestCase {

	public void testHello() {

	}

	public void testDecodeFragment() throws IOException {
		{
			MarkableFileReader reader = new MarkableFileReader("#nononono".getBytes());
			assertEquals("#nononono", HttpGetRequestUri.fragment(reader));
			reader.close();
		}	
	}

	public void testDecodePath() throws IOException {
		{
			MarkableFileReader reader = new MarkableFileReader("/test/xxx.html".getBytes());
			assertEquals("/test/xxx.html", HttpGetRequestUri.path(reader));
			reader.close();
		}

	}
	public void testDecodePort() throws IOException {
		{
			MarkableFileReader reader = new MarkableFileReader("8080".getBytes());
			assertEquals(8080, HttpGetRequestUri.port(reader));
			reader.close();
		}


		{
			MarkableFileReader reader = new MarkableFileReader("808080808080808080808080".getBytes());
			try {
				HttpGetRequestUri.port(reader);
				assertTrue(false);
			} catch(NumberFormatException e) {
				assertEquals(0, reader.getFilePointer());
			} finally {
				reader.close();
			}

		}
	}

	public void testDecodeHost() throws IOException {
		{
			MarkableFileReader reader = new MarkableFileReader("127.0.0.1".getBytes());
			assertEquals("127.0.0.1", HttpGetRequestUri.host(reader));
			reader.close();
		}

		{
			MarkableFileReader reader = new MarkableFileReader("xxx.yyyy.xxx:".getBytes());
			assertEquals("xxx.yyyy.xxx", HttpGetRequestUri.host(reader));
			reader.close();
		}

		{
			MarkableFileReader reader = new MarkableFileReader(":xxx.yyyy.xxx:".getBytes());
			assertEquals("", HttpGetRequestUri.host(reader));
			reader.close();
		}
	}

	public void testDecodeScheme() throws IOException {
		{
			MarkableFileReader reader = new MarkableFileReader("http".getBytes());
			assertEquals("http", HttpGetRequestUri.scheme(reader));
			reader.close();
		}

		{
			MarkableFileReader reader = new MarkableFileReader("https".getBytes());
			assertEquals("https", HttpGetRequestUri.scheme(reader));
			reader.close();
		}

		{
			MarkableFileReader reader = new MarkableFileReader("htt".getBytes());
			try {
			assertEquals("htt", HttpGetRequestUri.scheme(reader));
			assertTrue(false);
			} catch(IOException e) {
				
			}
			assertEquals(0, reader.getFilePointer());
		}
	}

	public void testEncode001() throws IOException {
		HttpGetRequestUri uri = new HttpGetRequestUri("*");
		CashKyoroFile output = new CashKyoroFile(512);
		uri.encode(output.getLastOutput());
		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("*", tag);
	}

	public void testEncode002() throws IOException {
		HttpGetRequestUri uri = new HttpGetRequestUri("/announce");
		uri.putVale("01", "value01");

		CashKyoroFile output = new CashKyoroFile(512);
		uri.encode(output.getLastOutput());
		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("/announce?01=value01", tag);
	}

	public void testEncode003() throws IOException {
		HttpGetRequestUri uri = new HttpGetRequestUri("/announce");
		uri.putVale("01", "value01");
		uri.putVale("02", "value02");
		uri.putVale("03", "value03");
		CashKyoroFile output = new CashKyoroFile(512);
		uri.encode(output.getLastOutput());
		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("/announce?01=value01&02=value02&03=value03", tag);
	}

	public void testDecode001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("*".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpGetRequestUri value = HttpGetRequestUri.decode(reader);
		assertEquals("*", value.getPath());
	}

	public void testDecode002() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("/announce?01=value01".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpGetRequestUri value = HttpGetRequestUri.decode(reader);
		assertEquals("/announce", value.getPath());
		Iterator<String> keys = value.keySet().iterator();
		assertEquals("/announce", value.getPath());
		String key = keys.next();
		assertEquals("01", key);
		assertEquals("value01", value.getValue(key));
		assertEquals(1, value.keySet().size());
	}

	public void testDecode003() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("/announce?01=value01&02=value02&03=value03".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpGetRequestUri value = HttpGetRequestUri.decode(reader);
		assertEquals("/announce", value.getPath());

		Iterator<String> keys = value.keySet().iterator();
		{
			String key = keys.next();
			assertEquals("01", key);
			assertEquals("value01", value.getValue(key));
		}
		{
			String key = keys.next();
			assertEquals("02", key);
			assertEquals("value02", value.getValue(key));
		}
		{
			String key = keys.next();
			assertEquals("03", key);
			assertEquals("value03", value.getValue(key));
		}
		{
			assertEquals(3, value.keySet().size());
		}
	}
}
