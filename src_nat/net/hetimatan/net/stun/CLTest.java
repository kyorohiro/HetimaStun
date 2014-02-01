package net.hetimatan.net.stun;

import java.io.IOException;
import java.net.InetAddress;

import net.hetimatan.util.event.net.KyoroSocketEventRunner;
import net.hetimatan.util.http.HttpObject;

public class CLTest {
	public static HtunServer todo = null;
	public static HtunClient todoc = null;
	public static void main(String[] args) {
		try {
			todoc = new HtunClient(
					HttpObject.address(InetAddress.getLocalHost().getHostAddress(), 8082),
					HttpObject.address("59.157.6.137", 18080));
			KyoroSocketEventRunner runnerC = todoc.startTask(null);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
