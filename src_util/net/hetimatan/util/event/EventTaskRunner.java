package net.hetimatan.util.event;


import java.util.HashMap;

import net.hetimatan.util.io.ByteArrayBuilder;


public abstract class EventTaskRunner {
	public abstract int numOfWork();
	public abstract void releaseTask(EventTask task);
	public abstract void pushTask(EventTask task);
	public abstract void pushTask(EventTask task, int timeout);
	public abstract EventTask popTask();
	public abstract boolean contains(EventTask task);

	// 
	private boolean mIsClosed = false;
	private Object mCloseLock = new Object();
	public void close() {
		synchronized (mCloseLock) {
			mIsClosed = true;
			mCloseLock.notify();
		}
	}

	public boolean waitByClose(int timeout) throws InterruptedException {
		synchronized (mCloseLock) {
			if(mIsClosed) {
				return mIsClosed;
			}
			mCloseLock.wait(timeout);
			return mIsClosed;
		}
	}

	//
	//-------------------------------------
	//

	private boolean mIsFirst = true;
	public void start(EventTask task) {
		if(mIsFirst) {
			putWorker(Thread.currentThread(), this);
			mIsFirst = false;
		}
	}

	public boolean isAlive() {return true;}

	//	private static WeakHashMap<Thread, EventTaskRunner> mMap = new WeakHashMap<Thread, EventTaskRunner>();
	private static HashMap<Thread, EventTaskRunner> mMap = new HashMap<Thread, EventTaskRunner>();
	public static synchronized void putWorker(Thread th, EventTaskRunner runner) {
		mMap.put(th, runner);
	}

	public static synchronized EventTaskRunner getYourWorker() {
		Thread current = Thread.currentThread();
		if(mMap.containsKey(current)) {
			return mMap.get(current);
		} else {
			return null;
		}
	}

	//
	// Thread dependent variable byte array
	//
	private ByteArrayBuilder mTemp = new ByteArrayBuilder(); 
	public ByteArrayBuilder getTemp() {
		return mTemp;
	}

	public static ByteArrayBuilder getByteArrayBuilder() {
		EventTaskRunner runner = EventTaskRunner.getYourWorker();
		if(runner != null){
			return runner.getTemp();
		} else {
			return new ByteArrayBuilder();
		}
	}

}
