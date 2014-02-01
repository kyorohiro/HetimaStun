package net.hetimatan.util.event.net.io;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.SelectableChannel;

import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.log.Log;

public abstract class KyoroSelectable {
	public abstract SelectableChannel getRawChannel();
	private String mDebug = "001";
	private WeakReference<Object> mRelative = null; 

	private WeakReference<EventTask> mAcceptTask = null; 
	private WeakReference<EventTask> mReadTask = null; 
	private WeakReference<EventTask> mWriteTask = null; 
	private WeakReference<EventTask> mConnectTask = null; 
	private Object mMemo = null;

	public void setMemo(byte[] tmp) {
		mMemo = tmp;
	}

	public Object getMemo() {
		return mMemo;
	}

	public void setRelative(Object obj) {
		mRelative = new WeakReference<Object>(obj);
	}

	public void setDebug(String deubg) {
		mDebug = deubg;
	}

	public Object getRelative() {
		if(mRelative == null) {
			return null;
		}
		return mRelative.get();
	}

	public void rejectEventTask(EventTask task) {
		if(rejectEventTask(mAcceptTask, task)) {mAcceptTask = null;}
		if(rejectEventTask(mConnectTask, task)) {mConnectTask = null;}
		if(rejectEventTask(mReadTask, task)) {mReadTask = null;}
		if(rejectEventTask(mWriteTask, task)) {mWriteTask = null;}
	}

	private boolean rejectEventTask(WeakReference<EventTask> wtask, EventTask task) {
		if(wtask == null) {return false;};
		EventTask t = wtask.get();
		if(t == null) { return false;}
		
		if(t == task) {
			return true;
		} else {
			return false;
		}
	}
	public void setEventTaskAtWrakReference(EventTask task, int state) {
		if((state&KyoroSelector.ACCEPT)==KyoroSelector.ACCEPT) {
			if(task == null) {
				mAcceptTask = null;
			} else {
				mAcceptTask = new WeakReference<EventTask>(task);
			}
		}
		if((state&KyoroSelector.READ)==KyoroSelector.READ) {
			if(task == null) {
				mReadTask = null;
			} else {
				mReadTask = new WeakReference<EventTask>(task);
			}
		}
		if((state&KyoroSelector.WRITE)==KyoroSelector.WRITE) {
			if(task == null) {
				mWriteTask = null;
			} else {
				mWriteTask = new WeakReference<EventTask>(task);
			}
		}
		if((state&KyoroSelector.CONNECT)==KyoroSelector.CONNECT) {
			if(task == null) {
				mConnectTask = null;
			} else {
				mConnectTask = new WeakReference<EventTask>(task);
			}
		}
	}

	public boolean startEventTask(EventTaskRunner runner, int key) {
		boolean ret = false;
		if((key&KyoroSelector.ACCEPT)==KyoroSelector.ACCEPT) {
			ret |= action(runner, mAcceptTask);
		}
		if((key&KyoroSelector.READ)==KyoroSelector.READ) {
			ret |= action(runner, mReadTask);
		}
		if((key&KyoroSelector.WRITE)==KyoroSelector.WRITE) {
			ret |= action(runner, mWriteTask);
		}
		if((key&KyoroSelector.CONNECT)==KyoroSelector.CONNECT) {
			ret |= action(runner, mConnectTask);
		}
		return ret;
	}

	private boolean action(EventTaskRunner runner, WeakReference<EventTask> eventTask ) {
		if(eventTask == null) {
			return false;
		}
		EventTask task =  eventTask.get();
		if(task == null) {
			return false;
		}
		Log.v("mm", "selector push:"+"["+task.mid+"]"+task.toString());
		runner.start(task);
		return true;
	}

	public void close() throws IOException  {
		mRelative = null;
		mAcceptTask = null;
		mConnectTask = null;
		mReadTask = null;
		mWriteTask = null;
	}
}
