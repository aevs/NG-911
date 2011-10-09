package se.omnitor.protocol.sdp;

import se.omnitor.protocol.sdp.format.CustomFormat;
import se.omnitor.protocol.sdp.format.RedFormat;
import se.omnitor.protocol.sdp.format.T140Format;
import se.omnitor.protocol.sdp.attribute.*;

public abstract class Format {

    public static final int SEND = 1;
    public static final int RECEIVE = 2;
    public static final int SEND_RECEIVE = 3;

    protected int clockRate;
    protected int type;
    protected int payloadNbr;
    protected int ptime;

    /**
     * Returns the MIME registered name for this format
     *
     * @return The MIME registered name for this format
     */
    public abstract String getName();

    /**
     * Duplicate the current format.
     *
     * @return A copy of the current format.
     */
    public abstract Format duplicate();

    //public abstract Format getResponse(Format inFormat);

    public abstract String negotiate(Format remoteFormat, 
				     Format[] resultFormat);

    /**
     * Gets the type, either SEND, RECEIVE or SEND_RECEIVE (both send and
     * receive).
     *
     * @return The type.
     */
    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }

    /**
     * The RTP payload type number.
     *
     * @return The RTP payload type number to use.
     */
    public int getPayloadNumber() {
	return payloadNbr;
    }

    public void setInfo(Rtpmap rtpmap) {
	this.clockRate = rtpmap.getClockRate();
    }

    public abstract void setInfo(Fmtp fmtp);

    /**
     * Sets Recvonly info
     *
     * @param recvonly The Recvonly object to fetch info from
     */
    public void setInfo(Recvonly recvonly) {
	this.type = RECEIVE;
    }

    /**
     * Sets Sendonly info
     *
     * @param sendonly The Sendonly object to fetch info from
     */
    public void setInfo(Sendonly sendonly) {
	this.type = SEND;
    }

    /**
     * Sets Sendrecv info
     *
     * @param sendrecv The Sendrecv object to fetch info from
     */
    public void setInfo(Sendrecv sendrecv) {
	this.type = SEND_RECEIVE;
    }

    public abstract String getSdp();


    /**
     * This gives the length of time in milliseconds represented by
     * the media in a packet.  This is probably only meaningful for
     * audio data, but may be used with other media types if it makes
     * sense.  It should not be necessary to know ptime to decode RTP
     * or vat audio, and it is intended as a recommendation for the
     * encoding/packetisation of audio.  It is a media attribute, and
     * is not dependent on charset.
     *
     * @return The length of time for a packet. A value of zero means that
     * ptime is not used.
     */
    public int getPtime() {
	return ptime;
    }

    public void setPtime(int ptime) {
	this.ptime = ptime;
    }

    /**
     * Gets the clock rate
     *
     * @return The clock rate
     */
    public int getClockRate() {
	return clockRate;
    }

    /**
     * Sets the clock rate
     *
     * @param clockRate The new clock rate
     */
    public void setClockRate(int clockRate) {
	this.clockRate = clockRate;
    }

    /**
     * Gives a String representation of this format
     *
     * @return A String representation of this format
     */
    public String toString() {
	return 
	    payloadNbr + ", " + getName() + ", " + getTypeName(type) + ", " + 
	    ptime + ", " + clockRate;
    }

    public static Format getFormat(Rtpmap rtpmap) {
	String fmtStr = rtpmap.getEncodingName().toUpperCase();

	if (fmtStr.equals("T140")) {
	    return new T140Format(rtpmap);
	}
	if (fmtStr.equals("RED")) {
	    return new RedFormat(rtpmap);
	}

	return new CustomFormat(rtpmap);
    }

    public static Format getFormat(int payloadNbr) {
	switch (payloadNbr) {
	case 0:
	    return new CustomFormat(0, "PCMU", 8000);
	case 3:
	    return new CustomFormat(3, "GSM", 8000);
	case 4:
	    return new CustomFormat(4, "G723", 8000);
	case 5:
	    return new CustomFormat(5, "DVI4", 8000);
	case 6:
	    return new CustomFormat(6, "DVI4", 16000);
	case 7:
	    return new CustomFormat(7, "LPC", 8000);
	case 8:
	    return new CustomFormat(8, "PCMA", 8000);
	case 9:
	    return new CustomFormat(9, "G722", 8000);
	case 10:
	    return new CustomFormat(10, "L16", 44100);
	case 11:
	    return new CustomFormat(11, "L16", 44100);
	case 12:
	    return new CustomFormat(12, "QCELP", 8000);
	case 13:
	    return new CustomFormat(13, "CN", 8000);
	case 14:
	    return new CustomFormat(14, "MPA", 90000);
	case 15:
	    return new CustomFormat(15, "G728", 8000);
	case 16:
	    return new CustomFormat(16, "DVI4", 11025);
	case 17:
	    return new CustomFormat(17, "DVI4", 22050);
	case 18:
	    return new CustomFormat(18, "G729", 8000);
	case 25:
	    return new CustomFormat(25, "CelB", 90000);
	case 26:
	    return new CustomFormat(26, "JPEG", 90000);
	case 28:
	    return new CustomFormat(28, "nv", 90000);
	case 31:
	    return new CustomFormat(31, "H261", 90000);
	case 32:
	    return new CustomFormat(32, "MPV", 90000);
	case 33:
	    return new CustomFormat(33, "MP2T", 90000);
	case 34:
	    return new CustomFormat(34, "H263", 90000);
	default:
	    return new CustomFormat(payloadNbr, "");
	}
    }

    public static String getTypeName(int type) {
	switch (type) {
	case SEND:
	    return "SEND";
	case RECEIVE:
	    return "RECEIVE";
	case SEND_RECEIVE:
	    return "SEND_RECEIVE";
	default:
	    return "unknown: " + type;
	}
    }

    public abstract void copyInfoFrom(Format f);

	    



}
