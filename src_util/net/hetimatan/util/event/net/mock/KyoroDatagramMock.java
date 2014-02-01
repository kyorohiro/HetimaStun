package net.hetimatan.util.event.net.mock;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.SelectableChannel;
import java.util.LinkedList;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import net.hetimatan.util.event.net.io.KyoroDatagram;
import net.hetimatan.util.event.net.io.KyoroSelector;
import net.hetimatan.util.http.HttpObject;
import net.hetimatan.util.io.ByteArrayBuilder;

//
// test用
// 画面上に表示する
//
public class KyoroDatagramMock extends KyoroDatagram {
	public static final int NAT_TYPE_OPEN_INTERNET = 0;
	public static final int NAT_TYPE_FULL_CONE = 1;
	public static final int NAT_TYPE_RESTRICTED_ADDRESS = 2;
	public static final int NAT_TYPE_RESTRICTED_PORT = 3;
	public static final int NAT_TYPE_SYMMETRIC = 4;

	private int mNatType = 0;
	// for debug
	private int mDeviceId = 0;
	public KyoroDatagramMock(int type, int id) {
		mNatType = type;
		mDeviceId = id;
	}

	public int getDeviceId() {
		return mDeviceId;
	}

	public int getNatType() {
		return mNatType;
	}

	private Stack<DatagramPacket> mPackets = new Stack<>();
	private WeakReference<KyoroSelector> mCurrentSelector = null;
	public void onReceivePacket(byte[] content, byte[] ip) throws IOException {
		if(!isConnectable(ip)) {
			//throw new IOException("failed connect");
			return;
		}
		mPackets.push(new DatagramPacket(content, ip));
		if(mCurrentSelector == null) {
			return;
		}
		KyoroSelector selector = this.mCurrentSelector.get();
		if(selector == null) {
			return;
		}
		selector.wakeup(this, KyoroSelector.READ);
	}

	public boolean isConnectable(byte[] ip) {
		boolean ret = false;
		//
		// 
		if (mNatType == KyoroDatagramMock.NAT_TYPE_FULL_CONE) {
			return true;
		} else if(mNatType == KyoroDatagramMock.NAT_TYPE_FULL_CONE){
			return true;
		} else if(mNatType == KyoroDatagramMock.NAT_TYPE_SYMMETRIC){
			return true;
		}

		for(MappedAddress address : mAddressList) {
			if (mNatType == KyoroDatagramMock.NAT_TYPE_RESTRICTED_ADDRESS) {
				byte[] sended = address.getSendAd();
				if(sended != null & 
				   sended[4] == ip[4] && sended[5] == ip[5]) {
					return true;
				}
			} else if (mNatType == KyoroDatagramMock.NAT_TYPE_RESTRICTED_PORT) {
				byte[] sended = address.getSendAd();
				if(sended != null & 
				   sended[4] == ip[4] && sended[5] == ip[5]) {
					return true;
				}
			}
		}
		return false;
	}

	private DatagramPacket mCurrentPacket = null;
	private LinkedList<MappedAddress> mAddressList = new LinkedList<>();

	@Override
	public SelectableChannel getRawChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bind(byte[] ip) throws IOException {
		MappedAddress pa = new MappedAddress(ip, null); 
		mAddressList.add(pa);
		if( null != DatagramMockMgr.getInstance().find(pa.getMappedAd()) ) {
			throw new IOException();
		}
		DatagramMockMgr.getInstance().bind(this);
	}

	public int numOfMappedAddress() {
		return mAddressList.size(); 
	}

	public MappedAddress getMappedAddress(int i) {
		return mAddressList.get(i);
	}

	public byte[] getRawIp() {
		return mAddressList.get(0).getRawAd();
	}

	public byte[] getMappedIp() {
		return mAddressList.get(0).getMappedAd();
	}

