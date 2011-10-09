/* 
 * T140handler for NG9-1-1 project
 * 
 * � 2007 copyright by the Trustees of Columbia University in the City of New York. 
 * 
 * Modified by Wonsang Song (wonsang@cs.columbia.edu)
 * Modified Date: 12/16/2007
 * 
 * "Parts of this program were based on reference designs developed by Omnitor AB 
 * and the Trace Center, University of Wisconsin-Madison under funding from the 
 * National Institute on Disability and Rehabilitation Research US Dept of 
 * Education and the European Commission."
 * 
 * This code was modified from the original distributed by Trace/Omnitor 
 * in order to integrated with SIPc.
 */

/*
 * Open Source Exemplar Software
 *
 * Copyright (C) 2005 University of Wisconsin (Trace R&D Center)
 * Copyright (C) 2005 Omnitor AB
 *
 * This reference design was developed under funding from the National
 * Institute on Disability and Rehabilitation Research US Dept of Education
 * and the European Commission.
 *
 * This piece of software is a part of a package that was developed as a joint
 * effort of Omnitor AB and the Trace Center - University of Wisconsin and is
 * released to the public domain with only the following restrictions:
 *
 * 1) That the following acknowledgement be included in the source code and
 * documentation for the program or package that use this code
 *
 * "Parts of this program were based on reference designs developed by
 * Omnitor AB and the Trace Center, University of Wisconsin-Madison under
 * funding from the National Institute on Disability and Rehabilitation
 * Research US Dept of Education and the European Commission."
 *
 * 2) That this program not be modified unless it is plainly marked as
 * modified from the original distributed by Trace/Omnitor.
 *
 * (NOTE: This release applies only to the files that contain this notice -
 * not necesarily to any other code or libraries associated with this file.
 * Please check individual files and libraries for the rights to use each)
 *
 * THIS PIECE OF THE SOFTWARE PACKAGE IS EXPERIMENTAL/DEMONSTRATION IN NATURE.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE
 * BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES,
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 *
 */

package com.columbia.ng911;

import java.net.InetAddress;
import java.util.Vector;


import se.omnitor.protocol.rtp.RtpTextTransmitter;
import se.omnitor.protocol.rtp.RtpTextReceiver;
import se.omnitor.protocol.rtp.Session;
import se.omnitor.protocol.rtp.text.SyncBuffer;
import se.omnitor.protocol.sdp.SdpMedia;
import se.omnitor.protocol.sdp.Format;
import se.omnitor.protocol.sdp.format.T140Format;
import se.omnitor.util.FifoBuffer;

