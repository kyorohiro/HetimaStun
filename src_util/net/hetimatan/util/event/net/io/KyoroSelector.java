package net.hetimatan.util.event.net.io;


import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.WeakHashMap;

public class KyoroSelector {
	public static final int ACCEPT = SelectionKey.OP_ACCEPT;
	public static final int READ = SelectionKey.OP_READ;
	public static final int WRITE = SelectionKey.OP_WRITE;
	public static final int CONNECT = SelectionKey.OP_CONNECT;
	public static final int ALL = ACCEPT|READ|WRITE|CONNECT;
	public static final int READ_WRITE = READ|WRITE;
	
	public static final int CANCEL = -1;

	private Selector mRawSelector = null;
	private boolean mIsClosed = false;

	private WeakHashMap<SelectableChannel, KyoroSelectable> mClientList 
	= new WeakHashMap<SelectableChannel, KyoroSelectable>();

	private Iterator<SelectionKey>  mCurrentRawKeyList = null;
	private SelectionKey mCurrentRawKey = null;

	private KyoroSelectable mCurrentSocket = null;
	private int mCurrentKey = -1;

	public void putClient(KyoroSelectable s) {
		SelectableChannel channel = s.getRawChannel();
		if(channel != null) {
			mClientList.put(channel, s);
		} else {
			// debug
		}
	}

	public Selector getSelector() throws IOException {
		if(mRawSelector == null&&!mIsClosed) {
			mRawSelector = Selector.open();
		}
		return mRawSelector;
	}

	public int select(int timeout) throws IOException {
		int ret = selectFromMock_Zero();
		if(ret != 0) {
			return ret;
		}

		Selector selector = getSelector();
		if(selector == null) {
			return 0;
		}
		ret = 0;
	
		if(timeout == 0) {
			ret = selector.selectNow();
		} else {
			ret = selector.select(timeout);
		}
		return ret;
	}

	public synchronized void wakeup() {
		mRawSelector.wakeup();
	}

	public void close() throws IOException {
		mIsClosed = true;
		Selector s =mRawSelector;
		if(s != null) {
			for(SelectionKey key:s.selectedKeys()) {
				if(key != null) {
					key.cancel();
				}
			}
			s.close();
			mRawSelector = null;
		}
	}


	public boolean next() {
		boolean ret = nextFromSelectableChannel();
		if(ret) {
			return ret;
		} else {
			return nextFromMock();
		}
	}


	private boolean nextFromSelectableChannel() {
		mCurrentRawKey = null;
		mCurrentSocket = null;
		mCurrentKey = -1;

		if(mCurrentRawKeyList == null) {
			mCurrentRawKeyList = mRawSelector.selectedKeys().iterator();
		}
		if (!mCurrentRawKeyList.hasNext()) {
			mCurrentRawKeyList = null;
			return false;
		}
		mCurrentRawKey = mCurrentRawKeyList.next();
		mCurrentRawKeyList.remove();
		SelectableChannel channel = mCurrentRawKey.channel();
		mCurrentSocket = mClientList.get(channel);
		mCurrentKey = convertKey(mCurrentRawKey);
		return true;
	}

	public KyoroSelectable getCurrentSocket() {
		return mCurrentSocket;
	}


	public static  int convertKey(SelectionKey rawKey) {
		try {
			if(rawKey.isAcceptable()) {
				return KyoroSelector.ACCEPT;
			}
			else if(rawKey.isConnectable()) {
				return KyoroSelector.CONNECT;
			}
			else if(rawKey.isReadable()) {
				return KyoroSelector.READ;
			}
			else if(rawKey.isWritable()) {
				return KyoroSelector.WRITE;
			}
		} catch(CancelledKeyException e) {}
		return KyoroSelector.CANCEL;
		
	}

	public int getkey() {
		return mCurrentKey;
	}


	//
	// 
	//
	public synchronized void wakeup(KyoroSelectable selectable, int key) {
		mMock.add(new MockSelectorInfo(selectable, key));
		mRawSelector.wakeup();
	}

	private LinkedList<MockSelectorInfo> mMock = new LinkedList<>();
	public static class MockSelectorInfo {
		public KyoroSelectable mSelectable = null;
		public int mKey = 0;
		public MockSelectorInfo(KyoroSelectable selector, int key) {
			mSelectable = selector;
			mKey = key;
		}
	}
	private boolean nextFromMock() {
		if(mMock.size() >0) {
			MockSelectorInfo info = mMock.removeFirst();
			mCurrentSocket = info.mSelectable;
			mCurrentKey = info.mKey;
			return true;
		} else {
			return false;
		}
	}

	public int selectFromMock_Zero() {
		if(mMock.size() >0) {
			MockSelectorInfo info = mMock.getFirst();
			return info.mKey;
		} else {
			return 0;
		}
	}

}
