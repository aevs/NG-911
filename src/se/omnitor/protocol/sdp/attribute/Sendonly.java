package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;

public class Sendonly implements SdpAttribute {

    public int getType() {
	return SENDONLY;
    }

}
