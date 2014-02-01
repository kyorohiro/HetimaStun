package net.hetimatan.net.stun.message.attribute;

import java.io.IOException;
import java.io.OutputStream;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.net.stun.message.HtunAttribute;
import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.io.ByteArrayBuilder;

/**
 * ChangedAddress
 * MappedAddress
 * SourceAddress
 * 
 */
public class HtunXxxAddress extends HtunAttribute {

	private int mFamily = 0;
	private int mPort = 0;
	private byte[] mIp = new byte[4];

	public HtunXxxAddress(int type, int family, byte[] ip) {
		super(type);
		mFamily = family;
		mPort = HttpObject.bToPort(ip, 4);
		for(int i=0;i<4;i++) {
			mIp[i] = ip[i];
		}
	}

	public HtunXxxAddress(int type, int family, int port, byte[] ip) {
		super(type);
		mFamily = family;
		mPort = port;
		for(int i=0;i<4;i++) {
			mIp[i] = ip[i];
		}
	}

	public int getFamily() {
		return mFamily;
	}

	public int getPort() {
		return mPort;
	}

	public byte[] getIp() {
		return mIp;
	}

	@Override
	public void encode(OutputStream output) throws IOException {
		// 2
		output.write(ByteArrayBuilder.parseShort(
				getType(), ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// 2
		output.write(ByteArrayBuilder.parseShort(
				8, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// 2
		output.write(ByteArrayBuilder.parseShort(
				mFamily, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// 2
		output.write(ByteArrayBuilder.parseShort(
				mPort, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// 4
		output.write(mIp);
	}

	public static HtunXxxAddress decode(MarkableFileReader reader) throws IOException {
		// 2 byte type 
		int type = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(!(
				type == HtunAttribute.CHANGE_ADDRESS ||
				type == HtunAttribute.MAPPED_ADDRESS ||
				type == HtunAttribute.SOURCE_ADDRESS)) {
			throw new IOException("bad type =" + type);
		}
		// 2byte length
		int length = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(length != 8) {
			throw new IOException("bad length =" + length);
		}

		// two byte family
		int family = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);

		// two byte port
		int port = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);

		// 4 byte address
		byte[] ip = new byte[4];
		ip[0] = (byte)(0xFF&reader.read());
		ip[1] = (byte)(0xFF&reader.read());
		ip[2] = (byte)(0xFF&reader.read());
		ip[3] = (byte)(0xFF&reader.read());

		return new HtunXxxAddress(type, family, port, ip);
	}
}
