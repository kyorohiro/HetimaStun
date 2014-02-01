package net.hetimatan.io.file;

import java.io.IOException;

public interface MarkableReader {
	public static final int EOF = -1;
	public static final int STOCK_IS_NONE = -2;

	// mark
	public void pushMark();
	public long popMark();
	public void backToMark();

	// read
	public int peek() throws IOException;
	public int read() throws IOException;
	public int read(byte[] out, int start, int len) throws IOException;
	
	// fp
	public void seek(long pos) throws IOException;	
	public long getFilePointer() throws IOException;

	// block
	public boolean setBlockOn(boolean on);
	public int waitForUnreadable(int timeout) throws IOException;

	//other
	public long length() throws IOException;
	public void close() throws IOException;

	// option
	public boolean isEOF() throws IOException;

}
