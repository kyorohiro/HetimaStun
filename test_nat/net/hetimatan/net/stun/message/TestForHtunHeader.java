package net.hetimatan.net.stun.message;

import java.io.IOException;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.io.filen.CashKyoroFileHelper;
import net.hetimatan.net.stun.HtunServer;
import net.hetimatan.net.stun.message.attribute.HtunChangeRequest;
import net.hetimatan.net.stun.message.attribute.HtunXxxAddress;
import net.hetimatan.util.http.HttpObject;
import junit.framework.TestCase;

public class TestForHtunHeader extends TestCase {
	public void testEmptyAttribute() throws IOException {
		byte[] id = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		HtunHeader header = new HtunHeader(HtunHeader.BINDING_REQUEST, id);
		CashKyoroFile output = new CashKyoroFile(1000);
		header.encode(output.getLastOutput());
		byte[] buffer = CashKyoroFileHelper.newBinary(output);
		{
			byte[] expected = {
					0x00, 0x00, 
					0x00, HtunHeader.BINDING_REQUEST,
					0x00, 0x00, // length
					1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 //id
			};
			for(int i=0;i<expected.length;i++) {
				assertEquals("["+i+"]", expected[i], buffer[i]);
			}
		}

		{//decode
			MarkableFileReader reader = new MarkableFileReader(buffer);
			HtunHeader exHeader = HtunHeader.decode(reader);
			assertEquals(HtunHeader.BINDING_REQUEST, exHeader.getType());
			reader.close();
		}
	}


	public void testChangeReauestAttribute() throws IOException {
		byte[] id = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		HtunHeader header = new HtunHeader(HtunHeader.BINDING_REQUEST, id);
		header.addAttribute(new HtunChangeRequest(HtunChangeRequest.STATUS_CHANGE_IP));
		CashKyoroFile output = new CashKyoroFile(1000);
		header.encode(output.getLastOutput());
		byte[] buffer = CashKyoroFileHelper.newBinary(output);
		{
			byte[] expected = {
					0x00, 0x00, 
					0x00, HtunHeader.BINDING_REQUEST,
					0x00, 0x08, // length
					1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, //id
					0x00, HtunAttribute.CHANGE_RESUQEST,
					0x00, 0x04,
					0x00, 0x00, 0x00, HtunChangeRequest.STATUS_CHANGE_IP
			};
			for(int i=0;i<expected.length;i++) {
				assertEquals("["+i+"]", expected[i], buffer[i]);
			}
		}

		{//decode
			MarkableFileReader reader = new MarkableFileReader(buffer);
			HtunHeader exHeader = HtunHeader.decode(reader);
			assertEquals(HtunHeader.BINDING_REQUEST, exHeader.getType());
			assertEquals(1, exHeader.numOfAttribute());
			assertEquals(false, ((HtunChangeRequest)exHeader.getHtunAttribute(0)).chagePort());
			assertEquals(true, ((HtunChangeRequest)exHeader.getHtunAttribute(0)).changeIp());
			
			reader.close();
		}
	}

	public void testChangeResponseAttribute() throws IOException {
		byte[] id = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		HtunHeader header = new HtunHeader(HtunHeader.BINDING_RESPONSE, id);
		header.addAttribute(new HtunXxxAddress(
				HtunAttribute.SOURCE_ADDRESS, 0x01,
				800, HttpObject.aton("127.0.0.1")
				));
		
		// encode test
		CashKyoroFile output = new CashKyoroFile(1000);
		header.encode(output.getLastOutput());
		byte[] buffer = CashKyoroFileHelper.newBinary(output);
		{
			byte[] expected = {
					0x00, 0x00, 
					0xFF&(HtunHeader.BINDING_RESPONSE>>8), 0xFF&HtunHeader.BINDING_RESPONSE,
					0x00, 0x0C, // length
					1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, //id
					0x00, HtunAttribute.SOURCE_ADDRESS,
					0x00, 0x08, //length
					0x00, 0x01, //family
					0x03, (byte)(0xFF&0x20), //port
					127, 0, 0, 1
			};
			for(int i=0;i<expected.length;i++) {
				assertEquals("["+i+"]", 0xFF&expected[i], 0xFF&buffer[i]);
			}
		}
		//decode test
		{//decode
			MarkableFileReader reader = new MarkableFileReader(buffer);
			HtunHeader exHeader = HtunHeader.decode(reader);
			assertEquals(HtunHeader.BINDING_RESPONSE, exHeader.getType());
			assertEquals(1, exHeader.numOfAttribute());
			assertEquals(HtunAttribute.SOURCE_ADDRESS, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getType());
			assertEquals(0x01, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getFamily());
			assertEquals(800, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getPort());
			assertEquals(127, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[0]);
			assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[1]);
			assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[2]);
			assertEquals(1, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[3]);
			
