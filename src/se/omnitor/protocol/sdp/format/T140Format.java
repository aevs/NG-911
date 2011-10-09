package se.omnitor.protocol.sdp.format;

import se.omnitor.protocol.sdp.Format;
import se.omnitor.protocol.sdp.SdpMedia;
import se.omnitor.protocol.sdp.attribute.Rtpmap;
import se.omnitor.protocol.sdp.attribute.Fmtp;


public class T140Format extends Format {

    private static final String NAME = "t140";

    private RedFormat redFormat;

    public T140Format(int rtpPayloadNumber) {
	payloadNbr = rtpPayloadNumber;

	clockRate = 1000;
	type = SEND_RECEIVE;
	ptime = 0;
	redFormat = null;
    }

    public T140Format(int rtpPayloadNumber, int clockRate) {
	payloadNbr = rtpPayloadNumber;
	this.clockRate = clockRate;

	type = SEND_RECEIVE;
	ptime = 0;
	redFormat = null;
    }

    public T140Format(Rtpmap rtpMap) {
	payloadNbr = rtpMap.getPayloadType();
	clockRate = rtpMap.getClockRate();

	type = SEND_RECEIVE;
	ptime = 0;
	redFormat = null;
    }

    public void copyInfoFrom(Format f) {
	type = f.getType();
	ptime = f.getPtime();
    }

    public Format duplicate() {
	T140Format tf = new T140Format(payloadNbr, clockRate);
	tf.setType(type);
	tf.setPtime(ptime);
	tf.setRedFormat(redFormat);

	return tf;
    }

    public String getName() {
	return NAME;
    }

    /*
    public Format getResponse(SdpMedia inMedia) {
	return null;
    }
    */

    public void setInfo(Fmtp fmtp) {
    }

    public void setInfo(Rtpmap rtpmap) {
	this.payloadNbr = rtpmap.getPayloadType();
	this.clockRate = rtpmap.getClockRate();
    }

    public String getSdp() {
	return "a=rtpmap:" + payloadNbr + " " + NAME + "/" + clockRate +
	    "\r\n";
    }

    /**
     * Performs negotiation. Checks if the remote format's information is
     * OK and returns a response SDP. This function also returns a resulting
     * format, which should be handled locally when starting media.
     *
     * @param remoteFormat The remote's format
     * @param resultFormat This MUST be an initialized array with at least
     * one element. This function returns a resulting format in the first
     * element of this vector.
     *
     * @return Response SDP. Also see the "resultFormat" parameter.
     */
    public String negotiate(Format remoteFormat, Format[] resultFormat) {
	String sdp = "a=rtpmap:" + remoteFormat.getPayloadNumber() +
	    " " + NAME + "/" + clockRate;

	// Create a new format and link
	resultFormat[0] = new T140Format(remoteFormat.getPayloadNumber(),
					 clockRate);
	RedFormat rf = ((T140Format)remoteFormat).getRedFormat();
	if (rf != null && this.getRedFormat() != null) {
	    ((T140Format)resultFormat[0]).setRedFormat(rf);
	    rf.setFormat((T140Format)resultFormat[0]);
	}

	return sdp + "\r\n";
    }


    public void setRedFormat(RedFormat redFormat) {
	this.redFormat = redFormat;
    }

    public RedFormat getRedFormat() {
	return redFormat;
    }

    public boolean useRedundancy() {
	return redFormat != null;
    }

    public int getRedundancyPayloadType() {
	if (redFormat != null) {
	    return redFormat.getPayloadNumber();
	}

	return 0;
    }

    public int getRedundantGenerations() {
	if (redFormat != null) {
	    return redFormat.getGenerations();
	}

	return 0;
    }

    public String toString() {
	String str = super.toString();
	str +=
	    ", " +
	    (useRedundancy()? "use red": "no red") + ", " +
	    "redPt=" + getRedundancyPayloadType() + ", " +
	    "regGen=" + getRedundantGenerations();

	return str;
    }

}
