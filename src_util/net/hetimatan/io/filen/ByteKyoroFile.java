package net.hetimatan.io.filen;


import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import net.hetimatan.io.file.KyoroByteOutput;
import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.filen.CashKyoroFile.MyOutputStream;
import net.hetimatan.util.io.ByteArrayBuilder;

public class ByteKyoroFile implements KyoroFile, KyoroByteOutput {

	private long mFilePointer = 0;
	private long mLength = 0;
	private ByteArrayBuilder mBuffer = new ByteArrayBuilder();
	private long mSkip = 0;
	private int mLimit = 256;
	private boolean mUpdate = false;
	
	public ByteKyoroFile() {
		this(256, Integer.MAX_VALUE);
	}

	public ByteKyoroFile(ByteArrayBuilder buffer) {
		mLimit  = Integer.MAX_VALUE;
		mBuffer = buffer;
		mLength = buffer.length();
	}

	public ByteKyoroFile(int size, int limit) {
		mLimit = limit;
		mBuffer = new ByteArrayBuilder(size);		
	}

	public byte[] getBuffer() {
		return mBuffer.getBuffer();
	}

	public boolean isUpdated() {
		return mUpdate;
	}

	@Deprecated
	public void update(RandomAccessFile file) throws IOException {
		int length = mBuffer.getBuffer().length;
		byte[] buffer = mBuffer.getBuffer();
		long fp = file.getFilePointer();
		try {
			file.seek(mSkip);
			int len = file.read(buffer, 0, length);
			if (len>=0) {
				mBuffer.setPointer(len);
			}
		} finally {
			file.seek(fp);
		}
	}

	public void update(KyoroFile file) throws IOException {
		int length = mBuffer.getBuffer().length;
		byte[] buffer = mBuffer.getBuffer();
		long fp = file.getFilePointer();
		try {
			file.seek(mSkip);
			int len = file.read(buffer, 0, length);
			if (len>=0) {
				mBuffer.setPointer(len);
			}
		} finally {
			file.seek(fp);
		}
	}

	public boolean have(int fp) {
		long len = stockedLen();
		if(mSkip<=fp&&fp<(mSkip+len)) {
			return true;
		} else { 
			return false;
		}
	}
	@Override
	public long getFilePointer() {
		return mFilePointer;
	}

	public long skip() {
		return mSkip;
	}

	public void reset(long skip) throws IOException {
		mSkip = skip;
		mLength = mSkip;
		mFilePointer = mSkip;
	}
	public void skip(long skip) throws IOException {
		long prev = mSkip;
		long skipsize = skip-prev; 
		if(skipsize<0) {
			throw new IOException("skipsize=skip("+skip+")-("+prev+"),skipsize<0");
		} else {
			mBuffer.shift((int)skipsize);
		}
		mSkip = skip;
	}

	@Override
	public void seek(long point) throws IOException {
		if(point>(mLimit+mSkip)) {
			throw new IOException(point+">"+mLimit+"+"+mSkip);
		}
		mFilePointer = point;
		updateLength();
	}

	@Override
	public long length() throws IOException {
		return mLength;
	}

	public long Limit() {
		return mSkip+mLimit;
	}
	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int start, int buffLen) throws IOException {
		byte[] inlineBuffer= mBuffer.getBuffer();
		long remain = length()-getFilePointer();
		if(buffLen<0) {
			throw new IOException("illegal");
		}
		if(remain<buffLen) {
			buffLen = (int)remain;
		}
		System.arraycopy(inlineBuffer, (int)(mFilePointer-mSkip), buffer, start, buffLen);
		mFilePointer+=buffLen;
		return buffLen;
	}


	@Override
	public void addChunk(byte[] buffer, int begin, int end) throws IOException {
		long fp = getFilePointer();
		try {
			seek(length());
			write(buffer, begin, end-begin);
		} finally {
			seek(fp);
		}
	}


	@Override
	public void addChunk(byte[] buffer) throws IOException {
		addChunk(buffer, 0, buffer.length);
	}

	@Override
	public int write(int b) throws IOException {
		byte[] buffer = new byte[1];
		buffer[0] = (byte)(0xFF&b);
		return write(buffer, 0, 1);
	}

	@Override
	public int write(byte[] buffer, int start, int len) throws IOException {
		int bufferPoint = (int)(mFilePointer-mSkip);
		if(bufferPoint<0) {
			throw new IOException();
		}
		if((bufferPoint)>(mLimit+mSkip)) {
			throw new IOException(bufferPoint+"+"+1+">"+mLimit+"+"+mSkip);
		}
		mUpdate = true;
		int skippedFP = (int)(mFilePointer-mSkip);
		int neededLength = bufferPoint+len;
		mBuffer.setBufferLength(neededLength);
		mBuffer.setPointer(neededLength);

		byte[] inlineBuffer = mBuffer.getBuffer();
		for(int i=0;i<len;i++) {
			inlineBuffer[skippedFP+i] = buffer[start+i];
		}
		mFilePointer+=len;

		updateLength();
		return len;
	}

	public int stockedLen() {
		int len = (int)(mLength-mSkip);
		if(len>(mBuffer.getBuffer().length)) {
			len = (int)(mBuffer.getBuffer().length);
		}
		if(len<0) {
			len = 0;
		}
		return len;
	}


	public void writeTo(RandomAccessFile file) throws IOException {
		long fp = file.getFilePointer();
		try {
			file.seek(mSkip);
			int len = stockedLen();
			file.write(mBuffer.getBuffer(), 0, len);
			mUpdate = false;
		} finally {
			file.seek(fp);
		}
	}

	private void updateLength() {
		if(mFilePointer>mLength) {
			mLength=mFilePointer;
		}		
	}

	@Override
	public int waitForUnreadable(int timeout) throws IOException {return 1;}
	@Override
	public void syncWrite() throws IOException {}

	@Override
	public int write(byte[] data) throws IOException {
		return write(data, 0, data.length);
	}

	private OutputStream mLastOutput = null;
	public OutputStream getLastOutput() {
		if(mLastOutput == null) {
			mLastOutput = new MyOutputStream();
		}
		return mLastOutput;
	}

	public class MyOutputStream extends OutputStream {
		private byte[] buffer = new byte[1];
		@Override
		public synchronized void write(int b) throws IOException {
			KyoroFile file = ByteKyoroFile.this;
			buffer[0] = (byte)(0xFF&b);
			file.addChunk(buffer);
		}
		
	}

/*
	@Override
	public int write(byte[] buffer, int start, int len) throws IOException {
		int writed= 0;
		int i=start;
        for (; i < start + len; i++) {
        	writed = write((int)(0xFF&buffer[i]));
        	if(writed<0) {
        		break;
        	}
        }
        return i-start;
	}


	@Override
	public int write(int b) throws IOException {
		int bufferPoint = (int)(mFilePointer-mSkip);
		if(bufferPoint<0) {
			throw new IOException();
		}
		if((bufferPoint)>(mLimit+mSkip)) {
			throw new IOException(bufferPoint+"+"+1+">"+mLimit+"+"+mSkip);
		}
		mUpdate = true;
		int neededLength = bufferPoint+1;
		mBuffer.setBufferLength(neededLength);
		byte[] inlineBuffer = mBuffer.getBuffer();

		inlineBuffer[bufferPoint] = (byte)(0xFF&b);
		mBuffer.setPointer(neededLength);
		mFilePointer+=1;
		updateLength();
		return 1;
	}

*/
	@Override
	public void close() throws IOException {
		mBuffer.clear();
	}
}