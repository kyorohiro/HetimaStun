package net.hetimatan.util.event.net.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.hetimatan.util.event.EventTask;

public class KyoroServerSocketImpl extends KyoroServerSocket {

	private ServerSocketChannel mServerChannel = null;
	private boolean mIsBinded = false;

	public KyoroServerSocketImpl() throws IOException {
		 mServerChannel = ServerSocketChannel.open();
		 mServerChannel.configureBlocking(false);
	}

	@Override
	public void bind(int port) throws IOException {
		mServerChannel.socket().bind(new InetSocketAddress(port));
		mIsBinded = true;
	}

	@Override
	public boolean isBinded() {
		return mIsBinded;
	}

	@Override
	public int getPort() throws IOException {
		return mServerChannel.socket().getLocalPort();
	}


	@Override
	public KyoroSocket accept() throws IOException {
		SocketChannel channel = mServerChannel.accept();
		if(channel == null) {
			return null;
		}
		return new KyoroSocketImpl(channel);
	}

	@Override
	public void close() throws IOException {
		if(mServerChannel != null) {
			mServerChannel.close();
		}
		super.close();
	}

	@Override
	public void regist(KyoroSelector selector, int key) throws ClosedChannelException, IOException {
		mServerChannel.register(selector.getSelector(), key);
		selector.putClient(this);
	}

	public void setEventTaskAtWrakReference(KyoroSelector selector, EventTask task, int state) throws ClosedChannelException, IOException {	
		regist(selector, state);
		setEventTaskAtWrakReference(task, state);
	}

	public ServerSocketChannel getRawChannel() {
		return mServerChannel;
	}

	public String test() {
		return mServerChannel.socket().getLocalSocketAddress().toString();
	}
}
