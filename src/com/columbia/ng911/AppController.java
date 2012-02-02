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

import java.io.*;
import java.util.Vector;

import se.omnitor.protocol.rtp.text.SyncBuffer;
import se.omnitor.protocol.t140.T140Panel;
import se.omnitor.protocol.t140.T140Packetizer;
import se.omnitor.protocol.t140.T140DePacketizer;
import se.omnitor.util.FifoBuffer;

import se.omnitor.protocol.sdp.Format;
import se.omnitor.protocol.sdp.SdpMedia;
import se.omnitor.protocol.sdp.media.CustomMedia;
import se.omnitor.protocol.sdp.format.T140Format;
import se.omnitor.protocol.sdp.format.RedFormat;

public class AppController {
    private Vector<SdpMedia> negotiatedMedia;
    private SyncBuffer txTextBuffer;
    private FifoBuffer rxTextBuffer;
    private T140Panel t140Panel;
    private T140Packetizer t140Packetizer;
    private T140DePacketizer t140DePacketizer;
    private MediaManager mediaManager;

    /**
     * AppController - Real Time Text Controller Class
     * 
     * @param call_id    The unique ID to identify each RTT session
     * @param out        Output stream to display receving RTT characters
     * @param log_out    Output stream for logging
     */
    public AppController(String call_id, Writer out, Writer log_out) {
        txTextBuffer = new SyncBuffer(3, 300);
        t140Packetizer = new T140Packetizer(0);
        
        // set sending handler
        t140Panel = new T140Panel(t140Packetizer, call_id, out, log_out);
        t140Packetizer.setOutBuffer(txTextBuffer);
        
        rxTextBuffer = new FifoBuffer();
        t140DePacketizer = new T140DePacketizer(0);
        t140DePacketizer.setInBuffer(rxTextBuffer);
        
        // set receiving handler
        t140DePacketizer.setEventHandler(t140Panel);        
        t140DePacketizer.start();
    }    

    /**
     * Starting RTT sessions with given parameters
     * 
     * @param localIP        The phone's real IP
     * @param localPort      The port to be opened for RTT connection
     * @param remoteIP       The SIP server's real IP
     * @param remotePort     The SIP server's port
     * @param red_pt         RED value for RTT
     * @param t140_pt        T140 value for RTT
     * @param generation     RED Generation value
     */
    public void start(String localIP, int localPort, String remoteIP, int remotePort,
    		int red_pt, int t140_pt, int generation) {
    	try {    		
    		negotiatedMedia = new Vector<SdpMedia>(0, 1);
    		SdpMedia sdpMedia = new CustomMedia("text", remotePort, "RTP/AVP");
    		((CustomMedia)sdpMedia).setRemoteIp(remoteIP);
    		Vector<Format> formats;
    	    formats = new Vector<Format>(0, 1);
    	    RedFormat rformat = new RedFormat(red_pt);
    	    rformat.setGenerations(generation);
    	    T140Format tformat = new T140Format(t140_pt);
    	    rformat.setFormat(tformat);
    	    tformat.setRedFormat(rformat);
    	    formats.add(tformat);
    	    formats.add(rformat);
    	    sdpMedia.setFormats(formats);    		
    	    sdpMedia.setPhysicalPort(localPort);
    		negotiatedMedia.add(sdpMedia);	
	    	
	    	startMedia();
    	} catch (Exception e) {
    		System.out.println(e.getMessage());
    	}	
    }
    /**
     * Ends the program by shutting down the SIP client and then exiting.
     *
     */
    public void stop() {
        System.out.println("AppController close");
        if (mediaManager != null) {
            mediaManager.stopAll();
            mediaManager = null;
        }
        negotiatedMedia = null;        
    }

    /**
     * Starts all media.
     *
     */
    private void startMedia() {
        System.out.println("<<startMedia::>>");
        if (mediaManager != null) {
            mediaManager.stopAll();
        }

        mediaManager = new MediaManager(negotiatedMedia,
                                 txTextBuffer,
                                 rxTextBuffer);

        mediaManager.startAll();
    }

    /**
     * The function processInput actually receives user inputs from the phone.
     * 
     * @param in one character which is input by User on the phone
     */
    public void processInput(char in) {
    	t140Panel.keyTyped(in);
    }
}
