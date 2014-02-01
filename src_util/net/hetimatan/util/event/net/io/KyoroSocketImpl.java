package net.hetimatan.util.event.net.io;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import net.hetimatan.util.log.Log;
public class KyoroSocketImpl extends KyoroSocket {

	private SocketChannel mSocketChannel = null;
	private boolean mIsEOF = false;

	public KyoroSocketImpl() throws IOException {
		mSocketChannel = SocketChannel.open();
		mSocketChannel.configureBlocking(false);
	}

	public KyoroSocketImpl(SocketChannel socketChannel) throws IOException {
		mSocketChannel = socketChannel;
		mSocketChannel.configureBlocking(false);
	}

	// for test
	public Socket getSocket() {
		return mSocketChannel.socket();
	}

	@Override
	public void connect(String hostname, int port) throws IOException {
		mSocketChannel.connect(new InetSocketAddress(hostname, port));
		mSocketChannel.configureBlocking(false);
	}


	public SocketChannel getRawChannel() {
		return mSocketChannel;
	}

	@Override
	public int read(byte[] output, int start, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int len = 0;
		len = mSocketChannel.read(buffer);
		if (len < 0) {
			mIsEOF = true;
			return len;
		} else {
			buffer.flip();
			System.arraycopy(buffer.array(), 0, output, start, len);
			return len;//buffer.limit();// len;
		}
	}

	// fail safe
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public static final String TAG ="KyoroSocket";
	@Override
	public void close() throws IOException {
		//
		if(Log.ON){Log.v(TAG, "KyoroSocketImp#close()");}
		if(mSocketChannel != null) {
			mSocketChannel.close();
			mSocketChannel = null;
		}
		if(mKey != null) {
			mKey.cancel();
			mKey = null;
		}
		super.close();
	}

	@Override
	public int write(byte[] buffer, int start, int length) throws IOException {
		ByteBuffer src = ByteBuffer.allocate(length);
		src.put(buffer, start, length);
		src.flip();
		int ret =0;
		do {
	//		System.out.println("---------write--###");
			ret = mSocketChannel.write(src);
		///	System.out.println("/---------write--###");
			if(ret<0) {
				mIsEOF = true;
				break;
			}
			//
			// todo kiyo
			if(ret==0) {
				break;
			}
		}while(src.position()<src.limit());
		
		return src.position();
	}

	@Override
	public int getConnectionState() {
		try {
			if (mSocketChannel.finishConnect()) {
				return KyoroSocket.CN_CONNECTED;
			} else {
				return KyoroSocket.CN_CONNECTING;
			}
		}
		catch (IOException e) {
			//e.printStackTrace();
			return KyoroSocket.CN_DISCONNECTED;
		}
	}

	@Override
	public String getHost() throws IOException {
		Socket socket = mSocketChannel.socket();
		if(null == socket) {
			return "127.0.0.1";
		}
		InetAddress address = socket.getInetAddress();
		if(address == null) {
			return "127.0.0.1";
		}
		String ret = address.getHostAddress();
		if(ret == null) {
			return "127.0.0.1";
		}
		return ret; 
	}

	@Override
	public int getPort() throws IOException {
		Socket socket = mSocketChannel.socket();
		if(null == socket) {
			return 80;
		}
		return socket.getPort();
	}

	@Override
	public boolean isEOF() {
		return mIsEOF;
	}

	@Override
	public void regist(KyoroSelector selector, int key) throws IOException {
		mKey = mSocketChannel.register(selector.getSelector(), key);
		selector.putClient(this);
	}
	private SelectionKey mKey = null;

}
