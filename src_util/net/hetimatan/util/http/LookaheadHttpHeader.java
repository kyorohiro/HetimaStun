package net.hetimatan.util.http;


import java.io.IOException;
import net.hetimatan.io.file.MarkableReader;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.io.ByteArrayBuilder;

@Deprecated
public class LookaheadHttpHeader {
	public static final int EOF = 0;
	public static final int CRLF = 1;
	public static final int KEEP = 2;
	private MarkableReader mCurrentReader = null;
	private long mStartPointer = 0;
	private long mStartTime = 0;
	private boolean mIsFirst = true;

	public LookaheadHttpHeader(MarkableReader reader, int size) throws IOException {
		mCurrentReader = reader;
		mStartPointer = reader.getFilePointer();
		mStartTime = System.currentTimeMillis();
	}

	public long getStart() {
		return mStartPointer;
	}

	public long getElapsedTime() {
		return System.currentTimeMillis()-mStartTime;
	}

	public static final byte[] FIRST_LF1= {'\r', '\n'};
	public static final byte[] FIRST_LF2= {'\n'};

	public static final byte[] LF1= {'\r', '\n', '\r', '\n'};
	public static final byte[] LF2= {'\r', '\n', '\n'};
	public static final byte[] LF3= {'\n', '\r', '\n'};
	public static final byte[] LF4= {'\n', '\n'};
	
	private boolean match(byte[] buf, int bufLen, byte[] pat, boolean oneshot) {
		if(bufLen<pat.length) {
			return false;
		}
		boolean ret = false;

		for(int j=0;j<bufLen;j++) {
			ret = true;
			for(int i=0;i<pat.length;i++) {
				if(buf[i+j]!= pat[i]){
					ret = false;
					break;
				}
			}
			if(oneshot||ret==true){break;}
		}
		return ret;
	}

	public int readByEndOfHeader(boolean checkCrlf) throws IOException {
		MarkableReader reader = mCurrentReader;
		reader.setBlockOn(false);

		ByteArrayBuilder cashBuffer = EventTaskRunner.getByteArrayBuilder();
		cashBuffer.setBufferLength(5*1024);
		cashBuffer.clear();
		byte[] buf = cashBuffer.getBuffer();

		int start = 0;
		int len = 0;
		try {
			reader.pushMark();
			do {
				len =reader.read(buf, start, buf.length);
				if(mIsFirst) {
					if(match(buf, len, FIRST_LF1, true)){return LookaheadHttpHeader.CRLF;}
					if(match(buf, len, FIRST_LF2, true)){return LookaheadHttpHeader.CRLF;}
					if(len >= 2) {
						mIsFirst = false;
					}
				}
				
				if(match(buf, len, LF1, false)){return LookaheadHttpHeader.CRLF;}
				if(match(buf, len, LF2, false)){return LookaheadHttpHeader.CRLF;}
				if(match(buf, len, LF3, false)){return LookaheadHttpHeader.CRLF;}
				if(match(buf, len, LF4, false)){return LookaheadHttpHeader.CRLF;}			
				
				if(reader.isEOF()||len<0) {
					return LookaheadHttpHeader.EOF;
				}
				else if(len < buf.length) {
					return LookaheadHttpHeader.KEEP;
				}
				long next = reader.getFilePointer()-LF1.length;
				if(next<0) {next=0;}
				reader.seek(next);
			} while(true);
		} finally {
			reader.backToMark();
			reader.popMark();
		}
	}

	public static boolean readByEndOfHeader(LookaheadHttpHeader headerChunk, MarkableReader currentReader) throws IOException {
		if (headerChunk == null) {return true;}
		int ret = headerChunk.readByEndOfHeader(true);
		if(ret == LookaheadHttpHeader.CRLF || ret == LookaheadHttpHeader.EOF) {
			return true;
		} else if(ret == LookaheadHttpHeader.KEEP) {
			return false;
		} else {
			return false;
		}
	}

}
