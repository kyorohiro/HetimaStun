package net.hetimatan.io.file;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import net.hetimatan.io.filen.CashKyoroFile;

//todo
public class KyoroFileForFiles implements KyoroFile {

	private CashKyoroFile mVf = null;
	private KyoroFile[] mFiles = null;

	public static KyoroFileForFiles create(LinkedList<File> findFiles) throws IOException {
		KyoroFile[] tmp = new KyoroFile[findFiles.size()];
		int i=0;
		for(File f:findFiles) {
			tmp[i] = new CashKyoroFile(f,512, 2);
			i++;
		}
		return new KyoroFileForFiles(tmp);
	}
	public KyoroFileForFiles(KyoroFile[] files) throws IOException {
		mVf = new CashKyoroFile(1024, 2);
		mFiles = new KyoroFile[files.length];
		System.arraycopy(files, 0, mFiles, 0, mFiles.length);
	}

	public CashKyoroFile getVF() {
		return mVf;
	}

	@Override
	public long getFilePointer() {
		return mVf.getFilePointer();
	}

	@Override
	public void seek(long point) throws IOException {
		mVf.seek(point);
	}

	private long mLength = -1;
	@Override
	public long length() throws IOException {
		if(mLength==-1) {
			mLength = 0;
			for(KyoroFile f:mFiles){
				mLength += f.length();
			}
		}
		return mLength;
//		return mVf.length();
	}

	private byte[] mChunk = new byte[256];
	private int mCurrentFile = 0;

	private synchronized void addChunk(int length) throws IOException {
		if (mVf.length() >= mVf.getFilePointer() + length) {
			return;
		}
		if (mCurrentFile >= mFiles.length) {
			// no test
			return;
		}
		int added = 0;
		int len = 0;
		while (true) {
			len = mChunk.length;
			if (length - added < len) {
				len = length - added;
				//todo add
				if(len<=0) {
					break;
				}
			}

			len = mFiles[mCurrentFile].read(mChunk);
			if (len <= 0) {
				mFiles[mCurrentFile].close();
				mCurrentFile++;
				if (mCurrentFile >= mFiles.length) {
					break;
				} else {
					continue;
				}
			}
//			System.out.println("len="+len);
			mVf.addChunk(mChunk, 0, len);
			added += len;
		}
	}

	@Override
	public synchronized int read(byte[] buffer) throws IOException {
		addChunk(buffer.length);
		return mVf.read(buffer);
	}

	@Override
	public synchronized int read(byte[] buffer, int start, int buffLen) throws IOException {
		addChunk(buffLen);
		return mVf.read(buffer, start, buffLen);
	}

	@Override
	public void close() throws IOException {
		mVf.close();
		for(KyoroFile file : mFiles) {
			if (file != null) {
				file.close();
			}
		}
	}

	@Override
	public void addChunk(byte[] buffer, int begin, int end) throws IOException {
		mVf.addChunk(buffer, begin, end);
	}

	@Override
	public void addChunk(byte[] buffer) throws IOException {
		mVf.addChunk(buffer);
	}

	@Override
	public void syncWrite() throws IOException {
		mVf.syncWrite();
	}

	@Override
	public int waitForUnreadable(int timeout) throws IOException {
		if(mCurrentFile >= mFiles.length) {
			return -1;
		} else {
			return 1;
		}
	}


}
