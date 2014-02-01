package net.hetimatan.net.http;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.net.http.request.HttpGetRequester;
import net.hetimatan.net.http.request.HttpGetResponse;
import net.hetimatan.net.http.task.client.HttpGetConnectionTask;
import net.hetimatan.net.http.task.client.HttpGetReadBodyTask;
import net.hetimatan.net.http.task.client.HttpGetReadHeaderTask;
import net.hetimatan.net.http.task.client.HttpGetRequestTask;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.event.net.KyoroSocketEventRunner;
import net.hetimatan.util.event.net.MessageSendTask;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.http.HttpGetRequestUri;
import net.hetimatan.util.http.HttpRequestHeader;
import net.hetimatan.util.http.HttpResponse;
import net.hetimatan.util.log.Log;

public class HttpGet {

	public static final String TAG = "HttpGet";
	public String sId = "[httpget]";

	private HttpGetRequester mCurrentRequest  = null;
	private HttpGetResponse  mCurrentResponse = null;
	private KyoroSocket      mCurrentSocket   = null;
	private String mHost = "127.0.0.1";
	private String mPath = "/";
	private int mPort = 80;
	private EventTaskRunner mRunner = null;
	private HttpGetTaskManager mTaskManager = new HttpGetTaskManager();

	public HttpGet() throws IOException {}

	public EventTaskRunner getRunner() {return mRunner;}

	public KyoroSocket getSocket() {return mCurrentSocket;}

	protected HttpGetResponse getGetResponse() {return mCurrentResponse;}

	protected HttpGetRequester createGetRequest() {
		if (mCurrentRequest == null) {
			mCurrentRequest = new HttpGetRequester();
		}
		return mCurrentRequest;
	}

	public void update(String host, String path, int port) throws IOException {
		mHost = host;
		mPath = path;
		mPort = port;
		sId = "[httpget "+mHost+":"+mPort+mPath+"]";
		HttpHistory.get().pushMessage(sId+"#update:"+"\n");
		dispose();
	}

	public void update(String location) throws IOException {
		MarkableFileReader reader = null;
		try {
			reader = new MarkableFileReader(location.getBytes());
			HttpGetRequestUri geturi = HttpGetRequestUri.decode(reader);
			update(geturi.getHost(), geturi.getMethod(), geturi.getPort());
		} finally {
			reader.close();
		}
	}

	// <omake>
	private KyoroFile mBody = null;
	private boolean mIsPost = false;
	public void setBody(KyoroFile contents) throws IOException {
		mBody = contents;
		mIsPost = true;
	}

	//</omake>
	private LinkedHashMap<String,String> mHeaderValue = new LinkedHashMap<>();
	public void addHeader(String type, String value) {
		mHeaderValue.put(type, value);
	}
	
	public KyoroSocketEventRunner startTask(KyoroSocketEventRunner runner, EventTask last) {
		HttpHistory.get().pushMessage(sId+"#startTask"+"\n");
		mTaskManager.mLast = last;
		if(runner == null) {
			runner = new KyoroSocketEventRunner();
		}
		mRunner = runner;
		HttpGetConnectionTask connectionTask = new HttpGetConnectionTask(this, last);
		connectionTask.nextAction(new HttpGetRequestTask(this, last));
		runner.start(connectionTask);
		return runner; 
	}

	public void connect() throws IOException, InterruptedException {
		if(Log.ON){Log.v(TAG, "HttpGet#connection()");}
		mCurrentRequest = createGetRequest();
		mCurrentResponse = null;
		mCurrentRequest.getUrlBuilder().setHost(mHost).setPath(mPath).setPort(mPort);
		{
			Set<String> keys = mHeaderValue.keySet();
			for(String key: keys) {
				mCurrentRequest.getUrlBuilder().putHeader(key, mHeaderValue.get(key));
			}
		}
		mCurrentSocket = mCurrentRequest.connect(null);
		if(mIsPost) {
			mCurrentRequest.isPostMode(true);
			mCurrentRequest.setBody(mBody);
		}
	}

	public void send() throws InterruptedException, IOException {
		if(Log.ON){Log.v(TAG, "HttpGet#send()");}
		MessageSendTask sendTask = mCurrentRequest.getRequestTask(getSocket());
		sendTask
		.nextAction(new HttpGetReadHeaderTask(this, mTaskManager.mLast))
		.nextAction(new HttpGetReadBodyTask(this, mTaskManager.mLast));
		getRunner().pushTask(sendTask);
		mTaskManager.mSendTaskChain = sendTask;
	}

