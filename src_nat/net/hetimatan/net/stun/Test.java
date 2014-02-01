package net.hetimatan.net.stun;

import java.io.IOException;

import net.hetimatan.util.event.net.KyoroSocketEventRunner;
import net.hetimatan.util.http.HttpObject;

public class Test {
	public static HtunServer todo = null;
	public static HtunClient todoc = null;
	public static void main(String[] args) {
		try {
			todo = new HtunServer(
					HttpObject.aton("59.157.6.137"),
					HttpObject.aton("59.157.6.131"), 
					18080, 18081);
			KyoroSocketEventRunner runner = todo.startTask(null);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
