package net.hetimatan.net.stun.message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.file.MarkableReaderHelper;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.io.filen.CashKyoroFileHelper;
import net.hetimatan.util.io.ByteArrayBuilder;

public class HtunHeader {
	public static final int BINDING_REQUEST = 0x01;
	public static final int BINDING_RESPONSE = 0x101;
	public static final int BINDING_ERROR_RESPONSE = 0x111;
	public static final int SHARED_SECRET_REQUEST = 0x002;
	public static final int SHARED_SECRET_RESPONSE = 0x102;
	public static final int SHARED_SECRET_ERROR_RESPONSE = 0x112;

	private int mType = 0;
	private byte[] mId = new byte[16];
	private LinkedList<HtunAttribute> mAttributes = new LinkedList<>();

	public HtunHeader(int requestType, byte[] id) {
		mType = requestType;
		for(int i=0;i<16;i++) {
			mId[i] = id[i];
		}
	}

	public int getType() {
		return mType;
	}

	public byte[] getId() {
		return mId;
	}

	public void addAttribute(HtunAttribute attribute) {
		mAttributes.add(attribute);
	}

	public HtunAttribute getHtunAttribute(int index) {
		return mAttributes.get(index);
	}

	public HtunAttribute findHtunAttribute(int type) {
		for(int i=0;i<mAttributes.size();i++) {
			HtunAttribute attibute =  mAttributes.get(i);
			if(attibute != null) {
				if(type == attibute.getType()) {
					return attibute;
				};
			}
		}
		return null;
	}

	public int numOfAttribute() {
		return mAttributes.size();
	}

	public void encode(OutputStream output) throws IOException {
		// 2byte zero sign 
		output.write(ByteArrayBuilder.parseShort(0, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));

		// 2byte message type 
		output.write(ByteArrayBuilder.parseShort(mType, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));

		// create body 
		CashKyoroFile tmp = new CashKyoroFile(1024);
		for(int i=0;i<mAttributes.size();i++) {
			HtunAttribute attribute = mAttributes.get(i);
			attribute.encode(tmp.getLastOutput());
		}
		
		// 2byte message length 
		output.write(ByteArrayBuilder.parseShort((int)tmp.length(), ByteArrayBuilder.BYTEORDER_BIG_ENDIAN));

		output.write(mId);
		// nbyte messae body
		output.write(CashKyoroFileHelper.newBinary(tmp));

	}

	public static HtunHeader decode(MarkableFileReader reader) throws IOException {
		// 2byte zero sign.  
		int sign = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(sign != 0) {throw new IOException("bad sign "+sign);}

		// 2byte message type
		int messageType = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		
		// 2byte message body length
		int messageLength = MarkableReaderHelper.readShort(reader, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		if(messageLength<0) {
			throw new IOException("bad messageLength len="+messageLength );			
		}
		
		// id length
		byte[] id = new byte[16];
		int ret = reader.read(id);
		if (ret != 16) {
			throw new IOException("bad id len="+ret);
		}

		HtunHeader header = new HtunHeader(messageType, id);
		// data
		int begin = (int)reader.getFilePointer();
		int end = begin+messageLength;
		while(reader.getFilePointer()<end) {
			header.addAttribute(HtunAttribute.decode(reader));
		}
		return header;
	}

}
