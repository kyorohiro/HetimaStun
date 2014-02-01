package net.hetimatan.io.file;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import net.hetimatan.io.filen.CashKyoroFile;

public class MarkableFileReader implements MarkableReader {
	public static final int TIMEOUT = 30000; 
	private KyoroFile mFile = null;
	private long mFilePointer = 0;

	private LinkedList<Long> mMark = new LinkedList<Long>();
	private boolean mLogOn =  false;
	private boolean mBlockOn = true;

	public boolean setBlockOn(boolean on) {
		boolean tmp = mBlockOn;
		mBlockOn = on;
		return tmp;
	}

	public void pushMark() {
		try {
			mMark.addLast(getFilePointer());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void backToMark() {
		try {
			if(0<mMark.size()) {
				long fp = mMark.get(mMark.size()-1);
				mFilePointer = fp;
				seek(fp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public long popMark() {
		if(mMark.size()>0) {
			return mMark.removeLast();
		} else {
			System.out.println("warning popMark");
			return mFilePointer;
		}
	}

	public int markSize() {
		return mMark.size();
	}

	public MarkableFileReader(byte[] buffer) throws IOException {
		mFile = new CashKyoroFile(buffer);
	}

	public MarkableFileReader(File path, int cashSize) throws IOException {
		mFile = new CashKyoroFile(path, cashSize, 2);
	}

	public MarkableFileReader(KyoroFile base, int cashSize) throws FileNotFoundException {
		mFile = base;
	}

	public int peek() throws IOException {
		long pointer = getFilePointer();
		int ret = read();
		seek(pointer);
		return ret;
	}

	public void seek(long pos) throws IOException {
		mFilePointer = pos;
	}

	public long getFilePointer() throws IOException {
		return mFilePointer;
	}

	public long length() throws IOException {
		return mFile.length();
	}

	private boolean mBaseFileIsClosedWhenCallClose = true;
	public void baseFileIsClosedWhenCallClose(boolean isClose) {
		mBaseFileIsClosedWhenCallClose = isClose;
	}

	public void close() throws IOException {
		if(mBaseFileIsClosedWhenCallClose) {
			mFile.close();
		}
	}

	@Override
	public int waitForUnreadable(int timeout) throws IOException {
		long len = this.length();
		long fp = this.getFilePointer();
		if(0<(len-fp)) {
			return 1; 
		} else {
			return mFile.waitForUnreadable(timeout);
		}
	}

	//
	// this method is blocking communocation
	public int read() throws IOException {
		try {
			int ret = -1;
			byte[] buffer = new byte[1];
			boolean first = true;
			do {
				mFile.seek(mFilePointer);
				ret = mFile.read(buffer);
				if(mBlockOn== false || first==false||ret != 0) {break;}
				first = false;
				waitForUnreadable(TIMEOUT);
			} while(true);
			if(ret>0) {
				seek(mFilePointer + 1);
			}
			if(ret<0) {
				mIsEOF = true;
				return -1;
			}
			if(ret==0) {
				return -2;
			}
			return 0xFF & buffer[0];
		} catch (IOException e) {
			mIsEOF = true;
			throw e;
		}
	}

	@Override
	public int read(byte[] out, int start, int len) throws IOException {

		try {
			int ret = 0;
			boolean first = true;
			do {
				mFile.seek(mFilePointer);
				ret = mFile.read(out, start, len);
				if(mBlockOn== false || first==false||ret != 0) {break;}
				first = false;
				waitForUnreadable(TIMEOUT);
			} while(true);
			if(ret>0) {
				mFilePointer += ret;
			}
			if(ret<0) {
				mIsEOF = true;
			} else {
				mIsEOF = false;
			}
			return ret;
		} catch (IOException e) {
			mIsEOF = true;
			throw e;
		}
	}

	public int read(byte[] out) throws IOException {
		return read(out, 0, out.length);
	}

	private boolean mIsEOF = false;
	@Override
	public boolean isEOF() throws IOException {
		return mIsEOF;
	}


}
