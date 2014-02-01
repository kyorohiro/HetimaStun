package net.hetimatan.util.event.net.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import net.hetimatan.util.http.HttpObject;

public class KyoroDatagramImpl extends KyoroDatagram {

	private DatagramChannel mChannel = null;
	private ByteBuffer mBuffer = ByteBuffer.allocate(8*1024);

	public KyoroDatagramImpl() throws IOException {
		mChannel = DatagramChannel.open();
		mChannel.configureBlocking(false);
	}

	@Override
	public SelectableChannel getRawChannel() {
		return mChannel;
	}

	@Override
	public void bind(byte[] ip) throws IOException {
		byte[] addr = HttpObject.ip2Address(ip);
		int port = HttpObject.bToPort(ip, addr.length);
		InetSocketAddress addrO =
				new InetSocketAddress(
						InetAddress.getByAddress(addr), port);		
		mChannel.socket().bind(addrO);
	}

	@Override
	public void bind(int port) throws IOException {
		mChannel.socket().bind(new InetSocketAddress(port));
	}

	@Override
	public byte[] getByte() {
		System.out.println("-----"+mBuffer.position());
		return mBuffer.array();
	}

	@Override
	public byte[] receive() throws IOException {
		mBuffer.position(0);
//		mBuffer = ByteBuffer.allocate(8192);
		SocketAddress address = mChannel.receive(mBuffer);
		if(address == null) {
			return null;
		}
		byte[] ret = new byte[6];
		byte[] ad = ((InetSocketAddress)address).getAddress().getAddress();
		int port =  ((InetSocketAddress)address).getPort();
		System.arraycopy(ad, 0, ret, 0, 4);
		System.arraycopy(HttpObject.portToB(port), 0, ret, 4, 2);
		return ret;
	}

	@Override
	public int send(byte[] message, byte[] address) throws IOException {
		return send(message, 0, message.length, address);
	}

	@Override
	public int send(byte[] message, int start, int length, byte[] address) throws IOException {
		InetSocketAddress iad = getInetSocketAddress(address);
		ByteBuffer buffer = ByteBuffer.allocate(length);
		buffer.put(message, start, length);
		buffer.flip();
		int ret =  mChannel.send(buffer, iad);
		return ret;
	}

	@Override
	public void regist(KyoroSelector selector, int key) throws IOException {
		mChannel.register(selector.getSelector(), key);
		selector.putClient(this);
	}

	InetSocketAddress getInetSocketAddress(byte[] info) {
		byte[] address = new byte[4];
		byte[] port = new byte[2];
		System.arraycopy(info, 0, address, 0, 4);
		System.arraycopy(info, 4, port, 0, 2);
		return new InetSocketAddress(HttpObject.ntoa(address), HttpObject.bToPort(port));
	}
	
	@Override
	public void close() throws IOException {
		try {
			if(mChannel != null) {
				mChannel.close();
			}
			mChannel = null;
		} finally {
			super.close();
		}
	}
}
