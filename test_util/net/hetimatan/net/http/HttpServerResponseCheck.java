package net.hetimatan.net.http;

import java.io.IOException;



import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.http.HttpRequest;
import net.hetimatan.util.log.Log;

public class HttpServerResponseCheck extends HttpServer {

	private static HttpServerResponseCheck server = null;
	private int mResponseNumber = 0;
	public static void main(String[] args) {
		server = new HttpServerResponseCheck();
		server.setPort(18081);
		server.startServer(null);
	}

	@Override
	public KyoroFile createResponse(HttpServerFront front, KyoroSocket socket, HttpRequest uri) throws IOException {
		HttpHistory.get().pushMessage(sId+"#createResponse:"+front.sId+"\n");
		mResponseNumber++;
		return super.createResponse(front, socket, uri);
	}

	@Override
	public KyoroFile createContent(KyoroSocket socket, HttpRequest uri) throws IOException {
		if(Log.ON){Log.v(TAG, "HttpServer#createResponse");}
		try {
			return new CashKyoroFile("check".getBytes());
		} finally {
			if(Log.ON){Log.v(TAG, "/HttpServer#createResponse");}
		}
	}

	public int getResponseNumber() {
		return mResponseNumber;
	}
	
}