/**
 * The start-up class for T-Client.
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public class MediaManager {

    private Vector media;

    private Session rtpSession;
    private boolean rtpSessionCreated;

    private RtpTextTransmitter rtpTextTransmitter;
    private RtpTextReceiver rtpTextReceiver;



    private SyncBuffer txTextBuffer;
    private FifoBuffer rxTextBuffer;


    private int localTextPort = 0;


    private Vector mediaStarters;

    private boolean allStopped;

    private boolean isEconf351 = false;

    public MediaManager(Vector negotiatedMedia,
                        SyncBuffer txTextBuffer,
                        FifoBuffer rxTextBuffer) {

        media = negotiatedMedia;
        this.txTextBuffer = txTextBuffer;
        this.rxTextBuffer = rxTextBuffer;

        
        int mlen = media.size();
        for (int mcnt = 0; mcnt < mlen; mcnt++) {
            SdpMedia sdpMedia = (SdpMedia) media.elementAt(mcnt);

            String type = sdpMedia.getType().toLowerCase();

            if (type.equals("text")) {
                localTextPort = sdpMedia.getPhysicalPort();
            }
        }
        mediaStarters = new Vector(0, 1);

        allStopped = false;


        InetAddress localInetAddr = null;
        try {
            localInetAddr = java.net.InetAddress.getLocalHost();
        } catch (Exception e) {
            System.out.println(
                    "MediaManager: Could not determine local address: " + e);
        }
        System.out.println("Local addr: " + localInetAddr + ":" + localTextPort);
        

        rtpSession = null;
        rtpSessionCreated = false;
    }

    public void startAll() {

        int mlen = media.size();
        SdpMedia sdpMedia;
        Vector formats;
        se.omnitor.protocol.sdp.Format format;
        String type;
        MediaStarter ms;

        for (int mcnt = 0; mcnt < mlen; mcnt++) {
            sdpMedia = (SdpMedia) media.elementAt(mcnt);

            formats = sdpMedia.getFormats();

            if (formats.size() > 0) {
            	
                format = (se.omnitor.protocol.sdp.Format) formats.elementAt(0);

                type = sdpMedia.getType().toLowerCase();

                if (type.equals("text")) {
                	System.out.println("Remote addr: " + sdpMedia.getRemoteIp() + ":" + sdpMedia.getPort());
                    if (format.getName().equalsIgnoreCase("T140")) {
                        int redPl = ((T140Format)format).getRedundancyPayloadType();
                        if (redPl > 0) {
                            for (int i=0; i<formats.size(); i++)
                                if (formats.get(i) instanceof se.omnitor.protocol.sdp.format.RedFormat) {
                                    format = (Format)formats.get(i);
                                }
                        }
                    }

                    ms = new MediaStarter(MediaStarter.TEXT_IN,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);

                    ms = new MediaStarter(MediaStarter.TEXT_OUT,
                                          sdpMedia, format);
                    ms.start();
                    mediaStarters.add(ms);
                }
            }
        }

    }

    /**
     * Stops all media.
     *
     */
    public void stopAll() {

        allStopped = true;

        int len = mediaStarters.size();
        for (int cnt = 0; cnt < len; cnt++) {
            ((MediaStarter) mediaStarters.elementAt(cnt)).stop();
        }

        if (rtpSession != null) {
            rtpSession.stop();
            rtpSession = null;
        }

        if (rtpTextTransmitter != null) {
            rtpTextTransmitter.stop();
            rtpTextTransmitter = null;
        }

        if (rtpTextReceiver != null) {
            rtpTextReceiver.stop();
            rtpTextReceiver = null;
        }

    }


    /**
     * Starts text transmission.
     *
     */
    private void startTextOut(SdpMedia media, Format format) throws Exception {
        createRTPSession(media.getRemoteIp());

        rtpSession.openRTPTransmitSocket(localTextPort, media.getPort());

        rtpSession.createAndStartRTCPSenderThread(localTextPort + 1,
                                                  media.getPort() + 1);
        txTextBuffer.empty();

        boolean useT140Red = false;
        boolean useRed = false;
        int redT140Gen = 0;
        int t140Pt = 0;
        int redPt = 0;
        int redGen = 0;

        if (format instanceof T140Format) {
            useT140Red = ((T140Format) format).useRedundancy();
            //redPt = ((T140Format) format).getRedundancyPayloadType();
            redT140Gen = ((T140Format) format).getRedundantGenerations();
            t140Pt = ((T140Format) format).getPayloadNumber();
        }
        else if (format instanceof se.omnitor.protocol.sdp.format.RedFormat) {
            useRed = true;
            redGen = ((se.omnitor.protocol.sdp.format.RedFormat) format).
                     getGenerations();
            redPt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getPayloadNumber();
            t140Pt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getFormatPayloadNumber();
        }

        if (allStopped) {
            return;
        }

        rtpTextTransmitter =
                new RtpTextTransmitter(rtpSession,
                                       false,
                                       media.getRemoteIp(),
                                       localTextPort, //media.getPort() + 2,
                                       media.getPort(),
                                       t140Pt,
                                       useRed, // Red flag
                                       redPt, // Red pt
                                       redGen, // Red gens
                                       useT140Red, // T.140 red flag
                                       redT140Gen, // T.140 red gens
                                       txTextBuffer, isEconf351);

        if (allStopped) {
            return;
        }

        // The text receiver can inform about whether the remote
        // receiver is running. Therefore, we wait for the receiver.
        int waitingTime = 10;
        while (rtpTextReceiver == null && waitingTime > 0) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                return;
            }

            waitingTime--;
        }

        if (allStopped) {
            return;
        }

        // Now, wait for the receiver to receive a report from the
        // remote receiver before we start out transmitter.
        if (rtpTextReceiver != null) {
            rtpTextReceiver.waitForRemoteReceiver(5);
        }

        if (allStopped) {
            return;
        }

        //if (sc.getState() == SipController.IS_ESTABLISHED) {
        if (rtpTextTransmitter != null) {
            rtpTextTransmitter.start();
        }

        //}

    }

    /**
     * To preevent both the sending and receiving thread from creating
     * separate RTP session this synchronized method is used.
     *
     * @param ip The ip address to use.
     */
    private synchronized void createRTPSession(String ip) {
        if (!rtpSessionCreated) {
            rtpSession = new Session(ip, 64000, localTextPort);
            rtpSessionCreated = true;
        }
    }

    private void startTextIn(SdpMedia media, Format format) throws Exception {

        boolean useRed = false;
        int t140Pt = 0;
        int redPt = 0;

        createRTPSession(media.getRemoteIp());

        rtpSession.openRTPReceiveSocket(localTextPort);
        rtpSession.startRTPThread();
        rtpSession.createAndStartRTCPReceiverThread(localTextPort + 1);

        if (allStopped) {
            return;
        }

        if (format instanceof T140Format) {
            t140Pt = ((T140Format) format).getPayloadNumber();
            /*System.err.println("format instanceof T140Format");
            System.err.println("useRed " + useRed);
            System.err.println("redPt " + redPt);
            System.err.println("getPayloadNumber() -> " + format.getPayloadNumber());*/
        } else if (format instanceof se.omnitor.protocol.sdp.format.RedFormat) {
            useRed = true;
            redPt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getPayloadNumber();
            t140Pt = ((se.omnitor.protocol.sdp.format.RedFormat) format).getFormatPayloadNumber();
            /*System.err.println("format instanceof RedFormat");
            System.err.println("useRed " + useRed);
            System.err.println("redPt " + redPt);
            System.err.println("getPayloadNumber() -> " + format.getPayloadNumber()); */

        }

        rtpTextReceiver =
                new RtpTextReceiver(rtpSession,
                                    media.getRemoteIp(),
                                    localTextPort,
                                    useRed, // Red flag
                                    t140Pt,//format.getPayloadNumber(),
                                    redPt, // Red payload type
                                    rxTextBuffer);

        if (allStopped) {
            return;
        }

        /*
        rtpTextReceiver.setCName(appSettings.getUserRealName());
        rtpTextReceiver.setEmail("sip:" + appSettings.getPrimarySipAddress());
        */
        rtpTextReceiver.start();

        // Now, wait for the receiver to receive a report from the
        // remote receiver before we start out transmitter.
        /*
          if (rtpTextReceiver != null) {
            rtpTextReceiver.waitForLocalReceiver(20);
          }
         */
/*
        if (!allStopped) {
            ac.getProgramWindow().changeStatusLabel(ProgramWindow.
                    LABEL_TEXT_IN,
                    "T.140");
            ac.getT140Panel().getRemoteTextArea().setActiveLook(true);
        }
*/
    }

    class MediaStarter implements Runnable {

        public static final int TEXT_IN = 1;
        public static final int TEXT_OUT = 2;
        public static final int AUDIO_IN = 3;
        public static final int AUDIO_OUT = 4;
        public static final int VIDEO_IN = 5;
        public static final int VIDEO_OUT = 6;

        private Thread t;
        private int type;
        private SdpMedia media;
        private Format format;
        private boolean running;

        public MediaStarter(int type, SdpMedia media, Format format) {
            this.type = type;
            this.media = media;
            this.format = format;

            t = new Thread(this, "Media starter: " + getType(type));
            running = false;
        }

        public void start() {
            if (!running) {
                running = true;
                t.start();
            }
        }

        public void stop() {
            if (running) {
                t.interrupt();
            }
        }

        public void run() {
        	try {
	            switch (type) {
	            case TEXT_IN:
	                startTextIn(media, format);
	                break;
	            case TEXT_OUT:
	                startTextOut(media, format);
	                break;
	
	            default:
	            
	            }
	
	            running = false;
        	} catch (Exception e) {
        		System.exit(1);
        	}

        }

        public String getType(int type) {
            switch (type) {
            case TEXT_IN:
                return "TEXT_IN";
            case TEXT_OUT:
                return "TEXT_OUT";
            case AUDIO_IN:
                return "AUDIO_IN";
            case AUDIO_OUT:
                return "AUDIO_OUT";
            case VIDEO_IN:
                return "VIDEO IN";
            case VIDEO_OUT:
                return "VIDEO OUT";
            default:
                return "(Unknown type: " + type + ")";
            }
        }

    }

}

