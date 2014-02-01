package net.hetimatan.net.http.task.server;


import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpServerFront;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;

public class HttpFrontCloseTask extends EventTask {
	public static int sid = 0;
	public static final String TAG = "HttpFrontCloseTask";
	private WeakReference<HttpServerFront> mClientInfo = null;

	public int mId = sid++;
	public HttpFrontCloseTask(HttpServerFront clientInfo) {
		mClientInfo = new WeakReference<HttpServerFront>(clientInfo);
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
		info.close();
	}
}
