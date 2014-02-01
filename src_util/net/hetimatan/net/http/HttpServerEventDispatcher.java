package net.hetimatan.net.http;

import java.util.LinkedList;

public class HttpServerEventDispatcher {
	private LinkedList<HttpServerListener> mObserver = new LinkedList<>();
	public void dispatchOnBoot(HttpServer server) {
		for(HttpServerListener o: mObserver) {
			o.onBoot(server);
		}
	}
	public void addHttpServerListener(HttpServerListener observer) {
		mObserver.add(observer);
	}
	
	public static interface HttpServerListener {
		public void onBoot(HttpServer server);
	}
}