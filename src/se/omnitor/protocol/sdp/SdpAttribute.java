package se.omnitor.protocol.sdp;


public interface SdpAttribute {

    public static final int CAT = 1;
    public static final int KEYWDS = 2;
    public static final int TOOL = 3;
    public static final int PTIME = 4;
    public static final int MAXPTIME = 5;
    public static final int RTPMAP = 6;
    public static final int RECVONLY = 7;
    public static final int SENDRECV = 8;
    public static final int SENDONLY = 9;
    public static final int INACTIVE = 10;
    public static final int ORIENT = 11;
    public static final int TYPE = 12;
    public static final int CHARSET = 13;
    public static final int SDPLANG = 14;
    public static final int LANG = 15;
    public static final int FRAMERATE = 16;
    public static final int QUALITY = 17;
    public static final int FMTP = 18;

    public static final int CUSTOM = 0;

    public int getType();

}
