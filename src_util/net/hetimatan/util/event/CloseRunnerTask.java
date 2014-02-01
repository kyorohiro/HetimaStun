package net.hetimatan.util.event;

public class CloseRunnerTask extends EventTask {
	private EventTask mLastTask = null;
	public final static String TAG = "CloseTask";

	public CloseRunnerTask(EventTask lasttask) {
		mLastTask = lasttask;
	}

	@Override
	public String toString() {
		return TAG;
	}

	@Override
	public boolean isKeep() {
		if(mLastTask != null) {
			return mLastTask.isKeep();
		} else {
			return super.isKeep();
		}
	}

	@Override
	public void action(EventTaskRunner runner) throws Throwable {
		if(mLastTask != null) {
			mLastTask.action(runner);
			if(mLastTask.isKeep()) {
				return;
			}
		}
		runner.close();
	}
}
