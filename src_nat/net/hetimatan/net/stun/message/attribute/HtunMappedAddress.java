package net.hetimatan.net.stun.message.attribute;

import net.hetimatan.net.stun.message.HtunAttribute;

public class HtunMappedAddress extends HtunXxxAddress {

	public HtunMappedAddress(int family, byte[] ip) {
		super(HtunAttribute.MAPPED_ADDRESS, family, ip);
	}

}
