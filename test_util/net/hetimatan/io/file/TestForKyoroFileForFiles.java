package net.hetimatan.io.file;

import java.io.IOException;
import java.util.Random;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.KyoroFileForFiles;
import net.hetimatan.io.filen.ByteKyoroFile;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.io.ByteArrayBuilder;

import junit.framework.TestCase;

public class TestForKyoroFileForFiles extends TestCase {

	public void testHello() {
		
	}

	public void testOne001() throws IOException {
		CashKyoroFile[] files = new CashKyoroFile[1];
		files[0] = new CashKyoroFile("あいう".getBytes());
		KyoroFileForFiles kff = new KyoroFileForFiles(files);
		byte[] buffer = new byte[256];
		int len = kff.read(buffer);
		assertEquals("あいう".getBytes().length, len);
		assertEquals("あいう", new String(buffer, 0, len));
	}

	public void testOne002() throws IOException {
		byte[] testdata = new byte[256*1024-1];
		for(int i=0;i<testdata.length;i++){
			testdata[i] = (byte)Math.random();
		}
		CashKyoroFile[] files = new CashKyoroFile[1];
		files[0] = new CashKyoroFile(testdata);
		KyoroFileForFiles kff = new KyoroFileForFiles(files);
		byte[] buffer = new byte[256];
		int len = kff.read(buffer);
		assertEquals(256, len);
		for(int i=0;i<256;i++) {
			assertEquals(testdata[i], buffer[i]);
		}

		
		buffer = new byte[testdata.length];
		kff.seek(0);
		len = kff.read(buffer);
		assertEquals(buffer.length, len);
		for(int i=0;i<buffer.length;i++) {
			assertEquals(testdata[i], buffer[i]);
		}
	}

	public void testTwo001() throws IOException {
		CashKyoroFile[] files = new CashKyoroFile[2];
		files[0] = new CashKyoroFile("あいう".getBytes());
		files[1] = new CashKyoroFile("かきく".getBytes());
		KyoroFileForFiles kff = new KyoroFileForFiles(files);
		byte[] buffer = new byte[256];
		int len = kff.read(buffer);
		assertEquals("あいうかきく".getBytes().length, len);
		assertEquals("あいうかきく", new String(buffer, 0, len));
	}

	public void testTwo002() throws IOException {
		CashKyoroFile[] files = new CashKyoroFile[3];
		files[0] = new CashKyoroFile("あいう".getBytes());
		files[1] = new CashKyoroFile("かきく".getBytes());
		files[2] = new CashKyoroFile("さ".getBytes());
		KyoroFileForFiles kff = new KyoroFileForFiles(files);
		try {
			byte[] buffer = new byte[256];
			int len = kff.read(buffer);
			assertEquals("あいうかきくさ".getBytes().length, len);
			assertEquals("あいうかきくさ", new String(buffer, 0, len));
		} finally {
			kff.close();
		}
	}

	public void testTwo001_ByteKyoroFile() throws IOException {
		KyoroFile[] files = new KyoroFile[2];
		ByteArrayBuilder s0 = new ByteArrayBuilder();
		s0.append("ab".getBytes());
		files[0] = new ByteKyoroFile(s0);
		files[1] = new CashKyoroFile("cdefgh".getBytes());
		KyoroFileForFiles kff = new KyoroFileForFiles(files);
		byte[] buffer = new byte[4];
		{
			int len = kff.read(buffer);
			assertEquals(4, len);
			assertEquals("abcd", new String(buffer, 0, len));
		}
		{
			int len = kff.read(buffer);
			assertEquals(4, len);
			assertEquals("efgh", new String(buffer, 0, len));
		}

	}

	public void testOne003() throws IOException {
		byte[] testdata = new byte[4*256*1024-1];
		Random ra = new Random();
		for (int i=0;i<testdata.length;i++) {
			testdata[i] = (byte)(ra.nextInt()%128);
		}

		CashKyoroFile[] files = new CashKyoroFile[1];
		files[0] = new CashKyoroFile(testdata);
		KyoroFileForFiles kff = new KyoroFileForFiles(files);
		byte[] buffer = new byte[1024];

		int j=0;
		do {
			kff.read(buffer);
			for(int i=0;i<1024&&j<testdata.length;i++) {
				assertEquals(testdata[j], buffer[i]);
				j++;
			}
		} while(j<testdata.length);
	}

}
