package net.hetimatan.util.event.net;

import java.io.IOException;

import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.event.EventTaskRunnerImple;
import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.log.Log;


public class KyoroSocketEventRunner extends EventTaskRunnerImple {
	public static final String TAG ="looper";

	private SelctorLoopTask mOneShot = new SelctorLoopTask();
	private KyoroSelector mSelector = new KyoroSelector();
	private boolean mWaitIsSelect = false;

	public KyoroSocketEventRunner() {
		super();
		waitIsSelect(true);
	}

	public KyoroSelector getSelector() {
		return mSelector;
	}

	public void waitIsSelect(boolean on) {
		mWaitIsSelect = on;
	}

	@Override
	public void waitPlus(int timeout) throws InterruptedException, IOException {
		if(!mWaitIsSelect) {
			super.waitPlus(timeout);
			return;
		}
		//if(
		waitBySelectable(timeout);///) {
			//
		if(numOfWork() != 0) {
			pushTask(mOneShot);
		}
		//}
	}

	@Override
	public void kickWorker() {
		if(!mWaitIsSelect) {
			super.kickWorker();
		}else {
			synchronized (this){
			if(mSelector != null){// &&mIsSelecting == true) {
				if(!currentThreadIsMine()) {
					mSelector.wakeup();
				}
			}
			}
		}
	}

	public boolean waitBySelectable(int timeout) throws IOException, InterruptedException {
		if(Log.ON){Log.v(TAG, "waitBySelectable "+numOfWork());}
		if(numOfWork() == 0) {
			if(timeout<0) {
				mSelector.select(15000);//todo you test at 10000
			} else {
				mSelector.select(timeout);
			}
		} else {
			mSelector.select(0);			
		}
		boolean ret = false;
		while(mSelector.next()) {
			ret = true;
			if(!mSelector.getCurrentSocket().startEventTask(this, mSelector.getkey())) {
			//	if(Log.ON){Log.v(TAG,"Wearning not task");}
			}
		}
		return ret;
	}

	@Override
	public void close() {
		super.close();
		if(mSelector != null) {
			try {
				mSelector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized KyoroSocketEventRunner getYourWorker() {
		EventTaskRunner runner = EventTaskRunner.getYourWorker();
		if(runner instanceof KyoroSocketEventRunner) {
			return (KyoroSocketEventRunner)runner;
		} else {
			return null;
		}
	}

	public static class SelctorLoopTask extends EventTask {
		public static final String TAG = "SelctorLoopTask";
		public SelctorLoopTask() {
		}

		@Override
		public String toString() {
			return TAG;
		}

		@Override
		public void action(EventTaskRunner runner) throws Throwable {
			if(runner != null) {
				if(runner.numOfWork()!=0){
					if(((KyoroSocketEventRunner)runner).waitBySelectable(0)){
					}
					((KyoroSocketEventRunner)runner).pushTask(this);
				}
			}
		}
	}
}
