package net.hetimatan.util.event.net.io;

import java.io.IOException;

public abstract class KyoroSocket extends KyoroSelectable {
	public static final int CN_CONNECTED    = 0;
	public static final int CN_CONNECTING   = 1;
	public static final int CN_DISCONNECTED = 2; 

	// conection --- close
	public abstract void connect(String hostname, int port) throws IOException;
	public abstract int getConnectionState();
	public abstract int write(byte[] buffer, int start, int length) throws IOException;
	public abstract int read(byte[] buffer, int start, int length) throws IOException;
	
	// property
	public abstract String getHost() throws IOException;
	public abstract int getPort() throws IOException;
	public abstract boolean isEOF();

	public abstract void regist(KyoroSelector selector, int key) throws IOException;

}
