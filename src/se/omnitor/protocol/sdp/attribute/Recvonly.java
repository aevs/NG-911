package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;

public class Recvonly implements SdpAttribute {

    public int getType() {
	return RECVONLY;
    }

}
