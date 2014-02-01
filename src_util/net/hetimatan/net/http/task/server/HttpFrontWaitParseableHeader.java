package net.hetimatan.net.http.task.server;


import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpServerFront;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;

public class HttpFrontWaitParseableHeader extends EventTask {
	public static final String TAG = "HttpFrontWaitParseableHeader";
	private WeakReference<HttpServerFront> mClientInfo = null;

	public HttpFrontWaitParseableHeader(HttpServerFront clientInfo) {
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
		if(info.parseableHeader()) {
			nextAction(this);
		} else {
			nextAction(this);
		}
	}
}
