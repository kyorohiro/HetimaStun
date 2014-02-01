package net.hetimatan.util.log;

import java.io.File;
import java.io.IOException;

import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.event.GlobalAccessProperty;


public class Log {
	public static final boolean ON = true;//false;//true;//false;//true;//false;//true;//alse;//false;//false; 
	public static final long start = System.nanoTime();
    public static final int  MODE_STDOUT = 1;
    public static final int  MODE_FILE = 2;
    public static final Log  log = new Log();
    public static final int  mode = 1;//MODE_FILE;
    public static final boolean isTime = false;
    public static final int loglevel = 5;//8;// 5 v, 8 d
    private CashKyoroFile mLog = null;
  
    public Log() {
    	if(mode == MODE_FILE && ON) {
    		File parent = (new File("dummy")).getParentFile();
    		String path = GlobalAccessProperty.getInstance().get("my.home", parent.getAbsolutePath());
    		File home = new File(path);
            File file = new File(home, "master.log");
            try {
                mLog = new CashKyoroFile(file, 512, 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
    	}
    	Runtime.getRuntime().addShutdownHook(new Thread(new ShutdonwTask()));
	}

	public static void d(String tag, String message) {
		if(loglevel>=8) {
			v(tag, message);
		}
	}

	public static void v(String tag, String message) {
		String processingMessage = null;
		if(isTime) {
			processingMessage = ""+tag+":"+message+"+"+processingMessage+(System.nanoTime()-start);
		} else {
			processingMessage = ""+tag+":"+message;
		}
        switch(mode) {
        case MODE_FILE:
            try {
            	synchronized (Log.class) {
            		log.mLog.addChunk(processingMessage.getBytes());
            	}
                break;
            } catch (IOException e) {
            }
        case MODE_STDOUT:
    		System.out.println(processingMessage);
            break;
        }
	}

	public static void sync() {
        switch(mode) {
        case MODE_FILE:
            try {
            	synchronized (Log.class) {
            		log.mLog.syncWrite();
            	}
            } catch (IOException e) {
            }
        }		
	}

    public static class ShutdonwTask implements Runnable {
		@Override
		public void run() {
			Log.sync();
		}
    }

}
