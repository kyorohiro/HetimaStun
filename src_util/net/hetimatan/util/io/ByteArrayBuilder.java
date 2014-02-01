package net.hetimatan.util.io;

/**
 * Variable byte array
 * 
 * 80%
 */
public class ByteArrayBuilder {
	public static final int BYTEORDER_BIG_ENDIAN = 1;
	public static final int BYTEORDER_LITTLE_ENDIAN = 0;
	private int mByteOrder = BYTEORDER_BIG_ENDIAN;
	private int mPointer = 0;
	private byte[] mBuffer = null;

	public ByteArrayBuilder() {
		mBuffer = new byte[256];
	}

	public ByteArrayBuilder(int size) {
		mBuffer = new byte[size];
	}

	public void append(byte[] moji){
		append(moji, 0, moji.length);
	}

	public void append(byte[] moji, int offset, int len){
		for(int i=offset;i<len+offset;i++) {
			append(moji[i]);
		}
	}

	public void append(byte moji){
		if(mPointer >= mBuffer.length){
			updateBuffer();
		}
		mBuffer[mPointer] = moji;
		mPointer++;
	}

	private void updateBuffer() {
		updateBuffer(mBuffer.length*2);
	}

	private void updateBuffer(int length) {
		byte[] tmp = new byte[length];
		for(int i=0;i<mBuffer.length;i++) {
			tmp[i] = mBuffer[i];
		}
		mBuffer = tmp;
	}

	public void setBufferLength(int length) {
		if(mBuffer.length < length) {
			updateBuffer(length>mBuffer.length*2?length:mBuffer.length*2);
		}
	}

	//
	// 1111 1111
	// 001111 11
	public void leftShift(int size) {
		if(size<length()) {
			int len = length();
			int i=0;
			for(i=len;0<=i-size;i--) {
				mBuffer[i] = mBuffer[i-size];
			}
			for(;0<=i;i--) {
				mBuffer[i] = 0;
			}
			mPointer -= size;
		} else {
			mPointer = 0;
		}		
	}
	//
	// 1111 1111
	// 1111 1100
	public void shift(int size) {
		if(size<length()) {
			int len = length();
			int i=0;
			for(i=0;i+size<len;i++) {
				mBuffer[0+i] = mBuffer[size+i];
			}
			for(;i<len;i++) {
				mBuffer[i] = 0;
			}
			mPointer -= size;
		} else {
			mPointer = 0;
		}
	}

	public void zeroClear() {
		int len = length();
		for(int i=0;i<len;i++) {
			mBuffer[i] = 0;
		}		
	}

	public void clear() {
//		zeroClear();
		mPointer = 0;
	}

	public byte[] getBuffer(){
		return mBuffer;
	}

	public byte[] createBuffer() {
		byte[] buffer = new byte[length()];
		System.arraycopy(getBuffer(), 0, buffer, 0, buffer.length);
		return buffer;
	}

	public byte getLast() {
		if(0<mBuffer.length) {
			return mBuffer[mBuffer.length-1];
		} else {
			return 0;
		}
	}

	public int length(){
		return mPointer;
	}

	public void removeLast(){
		if(0<mPointer) {
			mPointer--;
		}
	}

	public void setPointer(int pointer) {
		mPointer = pointer;
	}

	public void appendInt(int value) {
		if (mByteOrder == BYTEORDER_BIG_ENDIAN) {
			append((byte) ((value >> 24) & 0xff));
			append((byte) ((value >> 16) & 0xff));
			append((byte) ((value >>  8) & 0xff));
			append((byte) ((value >>  0) & 0xff));
		} else {
			append((byte) ((value >>  0) & 0xff));
			append((byte) ((value >>  8) & 0xff));
			append((byte) ((value >> 16) & 0xff));
			append((byte) ((value >> 24) & 0xff));
		}
	}

	public static byte[] parseInt(int value, int byteOrder) {
		byte[] ret = new byte[4];
		if (byteOrder == BYTEORDER_BIG_ENDIAN) {
			ret[0] = ((byte) ((value >> 24) & 0xff));
			ret[1] = ((byte) ((value >> 16) & 0xff));
			ret[2] = ((byte) ((value >> 8) & 0xff));
			ret[3] = ((byte) ((value >> 0) & 0xff));
		} else {
			ret[0] = ((byte) ((value >> 0) & 0xff));
			ret[1] = ((byte) ((value >> 8) & 0xff));
			ret[2] = ((byte) ((value >> 16) & 0xff));
			ret[3] = ((byte) ((value >> 24) & 0xff));
		}
		return ret;
	}

	public static int parseInt(byte[] buffer, int byteOrder) {
		int ret = 0;
		if (byteOrder == BYTEORDER_BIG_ENDIAN) {
			ret = ret|((buffer[0]&0xff)<<24);
			ret = ret|((buffer[1]&0xff)<<16);
			ret = ret|((buffer[2]&0xff)<<8);
			ret = ret|((buffer[3]&0xff)<<0);
		} else {
			ret = ret|((buffer[3]&0xff)<<24);
			ret = ret|((buffer[2]&0xff)<<16);
			ret = ret|((buffer[1]&0xff)<<8);
			ret = ret|((buffer[0]&0xff)<<0);
		}
		return ret;
	}

	public static byte[] parseShort(int value, int byteOrder) {
		byte[] ret = new byte[2];
		if (byteOrder == BYTEORDER_BIG_ENDIAN) {
			ret[0] = ((byte) ((value >> 8) & 0xff));
			ret[1] = ((byte) ((value >> 0) & 0xff));
		} else {
			ret[1] = ((byte) ((value >> 0) & 0xff));
			ret[0] = ((byte) ((value >> 8) & 0xff));
		}
		return ret;
	}

	public static int parseShort(byte[] buffer, int begin, int byteOrder) {
		int ret = 0;
		if (byteOrder == BYTEORDER_BIG_ENDIAN) {
			ret = ret|((buffer[0+begin]&0xff)<<8);
			ret = ret|((buffer[1+begin]&0xff)<<0);
		} else {
			ret = ret|((buffer[1+begin]&0xff)<<8);
			ret = ret|((buffer[0+begin]&0xff)<<0);
		}
		return ret;
	}

}
