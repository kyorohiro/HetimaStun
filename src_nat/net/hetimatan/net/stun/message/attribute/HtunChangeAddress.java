package net.hetimatan.net.stun.message.attribute;

import net.hetimatan.net.stun.message.HtunAttribute;

public class HtunChangeAddress extends HtunXxxAddress {

	public HtunChangeAddress(int family, byte[] ip) {
		super(HtunAttribute.CHANGE_ADDRESS, family, ip);
	}

}
