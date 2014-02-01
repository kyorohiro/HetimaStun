package net.hetimatan.net.http.task.client;


import java.io.IOException;
import java.lang.ref.WeakReference;

import net.hetimatan.net.http.HttpGet;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.log.Log;

public class HttpGetReadHeaderTask extends EventTask {
	public static final String TAG = "HttpGetReadHeaderTask";
	private WeakReference<HttpGet> mOwner = null;
	private EventTask mLast = null;
	private boolean mHeaderIsReadable = false;
	private boolean mIsKeep = false;

	public HttpGetReadHeaderTask(HttpGet client, EventTask last) {
		mOwner = new WeakReference<HttpGet>(client);
		mLast = last;
		errorAction(last);
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public boolean isKeep() {
		return mIsKeep;
	}

	@Override
	public void action(EventTaskRunner runner) throws IOException, InterruptedException {
		if(!mHeaderIsReadable) {
			mHeaderIsReadable = mOwner.get().headerIsReadeable();
			//if(Log.ON){Log.v("===", "mHeaderIsReadable="+mHeaderIsReadable);}
			if(!mHeaderIsReadable) {
				mIsKeep = true;
				return;
			}
		}
		HttpGet httpget = mOwner.get();
		if(httpget == null) {return;}
		httpget.recvHeader();
		mIsKeep = false;
	}
	
}
