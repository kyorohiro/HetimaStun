package net.hetimatan.net.stun;

import java.io.IOException;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.io.filen.CashKyoroFileHelper;
import net.hetimatan.net.stun.HtunServer.ReceiveTask;
import net.hetimatan.net.stun.HtunServer.SendTask;
import net.hetimatan.net.stun.message.HtunAttribute;
import net.hetimatan.net.stun.message.HtunHeader;
import net.hetimatan.net.stun.message.attribute.HtunChangeRequest;
import net.hetimatan.net.stun.message.attribute.HtunXxxAddress;
import net.hetimatan.util.event.EventTask;
import net.hetimatan.util.event.EventTaskRunner;
import net.hetimatan.util.event.net.KyoroSocketEventRunner;
import net.hetimatan.util.event.net.io.KyoroDatagramImpl;
import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.url.PercentEncoder;

public class HtunClient {

	private KyoroSocketEventRunner mRunner = null;
	private KyoroDatagramImpl mDatagramSocket = null;
	private byte[] mStunIp;
	private byte[] mMainIp;
	
	public HtunClient(byte[] mainIp, byte[] stunIp) {
		mStunIp = stunIp;
		mMainIp = mainIp;	
	}

	public void init() throws IOException {
		mDatagramSocket = new KyoroDatagramImpl();
		mDatagramSocket.bind(mMainIp);
	}

	public KyoroSocketEventRunner startTask(KyoroSocketEventRunner runner) throws IOException {
		init();
		if(runner == null) {
			mRunner = new KyoroSocketEventRunner();
		}
		mRunner.waitIsSelect(true);
		mDatagramSocket.regist(mRunner.getSelector(), KyoroSelector.READ);
		mDatagramSocket.setEventTaskAtWrakReference(new ReceiveTask(), KyoroSelector.READ);
		
		byte[] id = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
		HtunHeader header = new HtunHeader(HtunHeader.BINDING_REQUEST, id);
		header.addAttribute(new HtunChangeRequest(HtunChangeRequest.STATUS_NO_CHANGE_PORT_IP));
		
		byte[] buffer = null;
		{
			CashKyoroFile output = new CashKyoroFile(1024);
			header.encode(output.getLastOutput());
			buffer = CashKyoroFileHelper.newBinary(output);
		}
		mRunner.pushTask(new SendTask(mStunIp, buffer), 3000);
		return mRunner;
	}

	public class ReceiveTask extends EventTask {
		@Override
		public void action(EventTaskRunner runner) throws Throwable {
			PercentEncoder encoder = new PercentEncoder();
			byte[] ip = mDatagramSocket.receive();
			byte[] buffer = mDatagramSocket.getByte();
			MarkableFileReader reader = new MarkableFileReader(buffer);
			HtunHeader header = HtunHeader.decode(reader);
			
			{
				System.out.println("####################################");
				System.out.println("##ID="+encoder.encode(header.getId()));
			}
			{
				HtunXxxAddress mappedAddress =(HtunXxxAddress)header.findHtunAttribute(HtunAttribute.MAPPED_ADDRESS);
				if(mappedAddress != null) {
					System.out.println("##MAPPED IP="+encoder.encode(mappedAddress.getIp()));
					System.out.println("##MAPPED PORT="+mappedAddress.getPort());
				}
			}
			{
				HtunXxxAddress sourceAddress =(HtunXxxAddress)header.findHtunAttribute(HtunAttribute.SOURCE_ADDRESS);
				if(sourceAddress != null) {
					System.out.println("##SOURCE IP="+encoder.encode(sourceAddress.getIp()));
					System.out.println("##SOURCE PORT="+sourceAddress.getPort());
				}
			}
			{
				HtunXxxAddress changedAddress =(HtunXxxAddress)header.findHtunAttribute(HtunAttribute.CHANGE_ADDRESS);
				if(changedAddress != null) {
					System.out.println("##SOURCE IP="+encoder.encode(changedAddress.getIp()));
					System.out.println("##SOURCE PORT="+changedAddress.getPort());
				}
				System.out.println("####################################");
			}
		}
	}

	public class SendTask extends EventTask {
		byte[] mMessage = null;
		byte[] mIp = null;
		public SendTask(byte[] ip, byte[] meesage) {
			mIp = ip;
			mMessage = meesage;
		}

		@Override
		public void action(EventTaskRunner runner) throws Throwable {
			mDatagramSocket.send(mMessage, mIp);
			runner.pushTask(this, 3000);
		}
	}

}
