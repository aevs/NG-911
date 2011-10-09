package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;

public class Fmtp implements SdpAttribute {

    private int format;
    private String parameters;

    public Fmtp(String value) {
	String[] spl = value.split(" ");
        if (spl.length >= 2) {
            format = Integer.parseInt(spl[0]);
            parameters = spl[1];
        }
        else if (spl.length == 1)
            format = Integer.parseInt(spl[0]);
    }

    public int getType() {
	return FMTP;
    }

    public int getFormat() {
	return format;
    }

    public String getParameters() {
	return parameters;
    }

}
