package net.hetimatan.util.url;


import java.io.IOException;

import net.hetimatan.util.url.PercentEncoder;
import junit.framework.TestCase;

public class TestForPercentEncoder extends TestCase {

	public void testOne() throws IOException {
		PercentEncoder encoder = new PercentEncoder();
		String input = "あいうえお";
		String ret1 = encoder.encode(input.getBytes("utf8"));
		assertEquals("%e3%81%82%e3%81%84%e3%81%86%e3%81%88%e3%81%8a", ret1.toLowerCase());
		byte[] ret2 = encoder.decode(ret1.getBytes("utf8"));
		assertEquals(input.getBytes("utf8").length, ret2.length);
		for(int i=0;i<ret2.length;i++) {
			assertEquals("i:"+i,input.getBytes("utf8")[i], ret2[i]);
		}
	}

	public void testTwo() throws IOException {
		PercentEncoder encoder = new PercentEncoder();
		String input = "aあいうえおd";
		String ret1 = encoder.encode(input.getBytes("utf8"));
		assertEquals("a%e3%81%82%e3%81%84%e3%81%86%e3%81%88%e3%81%8ad", ret1.toLowerCase());
		byte[] ret2 = encoder.decode(ret1.getBytes("utf8"));
		assertEquals(input.getBytes("utf8").length, ret2.length);
		for(int i=0;i<ret2.length;i++) {
			assertEquals("i:"+i,input.getBytes("utf8")[i], ret2[i]);
		}
	}

	public void testThree() throws IOException {
		PercentEncoder encoder = new PercentEncoder();
		String input = "0あいうzえお9";
		String ret1 = encoder.encode(input.getBytes("utf8"));
		assertEquals("0%e3%81%82%e3%81%84%e3%81%86z%e3%81%88%e3%81%8a9", ret1.toLowerCase());
		byte[] ret2 = encoder.decode(ret1.getBytes("utf8"));
		assertEquals(input.getBytes("utf8").length, ret2.length);
		for(int i=0;i<ret2.length;i++) {
			assertEquals("i:"+i,input.getBytes("utf8")[i], ret2[i]);
		}
	}

	public void testFour() throws IOException {
		PercentEncoder encoder = new PercentEncoder();
		String input = "%0あいうzえお9%";
		String ret1 = encoder.encode(input.getBytes("utf8"));
		assertEquals("%250%e3%81%82%e3%81%84%e3%81%86z%e3%81%88%e3%81%8a9%25", ret1.toLowerCase());
		byte[] ret2 = encoder.decode(ret1.getBytes("utf8"));
		assertEquals(input.getBytes("utf8").length, ret2.length);
		for(int i=0;i<ret2.length;i++) {
			assertEquals("i:"+i,input.getBytes("utf8")[i], ret2[i]);
		}
	}
}
