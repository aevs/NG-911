package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;

public class Custom implements SdpAttribute {

    private String name;
    private String value;

    public Custom(String name, String value) {
	this.name = name;
	this.value = value;
    }

    public int getType() {
	return CUSTOM;
    }

}
