package net.hetimatan.util.event.net.io;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import net.hetimatan.util.event.EventTask;

public abstract class KyoroServerSocket extends KyoroSelectable {
	public abstract void regist(KyoroSelector selector, int key) throws IOException;
	public abstract void bind(int port) throws IOException;
	public abstract int getPort() throws IOException;
	public abstract KyoroSocket accept() throws IOException;
	//public abstract void close() throws IOException;
	public abstract boolean isBinded();
	public abstract void setEventTaskAtWrakReference(KyoroSelector selector, EventTask task, int state) throws ClosedChannelException, IOException;
}