			reader.close();
		}
	}

	public void testChangeResponseAttribute001() throws IOException {
		byte[] id = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		HtunHeader header = new HtunHeader(HtunHeader.BINDING_RESPONSE, id);
		header.addAttribute(new HtunXxxAddress(
				HtunAttribute.SOURCE_ADDRESS, 0x01,
				800, HttpObject.aton("127.0.0.1")
				));
		header.addAttribute(new HtunXxxAddress(
				HtunAttribute.MAPPED_ADDRESS, 0x01,
				800, HttpObject.aton("127.0.0.1")
				));
		header.addAttribute(new HtunXxxAddress(
				HtunAttribute.CHANGE_ADDRESS, 0x01,
				800, HttpObject.aton("127.0.0.1")
				));
		
		// encode test
		CashKyoroFile output = new CashKyoroFile(1000);
		header.encode(output.getLastOutput());
		byte[] buffer = CashKyoroFileHelper.newBinary(output);
		{
			byte[] expected = {
					0x00, 0x00, 
					0xFF&(HtunHeader.BINDING_RESPONSE>>8), 0xFF&HtunHeader.BINDING_RESPONSE,
					0x00, 36, // length
					1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, //id
					0x00, HtunAttribute.SOURCE_ADDRESS,
					0x00, 0x08, //length
					0x00, 0x01, //family
					0x03, (byte)(0xFF&0x20), //port
					127, 0, 0, 1,
					0x00, HtunAttribute.MAPPED_ADDRESS,
					0x00, 0x08, //length
					0x00, 0x01, //family
					0x03, (byte)(0xFF&0x20), //port
					127, 0, 0, 1,
					0x00, HtunAttribute.CHANGE_ADDRESS,
					0x00, 0x08, //length
					0x00, 0x01, //family
					0x03, (byte)(0xFF&0x20), //port
					127, 0, 0, 1,

			};
			for(int i=0;i<expected.length;i++) {
				assertEquals("["+i+"]", 0xFF&expected[i], 0xFF&buffer[i]);
			}
		}
		//decode test
		{//decode
			MarkableFileReader reader = new MarkableFileReader(buffer);
			HtunHeader exHeader = HtunHeader.decode(reader);
			assertEquals(HtunHeader.BINDING_RESPONSE, exHeader.getType());
			assertEquals(3, exHeader.numOfAttribute());
			{
				assertEquals(HtunAttribute.SOURCE_ADDRESS, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getType());
				assertEquals(0x01, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getFamily());
				assertEquals(800, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getPort());
				assertEquals(127, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[0]);
				assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[1]);
				assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[2]);
				assertEquals(1, ((HtunXxxAddress)exHeader.getHtunAttribute(0)).getIp()[3]);
			}
			{
				assertEquals(HtunAttribute.MAPPED_ADDRESS, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getType());
				assertEquals(0x01, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getFamily());
				assertEquals(800, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getPort());
				assertEquals(127, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getIp()[0]);
				assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getIp()[1]);
				assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getIp()[2]);
				assertEquals(1, ((HtunXxxAddress)exHeader.getHtunAttribute(1)).getIp()[3]);
			}
			{
				assertEquals(HtunAttribute.CHANGE_ADDRESS, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getType());
				assertEquals(0x01, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getFamily());
				assertEquals(800, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getPort());
				assertEquals(127, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getIp()[0]);
				assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getIp()[1]);
				assertEquals(0, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getIp()[2]);
				assertEquals(1, ((HtunXxxAddress)exHeader.getHtunAttribute(2)).getIp()[3]);
			}

			reader.close();
		}
	}
}
