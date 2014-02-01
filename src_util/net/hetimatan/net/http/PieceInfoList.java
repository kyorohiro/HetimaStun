package net.hetimatan.net.http;

import java.util.Collections;
import java.util.LinkedList;

public class PieceInfoList {
	private LinkedList<PieceInfo> mInfo = new LinkedList<PieceInfo>();

	public int size() {
		return mInfo.size();
	}

	public PieceInfo getPieceInfo(int index) {
		return mInfo.get(index);
	}

	public void append(long start, long end) {
		mInfo.add(new PieceInfo(start, end));
		normalize();
	}

	// #patternA
	// <-----><----->
	//    <------>
	//    ↑start　↑end
	//
	// #patternB
	// <-----><----->
	//   <->
	//
	//
	// #patternC
	//  <-----><----->
	// <-------->
	//
	//
	public void remove(long start, long end) {
		for(int i=0;i<mInfo.size();) {
			long _s = mInfo.get(i).mStart;
			long _e = mInfo.get(i).mEnd;

			if(end<_e&&end<_s){
				break;
			}
			
			//#pattern B
			if(_s<=start&&start<_e&&_s<=end&&end<_e) {
				long prevE = mInfo.get(i).mEnd;
				mInfo.get(i).mEnd = start;
				append(end, prevE);
				break;
			} 
			// #pattern C
			else if(start<=_s&&_s<end&&start<=_e&&_e<end) {
				mInfo.remove(mInfo.get(i));				
			}
			// #pattern A
			else {
				if(_s<=start&&start<_e) {
					mInfo.get(i).mEnd = start;
				} 
				if(_s<=end&&end<_e){
					mInfo.get(i).mStart = end;
				}
				if(mInfo.get(i).mStart>=mInfo.get(i).mEnd) {
					mInfo.remove(mInfo.get(i));
				} else {
					i++;
				}
			}
		}
	}

	public void normalize() {
		Collections.sort(mInfo);
		for(int i=0;i<mInfo.size()-1;) {
			PieceInfo bef = mInfo.get(i);
			PieceInfo aft = mInfo.get(i+1);
			if(bef.mEnd >= aft.mStart) {
				long end = bef.mEnd;
				if(end<aft.mEnd) {
					end = aft.mEnd;
				}
				bef.mEnd = end;
				mInfo.remove(aft);
			} else {
				i++;
			}
		}
	}
}