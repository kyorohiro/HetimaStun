package net.hetimatan.util.io.net;

import java.io.IOException;
import java.net.UnknownHostException;

import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.event.net.mock.KyoroDatagramMock;
import net.hetimatan.util.http.HttpObject;
import junit.framework.TestCase;
import net.hetimatan.util.test.*;
public class TestForMockUDP extends TestCase {

	public void testSendReceive() throws UnknownHostException, IOException {
		KyoroDatagramMock d1 = new KyoroDatagramMock(KyoroDatagramMock.NAT_TYPE_FULL_CONE, 0);
		KyoroDatagramMock d2 = new KyoroDatagramMock(KyoroDatagramMock.NAT_TYPE_FULL_CONE, 1);
		try {
			d1.bind(HttpObject.address("127.0.0.1", 800));
			d2.bind(HttpObject.address("127.0.0.2", 801));

			d1.send("abc".getBytes(), d2.getMappedIp());
			byte[] receiveAddress = d2.receive();
			byte[] receiveData = d2.getByte();
			{
				TestUtil.assertArrayEquals(this, "..", "abc".getBytes(), receiveData);
				TestUtil.assertArrayEquals(this, "..", d1.getMappedIp(), receiveAddress);
			}
		} finally {
			d1.close();
			d2.close();
		}
	}

	public void testSelector() throws UnknownHostException, IOException {
		KyoroSelector selector = new KyoroSelector();
		//		KyoroSocketEventRunner runner = new KyoroSocketEventRunner();
		KyoroDatagramMock d1 = new KyoroDatagramMock(KyoroDatagramMock.NAT_TYPE_FULL_CONE, 0);
		KyoroDatagramMock d2 = new KyoroDatagramMock(KyoroDatagramMock.NAT_TYPE_FULL_CONE, 1);
		try {
			d1.bind(HttpObject.address("127.0.0.1", 800));
			d2.bind(HttpObject.address("127.0.0.2", 801));

			d1.regist(selector, KyoroSelector.READ);
			selector.select(0);
			assertEquals(false, selector.next());
			
			d2.send("abc".getBytes(), d1.getMappedIp());
			selector.select(0);
			assertEquals(true, selector.next());

			byte[] receiveAddress = d1.receive();
			byte[] receiveData = d1.getByte();
			{
				TestUtil.assertArrayEquals(this, "..", "abc".getBytes(), receiveData);
				TestUtil.assertArrayEquals(this, "..", d2.getMappedIp(), receiveAddress);
			}

			selector.select(0);
			assertEquals(false, selector.next());
		} finally {
			d1.close();
			d2.close();
		}
	}

	public void testNatTest() throws UnknownHostException, IOException {
		KyoroDatagramMock d1 = new KyoroDatagramMock(KyoroDatagramMock.NAT_TYPE_FULL_CONE, 0);
		KyoroDatagramMock d2 = new KyoroDatagramMock(KyoroDatagramMock.NAT_TYPE_RESTRICTED_PORT, 1);
		try {
			d1.bind(HttpObject.address("127.0.0.1", 800));
			d2.bind(HttpObject.address("127.0.0.2", 801));

			try {
				d1.send("...".getBytes(), d2.getMappedIp());
				assertEquals(0, 1);
			} catch(Exception e) {
			}

			try {
				d2.send("....".getBytes(), d1.getMappedIp());
			} catch(Exception e) {
			}

			{
				d1.send("abc".getBytes(), d2.getMappedIp());
			}

			byte[] receiveAddress = d2.receive();
			byte[] receiveData = d2.getByte();
			{
				TestUtil.assertArrayEquals(this, "..", "abc".getBytes(), receiveData);
				TestUtil.assertArrayEquals(this, "..", d1.getMappedIp(), receiveAddress);
			}
		} finally {
			d1.close();
			d2.close();
		}
	}

}
