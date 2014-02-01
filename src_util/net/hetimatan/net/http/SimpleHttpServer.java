package net.hetimatan.net.http;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.KyoroFileForFiles;
import net.hetimatan.io.filen.ByteKyoroFile;
import net.hetimatan.io.filen.CashKyoroFileHelper;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.net.http.HttpServerFront;
import net.hetimatan.net.http.HttpServer;
import net.hetimatan.util.event.net.io.KyoroSocket;
import net.hetimatan.util.http.HttpObjectHelper;
import net.hetimatan.util.http.HttpRequest;

//@todo
public class SimpleHttpServer extends HttpServer {

	private CashKyoroFile mFile = null;
	private LinkedHashMap<String, String> mMimetype = new LinkedHashMap<String,String>();
	{
		mMimetype.put("mp4", "video/mp4");
		mMimetype.put("txt", "text/plain");
		mMimetype.put("html", "text/html");
	}

	public SimpleHttpServer() { 
		try {
			mFile = new CashKyoroFile(new File("../../h264.mp4"), 16*1024, 4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public KyoroFile createResponse(HttpServerFront front, KyoroSocket socket, HttpRequest uri) throws IOException {
		String rangeHeader = uri.getHeaderValue("Range");
		boolean isRange = false;
		PieceInfoList list = null;
		if(rangeHeader != null && rangeHeader.length() != 0) {
			list = HttpObjectHelper.getRangeList(rangeHeader,mFile.length());
			if(list.size()>0) {
				isRange = true;
			}
		}
		if(!isRange) {
			return createDefaultResponse(front, socket, uri);
		} else {
			return createSingleRangeResponse(list.getPieceInfo(0), front, socket, uri);
//			return createMultiRangeResponse(list.getPieceInfo(0), front, socket, uri);
		}
	}

	public String getMimeType(String path) {
		Set<String> keyset = mMimetype.keySet();
		for(String key:keyset) {
			if(path.endsWith(key)){
				return mMimetype.get(key);
			}
		}
		return "text/plain";
	}

	public KyoroFile createDefaultResponse(HttpServerFront front, KyoroSocket socket, HttpRequest uri) throws IOException {
		KyoroFile content = CashKyoroFileHelper.subSequence(mFile, 0, mFile.length());
		KyoroFile header = new ByteKyoroFile();
		String path =uri.getLine().getRequestURI().getPath();
		header.addChunk(("HTTP/1.1 200 OK\r\n").getBytes());
		header.addChunk(("Content-Length: "+content.length()+"\r\n").getBytes());
		header.addChunk(("Connection: close\r\n").getBytes());
		header.addChunk(("Content-Type: "+getMimeType(path)+"\r\n").getBytes());
		header.addChunk(("\r\n").getBytes());

		KyoroFile[] files = new KyoroFile[2];
		files[0] = header;files[0].seek(0);
		files[1] = content;files[1].seek(0);
		KyoroFileForFiles kfiles = new KyoroFileForFiles(files);
		kfiles.seek(0);
		return kfiles;
	}

	public KyoroFile createSingleRangeResponse(PieceInfo piece, HttpServerFront front, KyoroSocket socket, HttpRequest uri) throws IOException {
		KyoroFile content = CashKyoroFileHelper.subSequence(mFile, piece.getStart(), piece.getEnd()+1);
		KyoroFile header = new ByteKyoroFile();
		String path =uri.getLine().getRequestURI().getPath();
		header.addChunk(("HTTP/1.1 206 Partial Content\r\n").getBytes());
		header.addChunk(("Content-Length: "+content.length()+"\r\n").getBytes());
		header.addChunk(("Connection: close\r\n").getBytes());
		header.addChunk(("Content-Type: "+getMimeType(path)+"\r\n").getBytes());
		header.addChunk(("Content-Range: bytes "+piece.getStart()+"-"+piece.getEnd()+"/"+mFile.length()+"\r\n").getBytes());
		header.addChunk(("\r\n").getBytes());

		KyoroFile[] files = new KyoroFile[2];
		files[0] = header;files[0].seek(0);
		files[1] = content;files[1].seek(0);
		KyoroFileForFiles kfiles = new KyoroFileForFiles(files);
		kfiles.seek(0);
		return kfiles;
	}

	
	public static SimpleHttpServer sServer = null;
	public static void main(String[] args) {
		SimpleHttpServer server = new SimpleHttpServer();
		server.setPort(8888);
		server.startServer(null);
		sServer = server;
	}
}

