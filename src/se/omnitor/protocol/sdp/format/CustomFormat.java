package se.omnitor.protocol.sdp.format;

import se.omnitor.protocol.sdp.Format;
import se.omnitor.protocol.sdp.attribute.Rtpmap;
import se.omnitor.protocol.sdp.attribute.Fmtp;

/**
 * This class defines a custom format, which could be used if there is no
 * own class for the format to be used.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class CustomFormat extends Format {

    private String name;

    /**
     * Initializes.
     *
     * @param rtpPayloadNumber The RTP payload number to assign
     * @param name The MIME extension name to assign (eg. for "text/t140" this
     * name should be "t140")
     */
    public CustomFormat(int rtpPayloadNumber, String name) {
	this.payloadNbr = rtpPayloadNumber;
	this.name = name;

	clockRate = 0;
	type = SEND_RECEIVE;
	ptime = 0;
    }

    /**
     * Initializes.
     *
     * @param rtpPayloadNumber The RTP payload number to assign.
     * @param name The MIME extension name to assign (eg. for "text/t140" this
     * name should be "t140")
     * @param clockRate The clock rate to assign
     */
    public CustomFormat(int rtpPayloadNumber, String name, int clockRate) {
	this.payloadNbr = rtpPayloadNumber;
	this.name = name;
	this.clockRate = clockRate;

	type = SEND_RECEIVE;
	ptime = 0;
    }

    /**
     * Initializes
     *
     * @param rtpmap An Rtpmap object to fetch all information from
     */
    public CustomFormat(Rtpmap rtpmap) {
	payloadNbr = rtpmap.getPayloadType();
	name = rtpmap.getEncodingName();
	clockRate = rtpmap.getClockRate();

	type = SEND_RECEIVE;
	ptime = 0;
    }

    public void copyInfoFrom(Format f) {
	type = f.getType();
	ptime = f.getPtime();
    }

    public Format duplicate() {
	CustomFormat cf = new CustomFormat(payloadNbr, name, clockRate);
	cf.setType(type);
	cf.setPtime(ptime);

	return cf;
    }

    /**
     * Sets Rtpmap info
     *
     * @param rtpmap The Rtpmap object to fetch info from
     */
    public void setInfo(Rtpmap rtpmap) {
	this.name = rtpmap.getEncodingName();
	this.clockRate = rtpmap.getClockRate();
    }

    /**
     * Sets Fmtp info
     *
     * @param fmtp The Fmtp object to fetch info from
     */
    public void setInfo(Fmtp fmtp) {
    }

    /**
     * Gets the MIME extension name for this format, eg. for "text/t140" the
     * name will be "t140".
     *
     * @return The MIME extension name
     */
    public String getName() {
	return name;
    }

    /**
     * Does nothing at the moment.
     *
     * @return null
     */
    /*
    public Format getResponse(Format inFormat) {
	return null;
    }
    */


    /**
     * Gets SDP for this format
     *
     * @return SDP
     */
    public String getSdp() {
	String sdp = "a=rtpmap:" + payloadNbr + " " + name + "/" + clockRate;
	return sdp + "\r\n";
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
	String sdp = "a=rtpmap:" + remoteFormat.getPayloadNumber() + " " +
	    name + "/" + clockRate;

	resultFormat[0] = new CustomFormat(remoteFormat.getPayloadNumber(),
					   name,
					   clockRate);

	return sdp + "\r\n";
    }



}


