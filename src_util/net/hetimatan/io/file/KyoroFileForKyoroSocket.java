package net.hetimatan.io.file;


import java.io.IOException;
import java.io.OutputStream;

import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.log.Log;

/**
 * 
 * 
 *
 */
public class KyoroFileForKyoroSocket extends OutputStream implements KyoroFile {

	private KyoroSocket mSocket = null;
	private CashKyoroFile mVf = null;
	private KyoroSelector mSelector = null;
	private byte[] mChunk = new byte[16*1024];

	public KyoroFileForKyoroSocket(KyoroSocket socket, int writeCashSize) throws IOException {
		mSocket = socket;
		mVf = new CashKyoroFile(writeCashSize, 2);
	}

	public void setSelector(KyoroSelector selector){
		mSelector = selector;
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

	@Override
	public long length() throws IOException {
		return mVf.length();
	}


	private synchronized void addChunk(int size) throws IOException {
		int addedSize = 0;
		while (true) {
			int len = mChunk.length;
			len = mSocket.read(mChunk, 0, len);
			if (len <= 0) {
				break;
			}
			mVf.addChunk(mChunk, 0, len);
			addedSize += len;
			size -= addedSize;
			if(addedSize>=0) {
				break;
			}
		}
	}

	@Override
	public synchronized int read(byte[] buffer) throws IOException {
//		return mVf.read(buffer, 0, buffer.length);
		return read(buffer, 0, buffer.length);
	}

	@Override
	public synchronized int read(byte[] buffer, int start, int buffLen) throws IOException {
		long available = length() - getFilePointer();
		if (available < buffer.length) {
			addChunk((int) (buffer.length - available));
		}
		int ret =  mVf.read(buffer, start, buffLen);
		if(ret<0&&mSocket.isEOF()) {
		      return -1;
		} 
		 else if(ret<0&&!mSocket.isEOF()) {
		      return 0;
		}
		return ret;
	}

	public static final String TAG = "KyoroFileForKyorSocket";
	@Override
	public void close() throws IOException {
		if(Log.ON){Log.v(TAG, "close");}
		if (mBaseFileIsClosedWhenCallClose) {
			mVf.close();
		}
		mSocket.close();
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

	private boolean mBaseFileIsClosedWhenCallClose = true;

	public void baseFileIsClosedWhenCallClose(boolean isClose) {
		mBaseFileIsClosedWhenCallClose = isClose;
	}

	@Override
	public void write(int b) throws IOException {
		byte ba = (byte) (0xFF & b);
		addChunk(ba);
	}

	@Override
	public int waitForUnreadable(int timeout) throws IOException {
		if(mSelector == null){
			return 1;
		}
		try {
			///todo
			Log.v("KyoroFileForSocket", "select");
			mSocket.regist(mSelector, KyoroSelector.READ);
			return mSelector.select(timeout);
		} finally {
			Log.v("KyoroFileForSocket", "/select");
		}
	}
}
