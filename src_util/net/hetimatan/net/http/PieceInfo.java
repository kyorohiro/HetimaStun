package net.hetimatan.net.http;

 
public class PieceInfo implements Comparable<PieceInfo>{
	protected long mStart = 0;
	protected long mEnd = 0;
	
	public PieceInfo(long start, long end) {
		mStart = start;
		mEnd = end;
	}

	public long getStart() {
		return mStart;
	}

	public long getEnd() {
		return mEnd;
	}

	@Override
	public int compareTo(PieceInfo o) {
		return (int)(this.mStart -o.mStart);
	}

}