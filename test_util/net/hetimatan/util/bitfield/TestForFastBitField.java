package net.hetimatan.util.bitfield;

public class TestForFastBitField extends TestForBitField {

	BitField newBitField(int bitsize) {
		return new FastBitField(bitsize);
	}

	public void testfast_bitsizeIs72() {
		FastBitField bitfield = (FastBitField)newBitField(71);
		assertEquals(9, bitfield.getBinary().length);
		assertEquals(71, bitfield.lengthPerBit());
		assertEquals(9, bitfield.lengthPerByte());
		assertEquals(false, bitfield.isAllOff());
		assertEquals(true, bitfield.isAllOn());
		assertEquals(0xFF, 0xFF&bitfield.getBinary()[0]);
		assertEquals(0xFE, 0xFF&bitfield.getBinary()[8]);

		BitField index = bitfield.getIndex();
		assertEquals(2, index.lengthPerBit());
		assertEquals(1, index.lengthPerByte());
		assertEquals(true, index.isOn(0));
		assertEquals(true, index.isOn(1));

		bitfield.isOn(0, true);
		bitfield.isOn(70, false);
		bitfield.isOn(69, false);
		bitfield.isOn(68, false);
		bitfield.isOn(67, false);
		bitfield.isOn(66, false);
		bitfield.isOn(65, false);
		assertEquals(false, index.isOn(1));
		bitfield.isOn(64, false);

		assertEquals(true, index.isOn(0));
		assertEquals(false, index.isOn(1));
		
		bitfield.isOn(67, true);
		assertEquals(true, index.isOn(0));
		assertEquals(false, index.isOn(1));

	}

	public void testfast_setBitField() {
		{
			FastBitField fbit = new FastBitField(1);
			byte[] bitfield = new byte[]{(byte)0xFF};
			fbit.setBitfield(bitfield);
			assertEquals(true, fbit.getIndex().isAllOn());			
		}
		{
			FastBitField fbit = new FastBitField(1);
			byte[] bitfield = new byte[]{(byte)0xFE};
			fbit.setBitfield(bitfield);
			assertEquals(false, fbit.getIndex().isAllOn());			
		}
		{
			FastBitField fbit = new FastBitField(8*8);
			byte[] bitfield = new byte[]{
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
			fbit.setBitfield(bitfield);
			assertEquals(true, fbit.getIndex().isAllOn());	
		}
		{
			FastBitField fbit = new FastBitField(8*8);
			byte[] bitfield = new byte[] {
					(byte)0xFF,(byte)0xFE,(byte)0xFF,(byte)0xFF,
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
			fbit.setBitfield(bitfield);
			assertEquals(false, fbit.getIndex().isAllOn());	
			assertEquals(1, fbit.getIndex().lengthPerBit());	
		}

		{
			FastBitField fbit = new FastBitField(8*8*3);
			byte[] bitfield = new byte[] {
					(byte)0xFF,(byte)0xFE,(byte)0xFF,(byte)0xFF,
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF};
			fbit.setBitfield(bitfield);
			assertEquals(false, fbit.getIndex().isAllOn());	
			assertEquals(false, fbit.getIndex().isOn(0));
			assertEquals(true, fbit.getIndex().isOn(1));
			assertEquals(true, fbit.getIndex().isOn(2));
			assertEquals(3, fbit.getIndex().lengthPerBit());	
		}

		{
			FastBitField fbit = new FastBitField(8*8*3);
			byte[] bitfield = new byte[] {
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
					(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,
					(byte)0xFF,(byte)0xFF,(byte)0xFE,(byte)0xFF
			};
			fbit.setBitfield(bitfield);
			assertEquals(false, fbit.getIndex().isAllOn());	
			assertEquals(true, fbit.getIndex().isOn(0));
			assertEquals(false, fbit.getIndex().isOn(1));
			assertEquals(true, fbit.getIndex().isOn(2));
			assertEquals(3, fbit.getIndex().lengthPerBit());	
		}
	}
}
