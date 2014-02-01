package net.hetimatan.net.http.request;


import java.io.IOException;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.event.net.MessageSendTask;
import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.event.net.io.KyoroSocketImpl;
import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.http.HttpRequest;


public class HttpGetRequester  {
	public static final String REQUEST_METHOD_GET = "GET";
	public static final String SCHEME_HTTP = "http";
	public static final String HTTP10 = "HTTP/1.0";
	public static final String HTTP11 = "HTTP/1.1";
	public static final String HEADER_HOST = "Host";
	public static final String USER_AGENT = "User-Agent";
	public static final String CONTENT_LENGTH = "Content-Length";

	private HttpRequestBuilder mBuilder = new HttpRequestBuilder();
	private MessageSendTask mTask = null;
	private CashKyoroFile mSendCash = null;

	// --- <omake> ---
	private boolean mIsPostMode = false;
	public void setBody(KyoroFile body) {
		mBuilder.putBody(body);
	}
	public void isPostMode(boolean on) {
		mIsPostMode = on;
	}
	public boolean isPostMode() {
		return mIsPostMode;
	}
	// --- </omake> ---

	public HttpGetRequester() {
	}

	public static void log(String message) {
		System.out.println("KyoroSocketGetRequester#"  + message);
	}

	public HttpRequestBuilder getUrlBuilder() {
		return mBuilder;
	}

	public KyoroSocket connect(KyoroSocket socket) throws IOException, InterruptedException {
		if(socket == null) {
			socket = new KyoroSocketImpl();
		}
		socket.setDebug("KyoroSocketGetConnection:"+mBuilder.getHost() +","+ mBuilder.getPort());
		socket.connect(mBuilder.getHost(), mBuilder.getPort());
		return socket;
	}

	public MessageSendTask getRequestTask(KyoroSocket socket) throws IOException {
		log("_writeRequest()");
		byte[] buffer = createRequest();
		log(new String(buffer));
		if(mSendCash == null) {
			mSendCash = new CashKyoroFile(1024, 3);
		}
		if(mTask == null) {
			mSendCash.addChunk(buffer);
			mTask = new MessageSendTask(socket, mSendCash);
		}
		return mTask;
	}

	public HttpGetResponse getResponse(KyoroSocket socket) throws IOException, InterruptedException {
		HttpGetResponse response = new HttpGetResponse(socket);
		return response;
	}	

	public synchronized HttpRequest createHttpRequest() throws IOException {
		if(false == mIsPostMode) {
			return mBuilder.createHttpGetRequest();
		} else {
			// omake
			return mBuilder.createHttpPostRequest();
		}
	}

	public synchronized byte[] createRequest() throws IOException {
		HttpRequest uri = createHttpRequest();
		return HttpObject.createEncode(uri).getBytes();
	}

	public void close() {
		try {
			if(mSendCash != null) {
				mSendCash.close();
			}
		} catch(IOException e) {

		}
	}
	/**
	 * テスト用
	 * @throws Throwable 
	 */
	public static HttpGetResponse syncRequest(HttpGetRequester requester, KyoroSelector selector) throws Throwable {
		KyoroSocket socket = null;
		try {
			socket = requester.connect(null);
			while(socket.getConnectionState() == KyoroSocket.CN_CONNECTING){
				Thread.yield();
				Thread.sleep(0);}
			MessageSendTask sendTask= requester.getRequestTask(socket);
			do {
				sendTask.action(null);
			} while(sendTask.isKeep());
			
			//
			HttpGetResponse res = requester.getResponse(socket);
			while(!res.headerIsReadable()){;}
			res.readHeader();
			while(!res.bodyIsReadable()){;}
			res.readBody();
			return res;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	/**
	 * テスト用
	 * @throws Throwable 
	 */
	public static HttpGetResponse doRequest(HttpGetRequester requester) throws Throwable {
		return syncRequest(requester, new KyoroSelector());
	}
}