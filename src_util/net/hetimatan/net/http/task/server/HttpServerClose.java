package net.hetimatan.net.http.task.server;


import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpServer;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;

public class HttpServerClose extends EventTask {
	public static final String TAG = "HttpServerClose";
	private WeakReference<HttpServer> mServer = null;

	public HttpServerClose(HttpServer httpServer) {
		mServer = new WeakReference<HttpServer>(httpServer);
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public void action(EventTaskRunner runner) throws Throwable {
		HttpServer server = mServer.get();
		server.close();
	}
}