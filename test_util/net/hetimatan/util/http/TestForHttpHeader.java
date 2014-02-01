package net.hetimatan.util.http;

import java.io.IOException;
import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.http.HttpRequestHeader;
import junit.framework.TestCase;

public class TestForHttpHeader extends TestCase {

	public void testHello() {

	}

	public void testEncode001() throws IOException {
		HttpRequestHeader header = new HttpRequestHeader("key","value");
		CashKyoroFile output = new CashKyoroFile(512);
		header.encode(output.getLastOutput());

		output.seek(0);
		byte[] buffer = new byte[(int)output.length()];
		int len = output.read(buffer);
		String tag = new String(buffer, 0, len);
		assertEquals("key: value\r\n", tag);
	}

	public void testDecode001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512);
		base.addChunk("key:value\r\n".getBytes());
		MarkableFileReader reader = new MarkableFileReader(base, 512);

		HttpRequestHeader value = HttpRequestHeader.decode(reader);
		assertEquals("key", value.getKey());
		assertEquals("value", value.getValue());
	}

}
