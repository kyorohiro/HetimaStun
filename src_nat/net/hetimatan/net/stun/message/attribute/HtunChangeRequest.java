package net.hetimatan.net.stun.message.attribute;

import java.io.IOException;
import java.io.OutputStream;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.net.stun.message.HtunAttribute;
import net.hetimatan.util.io.ByteArrayBuilder;

public class HtunChangeRequest extends HtunAttribute {

	public static final int STATUS_NO_CHANGE_PORT_IP = 0x00;
	public static final int STATUS_CHANGE_PORT = 0x02;
	public static final int STATUS_CHANGE_IP = 0x04;
	public static final int STATUS_CHANGE_IP_PORT = 0x06;

	private int mStatus = 0;

	public HtunChangeRequest(int status) {
		super(HtunAttribute.CHANGE_RESUQEST);
		mStatus = status;
	}
;
	public boolean changeIp() {
		if((mStatus&STATUS_CHANGE_IP) == STATUS_CHANGE_IP) {
			return true;
		} else {
			return false;
		}
	}

	public boolean chagePort() {
		if((mStatus&STATUS_CHANGE_PORT) == STATUS_CHANGE_PORT) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void encode(OutputStream output) throws IOException {
		// 2
		output.write(ByteArrayBuilder.parseShort(
				HtunAttribute.CHANGE_RESUQEST, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// 2
		output.write(ByteArrayBuilder.parseShort(
				4, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// 4
		output.write(ByteArrayBuilder.parseInt(
				mStatus, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		
	}

	public static HtunChangeRequest decode(MarkableFileReader reader) throws IOException {
		int type = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(type != HtunAttribute.CHANGE_RESUQEST) {
			throw new IOException("bad type =" + type);
		}
		int length = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(length != 4) {
			throw new IOException("bad length =" + length);
		}
		int status = MarkableReaderHelper.readInt(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		return new HtunChangeRequest(status);
	}
}
