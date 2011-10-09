package se.omnitor.protocol.sdp;

import java.util.Vector;
import javax.sdp.MediaDescription;

public interface SdpMedia {

    public abstract String getType();

    /**
     * Gets all Format objects associated with this media. In all cases,
     * the formats are listed in order of preference, with the first format
     * listed being preferred.  In this case, preferred means that the
     * recipient of the offer SHOULD use the format with the highest
     * preference that is acceptable to it.
     *
     * @return All Format objects associated with this media, collected in a
     * Vector.
     */
    public Vector getFormats();

    public void setFormats(Vector formatVector);

    /**
     * Gets the remote host's RTP port to send media to.
     *
     * @return The remote host's RTP port to send media to.
     */
    public int getPort();

    /**
     * Gets the remote host's RTCP port to send data to.
     *
     * @return The remote host's RTP port to send data to.
     */
    public int getRtcpPort();

    public int getLocalPort();

    public String getSdp();
    public String getDecliningSdp();

    public String negotiate(SdpMedia remoteMedia, SdpMedia[] resultMedia);

    public String getProtocol();

    public MediaDescription getResultMediaDescription(MediaDescription md);

    public String getRemoteIp();

    /**
     * Gets the actual port on the computer. If STUN mapping is used, this
     * port really tells which physical port to receive data on.
     * @param port int
     */
    public void setPhysicalPort(int port);
    public int getPhysicalPort();

}
