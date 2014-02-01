package net.hetimatan.util.url;

import java.io.IOException;

import net.hetimatan.util.io.ByteArrayBuilder;


/**
 * PerentEncoder
 * 
 * 80% 
 */
public class PercentEncoder {
	private ByteArrayBuilder mBuilder = new ByteArrayBuilder();
	private static final byte[] sEncodeMap = {
		'0','1','2','3','4','5','6',
		'7','8','9','A','B','C','D','E','F'};

	public synchronized byte[] decode(byte[] input) throws IOException {
		mBuilder.clear();

		int len = input.length;
		for(int i=0;i<len;) {
			byte atom = input[i];
			if(atom!='%') {
				mBuilder.append(atom);
				i+=1;
			} else {				
				mBuilder.append(convertChar2Byte(input, i));
				i+=3;
			}
		}
		
		byte[] output = new byte[mBuilder.length()];
		System.arraycopy(mBuilder.getBuffer(), 0, output, 0, output.length);
		return output;
	}

	public synchronized String encode(byte[] input) {
		return encode(input, 0, input.length);
	}

	public synchronized String encode(byte[] input, int start, int end) {
		mBuilder.clear();
		for(int i=start;i<end;i++) {
			byte atom = input[i];
			if(isConvert(atom)){
				encodeAtPersent(atom);
			} else {
				mBuilder.append(atom);
			}
		}
		return new String(mBuilder.getBuffer(), 0, mBuilder.length());
	}

	public static byte convertChar2Byte(byte[] percentData, int i) throws IOException {
		byte upper = 0;
		byte lower = 0;
		upper = convertChar2HalfByte(percentData[i+1]);
		lower = convertChar2HalfByte(percentData[i+2]);
		return (byte)((upper<<4)|lower);
	}

	public static byte convertChar2HalfByte(byte c) throws IOException {
		if ('0'<=c&&c<='9') {
			return (byte)(c-'0');
		} else if ('a'<=c&&c<='f') {
			return (byte)(c-'a'+10);			
		} else if ('A'<=c&&c<='F') {
			return (byte)(c-'A'+10);	
		}
		throw new IOException();
	}

	protected boolean isConvert(byte atom) {
		if ('0'<=atom&&atom<='9') {
			return false;
		} else if ('a'<=atom&&atom<='z') {
			return false;			
		} else if ('A'<=atom&&atom<='Z') {
			return false;
		}
		return true;
	}

	private void encodeAtPersent(byte atom) {
		mBuilder.append((byte)'%');
		mBuilder.append(sEncodeMap[(0xF0&atom)>>4]);
		mBuilder.append(sEncodeMap[atom&0xF]);
	}
}
