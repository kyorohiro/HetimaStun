package net.hetimatan.net.http.task.client;


import java.io.IOException;
import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpGet;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;

public class HttpGetConnectionTask extends EventTask {

	public static final String TAG = "HttpGetConnectionTask";
	private WeakReference<HttpGet> mOwner = null;
	private EventTask mLast = null;
	private boolean mIsFirst = true;	

	public HttpGetConnectionTask(HttpGet client, EventTask last) {
		mOwner = new WeakReference<HttpGet>(client);
		mLast = last;
		errorAction(last);
	}

	@Override
	public String toString() {
		return TAG;
	}

	private boolean mIsKeep = true;

	@Override
	public boolean isKeep() {
		return mIsKeep;
	}

	//
	@Override
	public void action(EventTaskRunner runner) throws InterruptedException, IOException {
		if (true == mIsFirst) {
			mIsFirst = false;
			mOwner.get().connect();
		}
		if(mOwner.get().isConnected()) {
			mIsKeep = false;
		} else {
			mIsKeep = true;
		}
	}
}
