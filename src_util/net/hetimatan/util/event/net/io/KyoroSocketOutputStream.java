package net.hetimatan.util.event.net.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.filen.CashKyoroFile;

public class KyoroSocketOutputStream extends OutputStream {

	private KyoroSocket mSocket = null;
	private boolean mIsBlock = true;
	private KyoroFile mCash = null;

	public KyoroSocketOutputStream(KyoroSocket socket) {
		mSocket = socket;
	}

	public void writeToCash(byte[] b, int off, int len) throws IOException {
		if(mCash == null) {
			mCash = new CashKyoroFile(1024, 256);
		}
		mCash.addChunk(b, off, off+len);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int writedOneshot = 0;
		int writedAll = 0;
		do {
			writedOneshot = mSocket.write(b, off+writedAll, len-writedAll);
			if(writedOneshot<0) {
				break;
			}
			writedAll += writedOneshot;
			System.out.println("##"+writedAll+"/"+len);
		} while(writedAll<len);
	}

	@Override
	public void write(int b) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		buffer.put((byte)(0xFF&b));
		buffer.flip();
		mSocket.write(buffer.array(), 0, 1);
	}

	@Override
	public void close() throws IOException {
		if(mCash != null) {
			mCash.close();
		}
		super.close();
	}
}
