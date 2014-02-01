package net.hetimatan.net.http.task.server;


import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpServerFront;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;

public class HttpFrontRequestTask extends EventTask {
	public static final String TAG = "HttpFrontRequestTask";
	private WeakReference<HttpServerFront> mClientInfo = null;

	public HttpFrontRequestTask(HttpServerFront clientInfo) {
		mClientInfo = new WeakReference<HttpServerFront>(clientInfo);
		errorAction(new HttpFrontCloseTask(clientInfo));
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public void action(EventTaskRunner runner) throws Throwable {
		HttpServerFront info = mClientInfo.get();
		if(info == null) {
			return;
		} 
		info.executeFrontWork();
	}
}
