package net.hetimatan.util.http;

import net.hetimatan.net.http.PieceInfoList;

public class HttpObjectHelper {
	private static long[] sdef = {0, Long.MAX_VALUE};

	public static PieceInfoList getRangeList(String range, long max) {
		long[] v= getRange(range, max);
		PieceInfoList list = new PieceInfoList();
		for(int i=0;i+1<v.length;i+=2) {
			list.append(v[i], v[i+1]);
		}
		return list;
	}

	public static long[] getRange(String range) {
		return getRange(range, Long.MAX_VALUE);
	}

	public static long[] getRange(String range,long max) {
		int index = range.indexOf("=");
		if(index+1<range.length()) {
			range = range.substring(index+1);
		}
		range = range.replaceAll("\\r\\n\\s\\t", "");
		max = max-1;
		if(range == null|| range.length() == 0){return sdef;}
		String[] r = range.split(",");
		if(range == null|| range.length() == 0){return sdef;}
		if(r.length == 0) {return sdef;}
		long[] ret = new long[r.length*2];
		String vs[];
		try {
			for(int i=0;i<ret.length;i+=2) {
				vs = r[i/2].split("-");
				if(vs.length ==0) {
					return new long[] {0, max};
				}
				ret[i] = Long.parseLong(vs[0]);
				if(vs.length>1) {
					ret[i+1] = Long.parseLong(vs[1]);
				} else {
					ret[i+1] = max;//Long.MAX_VALUE;
				}
			}
		} catch(NumberFormatException e) {
			return new long[] {0, max};
		}
		return ret;
	}

	
}
