package net.hetimatan.net.stun.message;

import java.io.IOException;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.io.filen.CashKyoroFileHelper;
import net.hetimatan.net.stun.message.attribute.HtunChangeRequest;
import net.hetimatan.util.url.PercentEncoder;
import junit.framework.TestCase;

public class TestForHtunAttribute extends TestCase {

	public void testChangeRequest() throws IOException {
		HtunChangeRequest changeRequest = new HtunChangeRequest(HtunChangeRequest.STATUS_CHANGE_IP_PORT);
		assertEquals(true, changeRequest.chagePort());
		assertEquals(true, changeRequest.changeIp());
		
		CashKyoroFile file = new CashKyoroFile(100);
		changeRequest.encode(file.getLastOutput());
		byte[] buffer = CashKyoroFileHelper.newBinary(file);
		{
			PercentEncoder encoder = new PercentEncoder();
			System.out.println(encoder.encode(buffer));
		}

		{//encode test
			byte[] result = {
				0x00, 0x03,0x00,0x04,
				0x00, 0x00,0x00,0x06
			};
			for(int i=0;i<result.length;i++) {
				assertEquals("["+i+"]", result[i], buffer[i]);
			}
		}
		
		{//decode test
			byte[] source = {
					0x00, 0x03,0x00,0x04,
					0x00, 0x00,0x00,0x06
				};
			MarkableFileReader reader = new MarkableFileReader(source);
			HtunChangeRequest decodedRequest = HtunChangeRequest.decode(reader);
			assertEquals(true, decodedRequest.chagePort());
			assertEquals(true, decodedRequest.changeIp());
		}
	}
}
