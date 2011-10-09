package se.omnitor.protocol.sdp.attribute;

import se.omnitor.protocol.sdp.SdpAttribute;
import javax.sdp.Attribute;
import javax.sdp.SdpParseException;

public class AttributeParser {

    public static SdpAttribute parse(Attribute attribute) 
	throws SdpParseException{

	String name = attribute.getName().toLowerCase();
	String value = attribute.getValue();


	if (name.equals("rtpmap")) {
	    return new Rtpmap(value);
	}

	if (name.equals("fmtp")) {
	    return new Fmtp(value);
	}

	return new Custom(name, value);

    }


}
