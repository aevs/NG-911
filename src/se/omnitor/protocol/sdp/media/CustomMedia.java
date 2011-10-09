package se.omnitor.protocol.sdp.media;

import java.util.Vector;
import se.omnitor.protocol.sdp.SdpMedia;
import javax.sdp.MediaDescription;
import javax.sdp.Media;
import javax.sdp.SdpParseException;
import se.omnitor.protocol.sdp.Format;


public class CustomMedia implements SdpMedia {

    private String mediaType;
    private int port;
    private int portCount;
    private Vector formats;
    private String protocol;
    private int localPort;
    private String remoteIp;
    private int physicalPort = -1;


    public CustomMedia(String mediaType) {
	this.mediaType = mediaType;
	port = 0;
	portCount = 0;
	protocol = "";
	remoteIp = "";

	formats = new Vector(0, 1);
    }

    public CustomMedia(String mediaType, int port, String protocol) {
	this.mediaType = mediaType;
	this.port = port;
	portCount = 0;
	this.protocol = protocol;
	remoteIp = "";

	formats = new Vector(0, 1);
    }

    public CustomMedia(Media media) throws SdpParseException {
	mediaType = media.getMediaType();
	port = media.getMediaPort();
	portCount = media.getPortCount();
	protocol = media.getProtocol();
	remoteIp = "";

	formats = new Vector(0, 1);
    }

    public String getType() {
	return mediaType;
    }

    public int getPort() {
	return port;
    }

    public void setPort(int port) {
	this.port = port;
    }

    public Vector getFormats() {
	return formats;
    }

    public void setFormats(Vector formats) {
	this.formats = formats;
    }

    public int getRtcpPort() {
	return port+1;
    }

    public MediaDescription getResultMediaDescription(MediaDescription md) {
	return null;
    }

    public String toString() {
	return mediaType + ", " + port + ", " + portCount + ", " + protocol;
    }


    public String negotiate(SdpMedia remoteMedia, SdpMedia[] resultMedia) {

	if (!remoteMedia.getProtocol().toUpperCase().equals
	    (protocol.toUpperCase())) {

	    return remoteMedia.getDecliningSdp();
	}

	String sdp = "";
	String payloadNumbers = "";

	Vector remoteFormats = remoteMedia.getFormats();
	int flen = remoteFormats.size();
	Format remoteFormat;
	Format localFormat;
	Format resultFormat[];
	Vector resultFormats = new Vector(0, 1);

	for (int cnt=0; cnt<flen; cnt++) {
	    remoteFormat = (Format)remoteFormats.elementAt(cnt);
	    localFormat = getFormat(remoteFormat);

	    if (localFormat != null) {
		payloadNumbers += " " + remoteFormat.getPayloadNumber();

		resultFormat = new Format[1];
		sdp += localFormat.negotiate(remoteFormat, resultFormat);
		if (resultFormat[0] != null) {
		    resultFormats.add(resultFormat[0]);
		}
	    }


	}

	if (payloadNumbers.equals("")) {
	    return remoteMedia.getDecliningSdp();
	}

	String resultSdp = "m=" + mediaType + " " + port;

	if (portCount > 0) {
	    resultSdp += "/" + portCount;
	}

	resultSdp += " " + protocol + payloadNumbers + "\r\n" + sdp;

	resultMedia[0] =
	    new CustomMedia(mediaType, remoteMedia.getPort(), protocol);
	((CustomMedia)resultMedia[0]).setLocalPort(getPort());
        if (physicalPort != -1) {
            resultMedia[0].setPhysicalPort(physicalPort);
        }
	resultMedia[0].setFormats(resultFormats);

	return resultSdp;
    }

    private Format getFormat(Format remoteFormat) {
	int payloadNumber = remoteFormat.getPayloadNumber();

	if (payloadNumber < 96) {

	    int fLen = formats.size();
	    for (int cnt=0; cnt<fLen; cnt++) {
		if (payloadNumber ==
		    ((Format)formats.elementAt(cnt)).getPayloadNumber()) {

		    return (Format)formats.elementAt(cnt);
		}
	    }

	}
	else {

	    String name = remoteFormat.getName();
	    Format tmpFormat;

	    int fLen = formats.size();
	    for (int cnt=0; cnt<fLen; cnt++) {
		tmpFormat = (Format)formats.elementAt(cnt);

		if (name.toUpperCase().equals(tmpFormat.getName().
					      toUpperCase()) &&
		    tmpFormat.getPayloadNumber() >= 96) {

		    return tmpFormat;
		}
	    }

	}

	return null;

    }


    public String getSdp() {
	String sdp = "";

	sdp = "m=" + mediaType + " " + port;

	if (portCount > 0) {
	    sdp += "/" + portCount;
	}

	sdp += " " + protocol;

	int flen = formats.size();
	for (int cnt=0; cnt<flen; cnt++) {
	    sdp += " " + ((Format)formats.elementAt(cnt)).getPayloadNumber();
	}

	sdp += "\r\n";

	for (int cnt=0; cnt<flen; cnt++) {
	    sdp += ((Format)formats.elementAt(cnt)).getSdp();
	}

	return sdp;

    }

    public String getDecliningSdp() {
	String sdp = "";

	sdp = "m=" + mediaType + " 0 " + protocol;

	int flen = formats.size();
	for (int cnt=0; cnt<flen; cnt++) {
	    sdp += " " + ((Format)formats.elementAt(cnt)).getPayloadNumber();
	}

	sdp += "\r\n";

	return sdp;
    }

    public String getProtocol() {
	return protocol;
    }

    public void setLocalPort(int localPort) {
	this.localPort = localPort;
    }

    public int getLocalPort() {
	return localPort;
    }

    public String getRemoteIp() {
	return remoteIp;
    }

    public void setRemoteIp(String ip) {
	remoteIp = ip;
    }

    public void setPhysicalPort(int port) {
        physicalPort = port;
    }

    public int getPhysicalPort() {
        if (physicalPort == -1) {
            return localPort;
        }

        return physicalPort;
    }

}


