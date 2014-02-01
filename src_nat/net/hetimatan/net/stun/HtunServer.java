package net.hetimatan.net.stun;

import java.io.IOException;

import net.hetimatan.io.file.MarkableFileReader;
import net.hetimatan.io.filen.CashKyoroFile;
import net.hetimatan.io.filen.CashKyoroFileHelper;
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

public class HtunServer {

	private KyoroSocketEventRunner mRunner = null;
	private KyoroDatagramImpl mMainIp_MainPort= null;
	private KyoroDatagramImpl mMainIp_SubPort = null;
	private KyoroDatagramImpl mSubIp_MainPort = null;
	private KyoroDatagramImpl mSubIp_SubPort = null;
	private byte[] mMainIp = null;
	private byte[] mSubIp = null;
	private int mMainPort = 0;
	private int mSubPort = 0;

	public HtunServer(byte[] mainIp, byte[] subIp, int mainPort, int subPort) throws IOException {
	    mMainIp_MainPort = new KyoroDatagramImpl();
	    mMainIp_SubPort = new KyoroDatagramImpl();
		mSubIp_MainPort = new KyoroDatagramImpl();
		mSubIp_SubPort = new KyoroDatagramImpl();
	    mMainIp = mainIp;
	    mSubIp = subIp;
	    mMainPort = mainPort;
	    mSubPort = subPort;
	}

	public void init() throws IOException {
		{
			byte[] address = HttpObject.address(HttpObject.ntoa(mMainIp), mMainPort);
			mMainIp_MainPort.bind(address);
			mMainIp_MainPort.setMemo(address);
		}
		{
			byte[] address = HttpObject.address(HttpObject.ntoa(mMainIp), mSubPort);
			mMainIp_SubPort.bind(address);
			mMainIp_SubPort.setMemo(address);
		}
		{
			byte[] address = HttpObject.address(HttpObject.ntoa(mSubIp), mMainPort);
			mSubIp_MainPort.bind(address);
			mSubIp_MainPort.setMemo(address);			
		}
		{
			byte[] address = HttpObject.address(HttpObject.ntoa(mSubIp), mSubPort);
			mSubIp_SubPort.bind(address);
			mSubIp_SubPort.setMemo(address);
		}
	}

	public void close()  {
		if(mMainIp_MainPort != null) {
			try {
				mMainIp_MainPort.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(mMainIp_SubPort != null) {
			try {
				mMainIp_SubPort.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(mSubIp_MainPort != null) {
			try {
				mSubIp_MainPort.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(mSubIp_SubPort != null) {
			try {
				mSubIp_SubPort.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public KyoroSocketEventRunner startTask(KyoroSocketEventRunner runner) throws IOException {
		init();

		if(runner == null) {
			mRunner = new KyoroSocketEventRunner();
		}
		mRunner.waitIsSelect(true);
		mMainIp_MainPort.regist(mRunner.getSelector(), KyoroSelector.READ);
		mMainIp_MainPort.setEventTaskAtWrakReference(new ReceiveTask(), KyoroSelector.READ);
		mRunner.start(null);

		return mRunner;
	}

	public HtunHeader parseMessage(byte[] buffer) throws IOException {
		MarkableFileReader reader = new MarkableFileReader(buffer);
		HtunHeader header = HtunHeader.decode(reader);
		return header;
	}

	public HtunChangeRequest parseMessage(HtunHeader header) throws IOException {
		HtunAttribute changeRequest = header.findHtunAttribute(HtunAttribute.CHANGE_RESUQEST);
		if (changeRequest != null) {
			return (HtunChangeRequest)changeRequest;
		} else {
			return null;
		}
	}

	public KyoroDatagramImpl selectSocket(HtunChangeRequest changeRequest) {
		boolean changePort = changeRequest.chagePort();
		boolean changeIp = changeRequest.changeIp();
		KyoroDatagramImpl tmp = null;
		if(!changeIp&&!changePort) {
			tmp = mMainIp_MainPort;
		} else if(!changeIp&&changePort) {
			tmp = mMainIp_SubPort;
		} else if(changeIp&&!changePort) {
			tmp = mSubIp_MainPort;			
		} else if(changeIp&&changePort) {
			tmp = mSubIp_SubPort;
		}
		return tmp;
	}

	public byte[] createResponse(HtunHeader header, byte[] mappedIp, KyoroDatagramImpl responseSocket) throws IOException {
		HtunHeader response = new HtunHeader(HtunHeader.BINDING_RESPONSE, header.getId());
		response.addAttribute(new HtunXxxAddress(HtunAttribute.MAPPED_ADDRESS, 0x01, mappedIp));
		response.addAttribute(new HtunXxxAddress(HtunAttribute.SOURCE_ADDRESS, 0x01, (byte[])responseSocket.getMemo()));
		response.addAttribute(new HtunXxxAddress(HtunAttribute.CHANGE_ADDRESS, 0x01, (byte[])mSubIp_SubPort.getMemo()));

		CashKyoroFile output = new CashKyoroFile(1024);
		response.encode(output.getLastOutput());
		return CashKyoroFileHelper.newBinary(output);
	}

	public class ReceiveTask extends EventTask {
		@Override
		public void action(EventTaskRunner runner) throws Throwable {
			byte[] mappedIp = mMainIp_MainPort.receive();
			byte[] buffer = mMainIp_MainPort.getByte();
			System.out.println("##="+new String(buffer));

			// parse message
			HtunHeader header = parseMessage(buffer);
			HtunChangeRequest changeRequest = parseMessage(header);
			if(changeRequest == null) {
				// error response
				return;
			}

			// select response socket
 			KyoroDatagramImpl responseSocket = selectSocket(changeRequest);

			// create response
			byte[] responseSource = createResponse(header, mappedIp, responseSocket);
			
			// send
			runner.pushTask(new SendTask(responseSocket, mappedIp, responseSource));
		}

	}

	public class SendTask extends EventTask {
		private KyoroDatagramImpl mDatagramSocket = null;
		byte[] mMessage = null;
		byte[] mIp = null;
		public SendTask(KyoroDatagramImpl socket, byte[] ip, byte[] meesage) {
			mDatagramSocket = socket;
			mIp = ip;
			mMessage = meesage;
		}

		@Override
		public void action(EventTaskRunner runner) throws Throwable {
			mDatagramSocket.send(mMessage, mIp);
		}
	}
}
