package net.hetimatan.io.filen;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import net.hetimatan.io.file.KyoroFile;
import net.hetimatan.util.event.GlobalAccessProperty;


public class CashKyoroFileHelper {

	public final static String GATAG_DEFAULTCASH = "my.tmp";
	private static int sID = 0;

	public static File newCashFile() {
		boolean deleted = true;
		File base = null;
		do {
			sID++;
			GlobalAccessProperty prop = GlobalAccessProperty.getInstance();
			String pathAsString = prop.get(GATAG_DEFAULTCASH, ".");
			File dir = new File(pathAsString);
			base = new File(dir, "virtualFile_"+sID);
			if(base.exists()) {
				deleted = base.delete();
			} else {
				deleted = true;
			}
		}while(!deleted);
		return base;
	}

	public static byte[] newBinary(KyoroFile src) throws IOException {
		return newBinary(src, 0);
	}

	public static byte[] newBinary(KyoroFile src, long start) throws IOException {
		int len = (int)(src.length()-start);
		if(len<=0) {
			return new byte[0];
		}
		byte[] buffer = new byte[len];
		long fp = src.getFilePointer();
		try {
			src.seek(start);
			src.read(buffer);
		} finally {
			src.seek(fp);
		}
		return buffer;
	}

	public static void xcopy(File[] srcs, CashKyoroFile dest) throws IOException {
		for(int i=0;i<srcs.length;i++) {
			CashKyoroFile src = new CashKyoroFile(srcs[i], 16*1024, 10);
			long fp = dest.getFilePointer();
			try {
				CashKyoroFileHelper.copy(src, dest);
				dest.syncWrite();
			} finally {
				dest.seek(fp+src.length());
				src.close();
			}
		}
	}

//	public static void copy(RACashFile src, RACashFile  dest) throws IOException {
	public static void copy(KyoroFile src, CashKyoroFile  dest) throws IOException {
		byte[] buffer = new byte[512];
		int len=0;
		long srcFP = src.getFilePointer();
		long destFP = dest.getFilePointer();

		try {
			do {
				len = buffer.length;
				len = src.read(buffer);
				if(len>0) {
//					dest.write(buffer, 0, 1);//len);
					dest.write(buffer, 0, len);
				}
			} while(len>0);
		} finally {
			src.seek(srcFP);
			dest.seek(destFP);
		}
	}

	public static KyoroFile subSequence(CashKyoroFile file, long start, long end) throws IOException {
		System.out.println("start="+start+",end="+end);
		return new ReferenceModifierKyoroFile(file, start, end, 256, 3);
	}

	public static void write(OutputStream output, KyoroFile source) throws IOException {
		byte[] buffer = new byte[1024];
		int wr = 0;

		source.seek(0);
		do {
			wr = source.read(buffer);
			if(wr<0){break;}
			output.write(buffer, 0, wr);
		} while(true);
	}
}
