package net.hetimatan.util.event;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import net.hetimatan.util.log.Log;

public class EventTaskRunnerImple extends EventTaskRunner {
	private static int sID = 0;
	public int mId = sID++;
	private SingleTaskRunner mRunner = new SingleTaskRunner();

	private LinkedList<EventTask> mTasks = new LinkedList<EventTask>();
	private LinkedList<DefferTask> mDefferTasks = new LinkedList<DefferTask>();

	private Worker mWorker = null;

	public boolean currentThreadIsMine() {
		return mRunner.currentThreaddIsMime();
	}

	public EventTaskRunnerImple() {
	}

	@Override
	public boolean isAlive() {
		if(mRunner == null) {return false;}
		return mRunner.isAlive();
	}

	public synchronized void pushTask(EventTask task) {
		start(task);
	}

	public synchronized void pushTask(EventTask task, int timeout) {
		mDefferTasks.add(new DefferTask(task, timeout));
		if(mRunner == null|| mWorker == null) {
			start(null);
		}
		kickWorker();
	}

	@Override
	public void releaseTask(EventTask task) {
		if(task != null) {
		mDefferTasks.remove(task);
		}
	}

	public int updateDeffer() {
		long curTime = System.currentTimeMillis();
		long time = -1;
		long ret = -1;
		for(int i=0;i<mDefferTasks.size();i++) {
			DefferTask task = mDefferTasks.get(i);
			time = task.deffer(curTime);
			if(time<=0) {
				pushTask(task.getEventTask());
				mDefferTasks.remove(i);
				ret = 0;
			} else {
				if(ret==-1) {
					ret = time;					
				}
				else if(ret>time) {
					ret = time;
				}
			}
		}
		return (int)ret;
	}

	public synchronized EventTask popTask() {
		try {
		if (mTasks.size() > 0) {
			return mTasks.remove(0);//removeFirst();
//			return mTasks.removeFirst();//(0);//removeFirst();
		} else {
			return null;
		}
		} catch(NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public void start(EventTask task) {
		super.start(task);
		//pushWork(task);
//		mTasks.addLast(task);
		if (task != null) {
			mTasks.add(task);
		}
		if (mRunner == null || !mRunner.isAlive()) {
			mRunner = new SingleTaskRunner();
			mRunner.startTask(mWorker = new Worker(this));
		} else if(mWorker != null){
			//mWorker.kick();
			kickWorker();
		}
	}

	public void dispose() {
		mRunner.endTask();
	}

	public void kickWorker() {
		mWorker.kick();
	}

	public synchronized void kick() {
		notify();
	}
	public synchronized void waitPlus(int time) throws InterruptedException, IOException {
		if(time<0) {
			//Thread.sleep(10000);
			wait(10000);
		} else {
			if(time > 0) {
				wait(time);
			}
			//Thread.sleep(time);			
		}
	}

	public static class Worker implements Runnable {

		WeakReference<EventTaskRunnerImple> mRunner = null;

		public Worker(EventTaskRunnerImple runner) {
			mRunner = new WeakReference<EventTaskRunnerImple>(runner);
		}

		public void kick() {
			EventTaskRunnerImple runner = mRunner.get();
			runner.kick();
		}

		@Override
		public void run() {
			putWorker(Thread.currentThread(), mRunner.get());
			try {
				while (true) {
					if(mRunner == null) {break;}
					EventTaskRunnerImple runner = mRunner.get();
					if (null == runner||runner.mRunner == null) {
						break;
					}
					EventTask task = runner.popTask();
					if (task == null) {
						//synchronized(runner){
							synchronized(this){
								if(!Thread.interrupted()) {
									runner.waitPlus(runner.updateDeffer());
								} 
							}
						//}
					} else {
						task.run(mRunner.get());
					}
				}
			} catch(InterruptedException e) {} catch(IOException e) {
			}
		}
	}

	@Override
	public void close() {
		super.close();
		if(Log.ON){Log.v("mm","EventTaskRunner#close");}
		if(mRunner != null) {
			mRunner.endTask();
		}
		mRunner = null;
	}

	@Override
	public boolean contains(EventTask task) {
		return mTasks.contains(task);
	}

	@Override
	public int numOfWork() {
		return mTasks.size();
	}


	public static class DefferTask {
		public static String TAG = "DifferTask";
		private EventTask mDefferTasks = null;
		private long mDefferEnd = 0;
		private long mDefferStart = 0;
		public DefferTask(EventTask task, int deffer) {
			mDefferStart = System.currentTimeMillis();
			mDefferEnd = mDefferStart + (long)deffer;
			mDefferTasks = task;
		}

		public long deffer(long curTime) {
			return mDefferEnd-curTime;
		}

		public EventTask getEventTask() {
			return mDefferTasks;
		}
		
		@Override
		public String toString() {
			return TAG;
		}
	}	
	
} 
