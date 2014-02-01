package net.hetimatan.net.http;

import java.io.IOException;
import java.util.LinkedList;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.KyoroFileForFiles;
import net.hetimatan.io.filen.ByteKyoroFile;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.net.http.task.server.HttpFrontRequestTask;
import net.hetimatan.net.http.task.server.HttpServerAcceptTask;
import net.hetimatan.net.http.task.server.HttpServerBootTask;
import net.hetimatan.util.event.net.KyoroSocketEventRunner;
import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.event.net.io.KyoroServerSocket;
import net.hetimatan.util.event.net.io.KyoroServerSocketImpl;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.http.HttpRequest;
import net.hetimatan.util.io.ByteArrayBuilder;
import net.hetimatan.util.log.Log;


//
// response "this is test"
//
public class HttpServer {
	public static final String TAG = "HttpServer";

	public String sId = "[http_server_empty]";

	private LinkedList<HttpServerFront> mClientInfos = new LinkedList<HttpServerFront>();
	private HttpServerAcceptTask mAcceptTask = null;
	private int mPort = 8080;

	private KyoroSocketEventRunner mRequestRunner = null;
	private boolean mMyReqRunner = false;
	private KyoroServerSocket mServerSocket = null;

	private HttpServerEventDispatcher mObserver = new HttpServerEventDispatcher();

	public HttpServerEventDispatcher getDispatcher() {
		return mObserver;
	}

	//
	// Task Runner
	//
	public KyoroSocketEventRunner getEventRunner() {
		return mRequestRunner;
	}

	public KyoroServerSocketImpl getServerSocket() {
		return (KyoroServerSocketImpl)mServerSocket;
	}

	public boolean isBinded() {
		if(mServerSocket == null) {
			return false;
		} else {
			return mServerSocket.isBinded();
		}
	}

	public void setPort(int port) {
		mPort = port;
	}

	public void boot() throws IOException {
		if(Log.ON){Log.v(TAG, "HttpServer#boot():"+mPort);}
		mServerSocket = new KyoroServerSocketImpl();
		mServerSocket.bind(mPort);
		mServerSocket.regist(mRequestRunner.getSelector(), KyoroSelector.ACCEPT);
		sId = "[httpserver"+mPort+"]";
		startAcceptTask();
		mObserver.dispatchOnBoot(this);
	}

	public void accept() throws IOException {
		if(Log.ON){Log.v(TAG, "HttpServer#accept():");}
		KyoroSocket socket = mServerSocket.accept();
		if(socket == null) {
			return;
		}
		startStartFrontTask(socket);
	}

	private void startAcceptTask() {
		HttpHistory.get().pushMessage(sId+"#startAccept:"+"\n");
		mAcceptTask = new HttpServerAcceptTask(this);
		mServerSocket.setEventTaskAtWrakReference(mAcceptTask, KyoroSelector.ACCEPT);
	}

	private void startStartFrontTask(KyoroSocket socket) throws IOException {
		HttpServerFront front = new HttpServerFront(this, socket);
		HttpHistory.get().pushMessage(sId+"#startFront:"+front.sId+"\n");

		socket.regist(mRequestRunner.getSelector(), KyoroSelector.READ);
		HttpFrontRequestTask requestTask = new HttpFrontRequestTask(front);
		front.addMyTask(requestTask);
		socket.setEventTaskAtWrakReference(requestTask, KyoroSelector.READ);
		addManagedHttpFront(front);		
	}

	/**
	 * this method is overrided
	 */
	public ByteArrayBuilder createHeader(KyoroSocket socket, HttpRequest uri, KyoroFile responce) throws IOException {
		if(Log.ON){Log.v(TAG, "HttpServer#createHeader");}
		try {
			ByteArrayBuilder builder = new ByteArrayBuilder();
			builder.append(("HTTP/1.1 200 OK\r\n").getBytes());
			builder.append(("Content-Length: "+responce.length()+"\r\n").getBytes());
			builder.append(("Content-Type: text/plain\r\n").getBytes());
			builder.append(("Connection: close\r\n").getBytes());
			builder.append(("\r\n").getBytes());
			return builder;
		} finally {
			if(Log.ON){Log.v(TAG, "/HttpServer#createHeader");}
		}
	}


	/**
	 * this method is overrided
	 */
	public KyoroFile createResponse(HttpServerFront front, KyoroSocket socket, HttpRequest uri) throws IOException {
		HttpHistory.get().pushMessage(sId+"#createResponse:"+front.sId+"\n");
		KyoroFile responce = createContent(socket, uri);
		ByteArrayBuilder builder = createHeader(socket, uri, responce);
		KyoroFile[] files = new KyoroFile[2];
		files[0] = new ByteKyoroFile(builder);
		files[0].seek(0);
		files[1] = responce;
		files[1].seek(0);
		KyoroFileForFiles kfiles = new KyoroFileForFiles(files);
		kfiles.seek(0);
		return kfiles;
	}

	/**
	 * this method is overrided
	 */
	public KyoroFile createContent(KyoroSocket socket, HttpRequest uri) throws IOException {
		if(Log.ON){Log.v(TAG, "HttpServer#createResponse");}
		try {
			return new CashKyoroFile("hello world".getBytes());
		} finally {
			if(Log.ON){Log.v(TAG, "/HttpServer#createResponse");}
		}
	}

	public void close() {
		HttpHistory.get().pushMessage(sId+"#close:"+"\n");

		try {
			if (null != mServerSocket) {
				mServerSocket.close();
			}
			if(mClientInfos != null) {
				while (0<mClientInfos.size()) {
					HttpServerFront front = mClientInfos.removeFirst();
					front.close();
				}
			}
			if(mMyReqRunner&&mRequestRunner != null) {
				mRequestRunner.close();
			}
			mAcceptTask = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void addManagedHttpFront(HttpServerFront front) {
		if(mClientInfos != null && front != null) {
			mClientInfos.addLast(front);
		}
	}

	public void removeManagedHttpFront(HttpServerFront front) {
		if(mClientInfos != null && front != null) {
			mClientInfos.remove(front);
		}
	}


	/**
	 * start http server.
	 */
	public KyoroSocketEventRunner startServer(KyoroSocketEventRunner requestRunner) {
		if(Log.ON){Log.v(TAG, "HttpServer#startServer()");}

		if (requestRunner != null) {
			mRequestRunner = requestRunner;
		} else {
			mMyReqRunner = true;
			mRequestRunner = new KyoroSocketEventRunner();
		}

		mRequestRunner.waitIsSelect(true);
		HttpServerBootTask boot = new HttpServerBootTask(this);
		boot.nextAction(null);
		mRequestRunner.start(boot);
		return mRequestRunner;
	}

}