	public void recvHeader() throws IOException, InterruptedException {
		if(Log.ON){Log.v(TAG, "HttpGet#revcHeader()");}
		if(mCurrentResponse == null) {
			mCurrentResponse = mCurrentRequest.getResponse(mCurrentSocket);
		}
		HttpHistory.get().pushMessage(sId+"#recvHeader:"+"\n");
		mCurrentResponse.readHeader();
	}

	public void recvBody() throws IOException, InterruptedException {
		HttpHistory.get().pushMessage(sId+"#recvBody:"+"\n");
		mCurrentResponse.readBody();

		try {
			CashKyoroFile vf = mCurrentResponse.getVF();
			byte[] buffer = getBody();
			vf.read(buffer, 0, buffer.length);
			//System.out.println("@1:"+new String(buffer, 0, mCurrentResponse.getVFOffset()));
			System.out.println("@2:"+new String(buffer));
			System.out.println("@3:"+mCurrentResponse.getVFOffset()+","+buffer.length);

		} finally {
			close();
		}
	}

	//
	//
	public byte[] getBody() throws IOException {
		CashKyoroFile vf = mCurrentResponse.getVF();
		vf.seek(mCurrentResponse.getVFOffset());
		int len = (int)vf.length() - mCurrentResponse.getVFOffset();
		byte[] buffer = new byte[len];
		vf.read(buffer, 0, len);
		return buffer;
	}

	public boolean isRedirect() throws IOException {
		HttpGetResponse response = getGetResponse();
		HttpResponse httpResponse = response.getHttpResponse();
		String statusCode = httpResponse.getStatusCode();
		for(String candidate :HttpResponse.REDIRECT_STATUSCODE) {
			if (candidate.equals(statusCode)) {
				return true;
			}
		}
		return false;
	}

	public String getLocation() throws IOException {
		HttpGetResponse response = getGetResponse();
		HttpResponse httpResponse = response.getHttpResponse();
		String path = httpResponse.getHeader(HttpRequestHeader.HEADER_LOCATION);
		path = path.replaceAll(" ", "");
		if(path.startsWith("http://")) {
			return path;
		} else {
			if(!path.startsWith("/")) {
				path = "/"+path;
			}
			return mHost+":"+mPort+""+path;
		}
	}

	/**
	 * you must call dispose too.
	 * this method don't release response cash 
	 */
	public void close() throws IOException {
		HttpHistory.get().pushMessage(sId+"#close:"+"\n");
		if(mCurrentSocket != null) {
			mCurrentSocket.close();
			mCurrentSocket = null;
		}
		if(mCurrentRequest != null) {
			mCurrentRequest.close();
			mCurrentRequest = null;
		}
	}

	public void dispose() throws IOException {
		if(mCurrentResponse!= null) {
			mCurrentResponse.close();
			mCurrentResponse = null;
		}
	}

	//
	//
	//
	public boolean isConnected() throws IOException {
		switch (mCurrentSocket.getConnectionState()) {
		case KyoroSocket.CN_CONNECTED:
			HttpHistory.get().pushMessage(sId+"#connected"+"\n");
			return true;
		case KyoroSocket.CN_CONNECTING:
			return false;
		case KyoroSocket.CN_DISCONNECTED:
		default:
			HttpHistory.get().pushMessage(sId+"#disconnected"+"\n");
			throw new IOException();
		}
	}

	public boolean headerIsReadeable() throws IOException, InterruptedException {
		//if(Log.ON){Log.v(TAG, "HttpGet#headerIsReadeable()");}
		if(mCurrentResponse == null) {
			mCurrentResponse = mCurrentRequest.getResponse(mCurrentSocket);
		}
		return mCurrentResponse.headerIsReadable();
	}

	public boolean bodyIsReadeable() throws IOException, InterruptedException {
		if(Log.ON){Log.v(TAG, "HttpGet#bodyIsReadeable()");}
		if(mCurrentResponse == null) {
			mCurrentResponse = mCurrentRequest.getResponse(mCurrentSocket);
		}
		return mCurrentResponse.bodyIsReadable();
	}

	public static class HttpGetTaskManager {
		public MessageSendTask mSendTaskChain = null;
		public EventTask mLast = null;
	}

}