	@Override
	public void bind(int port) throws IOException {
		byte[] portByte = ByteArrayBuilder.parseInt(port, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
		byte[] base = {0,0,0,0, 0,0};
		base[4] = portByte[0];
		base[4+1] = portByte[1];
		if( null == DatagramMockMgr.getInstance().find(base) ) {
			throw new IOException();
		}
		DatagramMockMgr.getInstance().bind(this);
	}

	@Override
	public byte[] getByte() {
		if(mCurrentPacket == null) {
			return new byte[0];
		}
		return mCurrentPacket.mData;
	}

	@Override
	public byte[] receive() throws IOException {
		if(mPackets.size()>0) {
			DatagramPacket packet = mPackets.pop();
			if(packet != null) {
				mCurrentPacket = packet;
				return mCurrentPacket.mIp;
			}
		}
		mCurrentPacket = null;
		return new byte[0];
	}

	@Override
	public int send(byte[] message, byte[] address) throws IOException {
		KyoroDatagramMock datagram = DatagramMockMgr.getInstance().find(address);
		if(datagram == null) { throw new IOException();}
		datagram.onReceivePacket(message, getMappedIp());
		mAddressList.add(new MappedAddress(getRawIp(), address, getMappedIp()));
		return message.length;
	}

	@Override
	public int send(byte[] message, int start, int length, byte[] address) throws IOException {
		byte[] ms = new byte[length];
		for(int i=0;i>length;i++) {
			ms[i] = message[i+start];
		}
		return send(ms, address);
	}

	@Override
	public void regist(KyoroSelector selector, int key) throws IOException {
		mCurrentSelector = new WeakReference<KyoroSelector>(selector);
		selector.putClient(this);		
	}

	public static class DatagramPacket {
		private byte[] mData = new byte[0];
		private byte[] mIp = {0,0,0,0, 0,0};
		public DatagramPacket(byte[] content, byte[] ip) {
			mData = new byte[content.length];
			for(int i=0;i<mData.length;i++) {
				mData[i] = content[i];
			}
			
			mIp = new byte[ip.length];
			for(int i=0;i<ip.length;i++) {
				mIp[i] = ip[i];
			}
		}
	}

	@Override
	public void close() throws IOException {
		DatagramMockMgr.getInstance().close(this);
		super.close();
	}

	public static class MappedAddress {
		public static final int BASE = 0;
		private static int sNextMapped = BASE;
		private static int sNextPort = 1;
		public static LinkedList<MappedAddress> sList = new LinkedList<>(); 

		public MappedAddress(byte[] rawAd, byte[] sendAd, byte[] mappedAd) {
			System.arraycopy(rawAd, 0, mRawAd, 0, 6);
			if(sendAd != null) {
				System.arraycopy(sendAd, 0, mSendAd, 0, 6);
			}
			System.arraycopy(sendAd, 0, mSendAd, 0, 6);
			System.arraycopy(mappedAd, 0, mMappedAd, 0, 6);
			sList.add(this);
		}

		public MappedAddress(byte[] rawAd, byte[] sendAd) {
			System.arraycopy(rawAd, 0, mRawAd, 0, 6);
			if(sendAd != null) {
				System.arraycopy(sendAd, 0, mSendAd, 0, 6);
			}
			{
				byte[] ad = new byte[6];
				byte[] r = ByteArrayBuilder.parseInt(sNextMapped, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
				System.arraycopy(r, 0, mMappedAd, 0, 4);
				byte[] p = ByteArrayBuilder.parseShort(sNextPort, ByteArrayBuilder.BYTEORDER_BIG_ENDIAN);
				sNextMapped++;
				System.arraycopy(p, 0, mMappedAd, 4, 2);
				sNextPort++;sNextMapped %=6000;
			}
			sList.add(this);
		}

		private byte[] mMappedAd = new byte[6];
		private byte[] mRawAd = new byte[6];
		private byte[] mSendAd = new byte[6];

		public byte[] getMappedAd() {
			return mMappedAd;
		}

		public byte[] getRawAd() {
			return mRawAd;
		}

		public byte[] getSendAd() {
			return mSendAd;
		}
	}

	public static class DatagramMockMgr {
		private static DatagramMockMgr sInst = null;
		public static DatagramMockMgr getInstance() {
			if(sInst == null) {
				sInst = new DatagramMockMgr();
			}
			return sInst;
		}

		private LinkedList<KyoroDatagramMock> mBindedList = new LinkedList<>();
		public DatagramMockMgr() {
		}

		public void bind(KyoroDatagramMock datagram) {
			mBindedList.add(datagram);
		}


		public void close(KyoroDatagramMock datagram) {
			mBindedList.remove(datagram);
		}

		public KyoroDatagramMock find(byte[] ip) {
			for(KyoroDatagramMock item: mBindedList) {
				if(item == null) {continue;}
				byte[] itemIp = item.getMappedIp();
				boolean isEqual = true;
				for(int i=0;i<ip.length;i++) {
					if(itemIp[i] != ip[i]) {
						isEqual = false;
						break;
					}
				}
				if(isEqual) {
					return item;
				}
			}
			return null;
		}
	}
}
