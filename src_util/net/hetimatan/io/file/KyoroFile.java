package net.hetimatan.io.file;

import java.io.IOException;

public interface KyoroFile {
	public long getFilePointer();

	public void seek(long point) throws IOException;

	public long length() throws IOException;

	public int waitForUnreadable(int timeout) throws IOException;

	public int read(byte[] buffer) throws IOException;

	public int read(byte[] buffer, int start, int buffLen) throws IOException;

	public void close() throws IOException;

	public void addChunk(byte[] buffer, int begin, int end) throws IOException;

	public void addChunk(byte[] buffer) throws IOException;

	public void syncWrite() throws IOException;

}
