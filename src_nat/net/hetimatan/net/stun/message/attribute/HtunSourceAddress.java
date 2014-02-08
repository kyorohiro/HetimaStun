package net.hetimatan.net.stun.message.attribute;

import net.hetimatan.net.stun.message.HtunAttribute;

public class HtunSourceAddress extends HtunXxxAddress {

	public HtunSourceAddress(int family, byte[] ip) {
		super(HtunAttribute.SOURCE_ADDRESS, family, ip);
	}

}
