package net.hetimatan.util.event.net;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.io.ByteArrayBuilder;

/**
 * 
 * 
 *
 */
public class MessageSendTask extends EventTask {
	public static final String TAG = "MessageSendTask";
	private int mBufferSize = 512;
	private KyoroFile mData = null;
	private KyoroSocket mSocket = null;
	private boolean mIsKeep = false;

	public MessageSendTask(KyoroSocket socket, KyoroFile data) {
		mData = data;
		mSocket = socket;
	}

	@Override
	public String toString() {
		return TAG;
	}

	public void setLength(int size) {
		mBufferSize = size;
	}

	@Override
	public boolean isKeep() {
		return mIsKeep;
	}

	@Override
	public void action(EventTaskRunner runner) throws Throwable {
		mIsKeep = false;
		int len = (int)mData.length();
		if (len>mBufferSize) {
			len = mBufferSize;
		}

		ByteArrayBuilder bufferBase = KyoroSocketEventRunner.getByteArrayBuilder();
		bufferBase.setBufferLength(len);
		byte[] buffer = bufferBase.getBuffer();
		len = mData.read(buffer, 0, len);
		if (len<0) {
			return;
		}

		int wrlen = mSocket.write(buffer, 0, len);
		if (wrlen<0) {return;}
		mData.seek(mData.getFilePointer()-(len-wrlen));
		if (mData.getFilePointer()<mData.length()) {
			mIsKeep = true;
		}
	}
	
}
