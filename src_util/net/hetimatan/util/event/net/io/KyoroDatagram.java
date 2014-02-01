package net.hetimatan.util.event.net.io;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

public abstract class KyoroDatagram extends KyoroSelectable {

	@Override
	public abstract SelectableChannel getRawChannel();
	public abstract void bind(byte[] ip) throws IOException;

	public abstract void bind(int port) throws IOException;

	public abstract byte[] getByte();

	public abstract byte[] receive() throws IOException;
	
	public abstract int send(byte[] message, byte[] address) throws IOException;

	public abstract int send(byte[] message, int start, int length, byte[] address) throws IOException;
	
	public abstract void regist(KyoroSelector selector, int key) throws IOException;

	@Override
	public void close() throws IOException {
		super.close();
	};

}
