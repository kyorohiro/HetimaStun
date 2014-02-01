package net.hetimatan.net.http.task.server;


import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpServer;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;

public class HttpServerAcceptTask extends EventTask {
	public static final String TAG = "HttpServerAcceptTask";
	private WeakReference<HttpServer> mServer = null;

	public HttpServerAcceptTask(HttpServer httpServer) {
		mServer = new WeakReference<HttpServer>(httpServer);
		errorAction(new HttpServerClose(httpServer));
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public void action(EventTaskRunner runner) throws Throwable {
		HttpServer server = mServer.get();
		server.accept();
	}
}