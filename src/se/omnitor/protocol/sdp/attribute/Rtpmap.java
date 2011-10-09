package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;

public class Rtpmap implements SdpAttribute {

    private int payloadType;
    private String encodingName;
    private int clockRate;
    private String encodingParameters;

    public Rtpmap(String value) {
	String[] spl;

	spl = value.split(" ");
	payloadType = Integer.parseInt(spl[0]);

	spl = spl[1].split("/");
	encodingName = spl[0];
	clockRate = Integer.parseInt(spl[1]);
	if (spl.length > 2) {
	    encodingParameters = spl[2];
	}
	else {
	    encodingParameters = null;
	}

    }

    public int getType() {
	return RTPMAP;
    }

    public int getPayloadType() {
	return payloadType;
    }

    public String getEncodingName() {
	return encodingName;
    }

    public int getClockRate() {
	return clockRate;
    }

    public String getEncodingParameters() {
	return encodingParameters;
    }

}
