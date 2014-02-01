package net.hetimatan.io.filen;


import java.io.IOException;
import java.util.LinkedList;

import net.hetimatan.io.file.KyoroFile;

//
// todo:RACashFileと同じコードがある。 
// RACashFileの一部をKyoroFileとして取得する。
// 独自にキャッシュをもてる。 
public class ReferenceModifierKyoroFile implements KyoroFile {
	private KyoroFile mRAFile = null;
	private LinkedList<ByteKyoroFile> mCash = null;
	private long mFilePointer = 0;
	private long mStart = 0;
	private long mEnd = 0;
	private int mChunkSize = 0;
	private boolean mIsCash = false;

	public ReferenceModifierKyoroFile(KyoroFile f, long start, long end, int chunkSize, int chunkNum) throws IOException {		
		mChunkSize = chunkSize;
		mRAFile = f;
		mIsCash = false;
		mStart = start;
		mEnd = end;
		mCash = new LinkedList<ByteKyoroFile>();
		for(int i=0;i<chunkNum;i++) {
			ByteKyoroFile cash = new ByteKyoroFile(mChunkSize, mChunkSize);
			cash.skip(mStart+i*chunkSize);
			cash.update(mRAFile);
			mCash.add(cash);
		}
	}

	public void cash(boolean on) {
		mIsCash = on;
	}

	public boolean cash() {
		return mIsCash;
	}

	@Override
	public void close() throws IOException {
	}
	
	@Override
	public long getFilePointer() {
		return mFilePointer;
	}

	@Override
	public void seek(long point) throws IOException {
		mFilePointer = point;
	}

	@Override
	public long length() throws IOException {
		return mEnd-mStart;
	}

	@Override
	public int waitForUnreadable(int timeout) throws IOException {
		return 1;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(byte[] buffer, int start, int buffLen) throws IOException {
  		int len = 0;
		long remain = length() - getFilePointer();
		if (remain<buffLen) {
			buffLen = (int)remain;
		}
		if (remain<=0) {
			return -1;
		}
		do {
			ByteKyoroFile file = getCash();
			int _len = buffLen - len;
			if (_len>file.getBuffer().length-(getFilePointer()-file.skip()+mStart)) {
				_len = (int)(file.getBuffer().length-(getFilePointer()-file.skip()+mStart));
			}
			System.arraycopy(file.getBuffer(), (int)(getFilePointer()-file.skip()+mStart),
					buffer, len+start, _len);
			len += _len;
			mFilePointer +=_len;
		} while(len<buffLen);
		return buffLen;
	}

	public ByteKyoroFile getCash() throws IOException {
		int size = mCash.size();
		long fp = getFilePointer()+mStart;
		for(int i=0;i<size;i++){
			ByteKyoroFile cash = mCash.get(i);
			if(cash.skip()<=fp&&fp<(cash.skip()+mChunkSize)){
				return cash;
			}
		}
		ByteKyoroFile ret = mCash.remove(0);
		mCash.addLast(ret);
		ret.reset(mStart+getFilePointer()-(getFilePointer()%mChunkSize));
		ret.update(mRAFile);
		return ret;
	}
	@Override
	public void addChunk(byte[] buffer, int begin, int end) throws IOException {}
	@Override
	public void addChunk(byte[] buffer) throws IOException {}
	@Override
	public void syncWrite() throws IOException {}

}