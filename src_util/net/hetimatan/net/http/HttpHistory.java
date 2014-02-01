package net.hetimatan.net.http;

import java.io.File;
import java.io.IOException;

import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.event.GlobalAccessProperty;
import net.hetimatan.util.log.Log;


public class HttpHistory {

	private CashKyoroFile mCash = null;
	private static HttpHistory sHistory = null;

	private HttpHistory() throws IOException {
		File parent = (new File("dummy")).getAbsoluteFile().getParentFile();
		String path = GlobalAccessProperty.getInstance().get("my.home", parent.getAbsolutePath());
		File home = new File(path);
		mCash = new CashKyoroFile(new File(home, "http.history"), 512, 2);
		mCash.isCashMode(false);
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutdonwTask()));
	}

	public static HttpHistory get() {
		if(sHistory == null) {
			try {
				sHistory = new HttpHistory();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sHistory;
	}

	public void sync() {
		try { mCash.syncWrite(); } catch (IOException e) {}
	}

	public synchronized void pushMessage(String mes) {
		if(mes != null) {
			Log.v("HISTORY", mes);
			try {
				mCash.addChunk(mes.getBytes());
			} catch(IOException e) {

			}
		}
	}


	public class ShutdonwTask implements Runnable {
		@Override
		public void run() {
			sync();
		}
	}

}
