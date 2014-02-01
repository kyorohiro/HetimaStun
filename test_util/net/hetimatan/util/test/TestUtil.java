package net.hetimatan.util.test;

import java.io.IOException;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.util.url.PercentEncoder;
import junit.framework.TestCase;

public class TestUtil {

	public static void assertArrayEquals(TestCase testcase, String message, long[] expect, long[] target) {
//		message += ":e="+new String(expect) +":t="+ new String(target);
		System.out.println(message);
		for(int i=0;i<expect.length&&i<target.length;i++) {
			testcase.assertEquals(":["+i+"]"+((char)expect[i])+","+((char)target[i])+":"+message, expect[i], target[i]);
		}
		testcase.assertEquals(expect.length, target.length);
	}

	public static void assertArrayEquals(TestCase testcase, String message, byte[] expect, byte[] target) {
//		message += ":e="+new String(expect) +":t="+ new String(target);
		PercentEncoder encoder = new PercentEncoder();
		message += ":e="+encoder.encode(expect)
				+":t="+encoder.encode(target);
		System.out.println(message);
		for(int i=0;i<expect.length&&i<target.length;i++) {
			testcase.assertEquals(":["+i+"]"+((char)expect[i])+","+((char)target[i])+":"+message, expect[i], target[i]);
		}
		testcase.assertEquals(expect.length, target.length);
	}

	public static byte[] createBuffer(KyoroFile kf) throws IOException {
		long fp = kf.getFilePointer();
		try {
			kf.seek(0);
			byte[] buffer = new byte[(int)kf.length()];
			kf.read(buffer);
			return buffer;
		} finally {
			kf.seek(fp);
		}
	}

}
