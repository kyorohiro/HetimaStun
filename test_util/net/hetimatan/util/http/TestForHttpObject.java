package net.hetimatan.util.http;

import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.io.ByteArrayBuilder;

import java.io.IOException;

import junit.framework.TestCase;

public class TestForHttpObject  extends TestCase {

	public void testHello() {

	}

	public void testEncode001() throws IOException {
		byte[] ipAsBytes = HttpObject.aton("127.0.0.1");
		int ipAsInt = ByteArrayBuilder.parseInt(ipAsBytes, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		String address = HttpObject.ntoa(ipAsInt);
		for(byte b:ipAsBytes) {
			System.out.println(""+b);
		}
		System.out.println(""+ipAsInt);
		System.out.println(""+address);
		assertEquals(2130706433, ipAsInt);
		assertEquals("127.0.0.1", address);
	}
}
