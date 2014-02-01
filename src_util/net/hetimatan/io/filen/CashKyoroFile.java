package net.hetimatan.io.filen;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import net.hetimatan.io.file.KyoroByteOutput;
import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.util.log.Log;

//
public class CashKyoroFile implements KyoroFile, KyoroByteOutput {
	public static final String TAG ="RACashFile";
	private RandomAccessFile mRAFile = null;
	private File mFile = null;
	private LinkedList<ByteKyoroFile> mCash = null;
	private long mFilePointer = 0;
	private long mLength = 0;
	private int mChunkSize = 0;
	private boolean mIsCash = false;
	private ByteKyoroFile mCurrent = null;

	public CashKyoroFile(byte[] data) throws IOException {
		this(CashKyoroFileHelper.newCashFile(), data.length, 2);
		addChunk(data);
	}

	public CashKyoroFile(int chunkSize) throws IOException {
		this(CashKyoroFileHelper.newCashFile(), chunkSize, 2);
		mIsCash = true;
	}

	public CashKyoroFile(int chunkSize, int chunkNum) throws IOException {
		this(CashKyoroFileHelper.newCashFile(), chunkSize, chunkNum);
		mIsCash = true;
	}


	public CashKyoroFile(File f, int chunkSize, int chunkNum) throws IOException {		
		mChunkSize = chunkSize;
		mFile = f;
		if(mFile.exists()) {
			mRAFile = new RandomAccessFile(mFile = f, "rw");
		}
		mIsCash = false;
		mCash = new LinkedList<ByteKyoroFile>();
		mLength = f.length();
		for(int i=0;i<chunkNum;i++) {
			ByteKyoroFile cash = new ByteKyoroFile(mChunkSize, mChunkSize);
			cash.skip(i*chunkSize);
			if(mRAFile != null) {
				cash.update(mRAFile);
			}
			mCash.add(cash);
		}
		mCurrent = mCash.get(0);
	}

	public void isCashMode(boolean on) {
		mIsCash = on;
	}

	public boolean isCashMode() {
		return mIsCash;
	}

	@Override
	public void close() throws IOException {
		if(mRAFile != null) {
			if(Log.ON){Log.v(TAG, "close");}
			mRAFile.close();
		}
		if(mIsCash) {
//			mFile.deleteOnExit();
			mFile.delete();
		}
	}
	
	@Override
	public long getFilePointer() {
		return mFilePointer;
	}

	@Override
	public void seek(long point) throws IOException {
		mFilePointer = point;
		updateLength();
	}

	@Override
	public long length() throws IOException {
		return mLength;
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
			if (_len>file.getBuffer().length-(getFilePointer()-file.skip())) {
				_len = (int)(file.getBuffer().length-(getFilePointer()-file.skip()));
			}
			System.arraycopy(file.getBuffer(), 
					(int)(getFilePointer()-file.skip()),
					buffer, len+start, _len);
			len += _len;
			mFilePointer +=_len;
		} while(len<buffLen);
		return buffLen;//todo(int)remain;
	}

	@Override
	public int write(byte[] data) throws IOException {
		return write(data, 0, data.length);
	}

	public int write(byte[] buffer, int start, int len) throws IOException {
		int tmp = -1;
		int writed = 0;
		do {
			tmp =_write(buffer, start+writed, len-writed);
			if(tmp<0) {
				return -1;
			} else {
				writed += tmp;
			}
		} while(writed<len);
		return writed;
	}

	private int _write(byte[] buffer, int start, int len) throws IOException {
		boolean isW = false;
		int writed = 0;
		int i=start;
		int tmp = -1;
		ByteKyoroFile file = getCash();
		file.seek(getFilePointer());

		int _len  =(int)(file.Limit()-getFilePointer());
		if(_len<len) {
			len = _len;
		}
		do {
			tmp = file.write(buffer, start+writed, len-writed);
			if(tmp>=0) {
				isW = true;
				writed += tmp;
			}
		} while(tmp>0&&tmp<len);
		if(!isW) {
			return -1;
		} else {
			mFilePointer +=(writed);
			updateLength();
			return writed;
		}
	}

	public int write(int b) throws IOException {
		ByteKyoroFile file = getCash();
		file.seek(getFilePointer());
		int ret = file.write(b);
		if(ret>0) {
			mFilePointer++;
			updateLength();
		}
		return ret;
	}

	public ByteKyoroFile getCash() throws IOException {
		int size =mCash.size();
		long fp = getFilePointer();
		if(mCurrent.skip()<=fp&&fp<(mCurrent.skip()+mChunkSize)) {
			return mCurrent;
		}
		for(int i=0;i<size;i++){
			ByteKyoroFile cash = mCash.get(i);
			if(cash.skip()<=fp&&fp<(cash.skip()+mChunkSize)){
				return cash;
			}
		}
		syncWrite(0);
		ByteKyoroFile ret = mCash.remove(0);
		mCash.addLast(ret);  
		ret.reset(getFilePointer()-(getFilePointer()%mChunkSize));
		if(mRAFile != null) {
			ret.update(mRAFile);
		}
		return ret;
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
	public void syncWrite() throws IOException {
		for(int i=0;i<mCash.size();i++) {
			syncWrite(i);
		}
	}

	public ByteKyoroFile syncWrite(int index) throws IOException {
		ByteKyoroFile file = mCash.get(index);
		if(file.isUpdated()) {
			if(mRAFile == null) {
				mRAFile = new RandomAccessFile(mFile, "rw");
			}
			file.writeTo(mRAFile);
		}
		return file;
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
			KyoroFile file = CashKyoroFile.this;
			buffer[0] = (byte)(0xFF&b);
			file.addChunk(buffer);
		}
		
	}

	private void updateLength() {
		if(mLength<mFilePointer){
			mLength = mFilePointer;
		}		
	}

}