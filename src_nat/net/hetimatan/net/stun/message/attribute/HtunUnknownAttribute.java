package net.hetimatan.net.stun.message.attribute;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.net.stun.message.HtunAttribute;
import net.hetimatan.util.io.ByteArrayBuilder;

public class HtunUnknownAttribute extends HtunAttribute {

	private int mType = 0;
	private LinkedList<Integer> mTypes = new LinkedList<>();

	public HtunUnknownAttribute() {
		super(HtunAttribute.UNKNOWN_ATTRIBUTE);
	}

	public HtunUnknownAttribute(int type) {
		super(HtunAttribute.UNKNOWN_ATTRIBUTE);
		mTypes.add(type);
	}

	@Override
	public void encode(OutputStream output) throws IOException {
		// 2
		output.write(ByteArrayBuilder.parseShort(
				HtunAttribute.UNKNOWN_ATTRIBUTE, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// length 2*type size
		int length = mTypes.size() * 2;
		output.write(ByteArrayBuilder.parseShort(
				2, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		// value
		for(Integer type : mTypes) {
			output.write(ByteArrayBuilder.parseShort(
				type, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));
		}
	}

	public static HtunUnknownAttribute decode(MarkableFileReader reader) throws IOException {
		int type = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(type != HtunAttribute.UNKNOWN_ATTRIBUTE) {
			throw new IOException("bad type =" + type);
		}
		int length = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(length < 0 || length%2!=0) {
			throw new IOException("bad length =" + length);
		}
		HtunUnknownAttribute ret = new HtunUnknownAttribute();
		for(int i=0;i<length/2;i++) {
			int unknownType = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
			ret.addUnknownAttribute(unknownType);
		}
		return ret;
	}

	public void addUnknownAttribute(int unknownType) {
		mTypes.add(unknownType);
	}

	public int getUnknownAttributeType(int index) {
		return mTypes.get(index);
	}

	public int numOfUnknownAttributeType() {
		return mTypes.size();
	}

}
