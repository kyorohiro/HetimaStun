package net.hetimatan.util.bitfield;

import java.util.Random;

import net.hetimatan.util.url.PercentEncoder;


public class BitField {
	public static final int[] BIT = {0xFF, 0x80, 0xC0, 0xE0, 0xF0, 0xF8, 0xFC, 0xFE};

	private byte[] mBitfield = new byte[0];
	private int mBitsize = 0;
	private Random mR = null;
	private int[] mShuffleList= new int[]{0,1,2,3,4,5,6,7};

	
	public static BitField relative(BitField ina, BitField inb, BitField out) {
		if(out == null) {
			int len = ina.lengthPerBit();
			out = new BitField(len);
		}
		int len = out.lengthPerByte();
		if(len>inb.lengthPerByte()) {
			len = inb.lengthPerByte();
		}
		for(int i=0;i<out.lengthPerByte();i++) {
			out.mBitfield[i] = (byte)(0xFF&out.mBitfield[i]);
		}
		for(int i=0;i<len;i++) {
			out.mBitfield[i] = (byte)(0xFF&ina.mBitfield[i]&(~inb.mBitfield[i]));
		}
		out.update();
		return out;
	}

	public static int numOfOn(BitField field) {
		int ret = 0;
		int size = field.lengthPerBit();
		for(int i=0;i<size;i++) {
			
		}
		return ret;
	}

	public void update(){}
	public BitField(int bitsize) {
		mR = new Random(System.currentTimeMillis());
		mBitsize = bitsize;
		int byteSize = bitsize/8;
		if((bitsize%8)!=0) {
			byteSize+=1;
		}
		mBitfield = new byte[byteSize];
		oneClear();
	}

	//
	// TODO next work following method is wrong
	//
	public int getOffPieceAtRandom() {
		return getPieceAtRandom(true);
	}

	public int getOnPieceAtRandom() {
		return getPieceAtRandom(false);
	}

	//
	// TODO next work following method is wrong
	//
	public int getPieceAtRandom(boolean isOff) {
		int byteLength  = lengthPerByte();
		if(byteLength<=0) {
			return -1;
		}
		int ia = mR.nextInt(byteLength);
		boolean findedAtIA = false;
		for(int i=ia;i<byteLength;i++) {
			if(isOff) {
				if(!isAllOnPerByte(i)) {
					ia = i;
					findedAtIA = true;break;
				}
			} else {
				if(!isAllOffPerByte(i)) {
					ia = i;
					findedAtIA = true;break;
				}
			}
		}

		if(!findedAtIA) {
			for(int i=ia;i>=0;i--) {
				if(isOff) {
					if(!isAllOnPerByte(i)) {
						ia = i;
						findedAtIA = true;break;
					}
				} else {
					if(!isAllOffPerByte(i)) {
						ia = i;
						findedAtIA = true;break;
					}					
				}
			}
		}
		if(!findedAtIA) {
			return -1;
		}

		if(isOff) {
			return getOffPieceAtRandomPerByte(ia);
		} else {
			return getOnPieceAtRandomPerByte(ia);			
		}
	}

	protected void shuffle(int[] shufflelist) {
		int tmp1 = 0;
		int tmp2 = 0;
		for(int i=0;i<8;i++) {
			tmp1 = mR.nextInt(8);
			tmp2 = shufflelist[i];
			shufflelist[i] = shufflelist[tmp1];
			shufflelist[tmp1] = tmp2;
		}
	}

	public int getOffPieceAtRandomPerByte(int numPerByte) {
		return getPieceAtRandomPerByte(numPerByte, true);
	}

	public int getOnPieceAtRandomPerByte(int numPerByte) {
		return getPieceAtRandomPerByte(numPerByte, false);
	}

	public int getPieceAtRandomPerByte(int numPerByte, boolean isOff) {
		int byteLength  = lengthPerByte();
		if(byteLength<=0) {
			return -1;
		}
		shuffle(mShuffleList);

		int rn = 8;
		if(rn>(lengthPerBit()-numPerByte*8)){
			rn =(lengthPerBit()-numPerByte*8); 
		}
		for(int i=0;i<8;i++) {
			if((numPerByte*8+mShuffleList[i])<lengthPerBit()&&
					isOff != isOn(numPerByte*8+mShuffleList[i])) {
				return (numPerByte*8+mShuffleList[i]);
			}			
		}
		return -1;
	}

	public boolean isAllOff() {
		int len =lengthPerBit();
		for(int i=0;i<len;i++) {
			if(isOn(i)) {
				return false;
			}
		}
		return true;
	}

	public boolean isAllOn() {
		int len =lengthPerBit();
		for(int i=0;i<len;i++) {
			if(!isOn(i)) {
				return false;
			}
		}
		return true;
	}


	public void oneClear() {
		int bitsize = mBitsize;
		int byteSize = bitsize/8;
		if((bitsize%8)!=0) {byteSize+=1;}
		for(int i=0;i<mBitfield.length;i++) {
			mBitfield[i] = (byte)0xFF;
		}
		if(mBitfield.length!=0) {
			mBitfield[byteSize-1]= (byte)(BIT[bitsize%8]&0xFF);
		}
	}

	public void zeroClear() {
		for(int i=0;i<mBitfield.length;i++) {
			mBitfield[i] = 0;
		}
	}

	public int lengthPerBit() {
		return mBitsize;
	}

	public int lengthPerByte() {
		return mBitfield.length;
	}

	public byte[] getBinary() {
		return mBitfield;
	}

	public void setBitfield(byte[] bitfield) {
		int length = bitfield.length;
		if(length > mBitfield.length) {
			length = mBitfield.length;
		}
		System.arraycopy(bitfield, 0, mBitfield, 0, length);
	}

	public void isOn(int number, boolean on) {
		int chunk = number/8;
		int pos = number%8;
		// 8 0, 7 1, 3 3 7 7 
		if(mBitfield == null || chunk>=mBitfield.length||number>=lengthPerBit()) {
			return;
		}

		int value = 0x01<<(7-pos);
		int v = mBitfield[chunk];
		if(on) {
			mBitfield[chunk] = (byte)(v|value);
		} else {
			value = value^0xFFFFFFFF;
			mBitfield[chunk] = (byte)(v&value);
		}
	}

	public boolean isOn(int number) {
		int chunk = number/8;
		int pos = number%8;
		// 8 0, 7 1, 3 3 7 7 
		if(mBitfield == null || chunk>=mBitfield.length) {
			return false;
		}
		if(((mBitfield[chunk]>>(7-pos))&0x01) == 0x01 ) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isAllOnPerByte(int number) {
		int len = lengthPerByte();
		int last = lengthPerBit()%8;

		if(number>=len) {
			return false;
		}
		if(number<(len-1)) {
			if((0xFF&mBitfield[number]) == 0xFF) {
				return true;
			} else {
				return false;
			}
		} else {
			if((0xFF&mBitfield[number]) == (0xFF&BIT[last])) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean isAllOffPerByte(int number) {
		int len = lengthPerByte();
		if(number>=len) {
			return false;
		}
		if((0xFF&mBitfield[number]) == 0x00) {
			return true;
		} else {
			return false;
		}
	}

	public String toURLString() {
		PercentEncoder en = new PercentEncoder();
		return ""+mBitfield.length+":"+en.encode(mBitfield)+"e";
	}
}
