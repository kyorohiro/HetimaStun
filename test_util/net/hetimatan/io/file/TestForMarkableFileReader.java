package net.hetimatan.io.file;

import java.io.IOException;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;

import junit.framework.TestCase;

public class TestForMarkableFileReader extends TestCase {

	public void testHello() {
		
	}

	public void test000() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512, 2);
		MarkableFileReader reader = new MarkableFileReader(base, 10);

		try {
			base.addChunk("0123456789abcdef".getBytes());
			base.seek(0);

			byte[] buffer = new byte[1];
			reader.read(buffer);
			assertEquals("0", new String(buffer, 0, 1));
		} finally {
			reader.close();
		}
	}

	public void test001() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512, 2);
		MarkableFileReader reader = new MarkableFileReader(base, 10);

		try {
			base.addChunk("0123456789abcdef".getBytes());
			base.seek(0);

			byte[] buffer = new byte[10];
			reader.read(buffer);
			assertEquals("0123456789", new String(buffer, 0, 10));
		} finally { 
			reader.close();
		}
	}

	public void test002() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512, 2);
		MarkableFileReader reader = new MarkableFileReader(base, 10);

		try {
			base.addChunk("0123456789abcdef".getBytes());
			base.seek(0);

			byte[] buffer = new byte[16];
			reader.read(buffer);
			assertEquals("0123456789abcdef", new String(buffer, 0, 16));
		} finally {
			reader.close();
		}
	}

	public void test003() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512, 2);
		MarkableFileReader reader = new MarkableFileReader(base, 10);

		try {
			base.addChunk("0123456789abcdef".getBytes());
			base.seek(0);

			byte[] buffer = new byte[16];
			reader.read(buffer);
			reader.seek(0);
			reader.read(buffer);
			assertEquals("0123456789abcdef", new String(buffer, 0, 16));
		} finally {
			reader.close();
		}
	}

	public void test004() throws IOException {
		CashKyoroFile base = new CashKyoroFile(512, 2);
		MarkableFileReader reader = new MarkableFileReader(base, 10);

		try {
			base.addChunk("0123".getBytes());
			base.seek(0);

			byte[] buffer = new byte[16];
			reader.read(buffer);
			reader.seek(0);
			reader.read(buffer);
			assertEquals("0123", new String(buffer, 0, 4));

			base.addChunk("456789abcdef".getBytes());
			reader.read(buffer);
			assertEquals("456789abcdef", new String(buffer, 0, 12));

			reader.seek(0);
			reader.read(buffer);
			assertEquals("0123456789abcdef", new String(buffer, 0, 16));

		} finally {
			reader.close();
		}
	}

}
