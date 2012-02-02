package com.columbia.ng911;

import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * SipController Class for RTP session
 *
 * Manage SIP provider information, and RTP session handling are done here
 */
public class SipController {
        private SipProvider sip;
        private UserAgent ua;

        private String serverIpAddress;
        private String serverPort;

        private String localIpAddress;
        private int defaultIncomingPort = 5060;
        private boolean isRealTime = true;
        
        private boolean isRTTconnected = false;
        
        NG911Activity NG911;
        
        char prevChar;
        
        /**
         * SipController Initializer Method
         * 
         * @param ng911 Android GUI Main Activity
         * @param serverID SIP Server ID to identify if there are several serves
         * @param ipAddress SIP Server IP Address
         * @param port SIP Server Port Number
         * @param writer T140Writer for handling Real-Time-Text
         * @param phoneNumber User's Phone Number
         * @param toIPAddress Phone's real IP Address - It can be 3G or WiFi
         */
        SipController(NG911Activity ng911, String serverID, String ipAddress, String port, T140Writer writer, String phoneNumber,String toIPAddress) {
                SipStack.log_path = "/data/misc/tmp/";
                SipStack.debug_level = 0;

                NG911 = ng911;
                SharedPreferences prefs = PreferenceManager
        				.getDefaultSharedPreferences(NG911.getApplicationContext());
        		String userName = prefs.getString(NG911Activity.USER_NAME, "undefined");

                this.serverIpAddress = ipAddress;
                this.serverPort = port;

                this.localIpAddress = toIPAddress; // for real device!

                sip = new SipProvider(this.localIpAddress, defaultIncomingPort);

                System.out.println("\n\n\nLocal Sip Addr = "+ localIpAddress + ":" + sip.getPort());

                // UserAgent
                String contact_url = "sip:"+userName+"("+phoneNumber+")@" + this.localIpAddress + ":" + sip.getPort();
                ua = new UserAgent(sip, this.localIpAddress, contact_url, writer);
                prevChar = 0;
        }
        
        /**
         * Get Shared SipProvider
         * @return Shared SipProvider Instance
         */
        public SipProvider getSharedSipProvider() {
        	return sip;
        }
        
        /**
         * Start to send INVITE Message to the server
         */
        public void call() {
        	if (isRTTconnected == false) {
	        	ua.call(this.serverIpAddress, this.serverPort);
	        	isRTTconnected = true;
        	}
        }
        
        /**
         * Send BYE Message to the server
         */
        public void hangup() {
        	if (isRTTconnected) {
	        	ua.hangup();
	        	isRTTconnected = false;
        	}
        }
        
        /**
         * Set current mode
         * @param isRealTime If the current input mode is RTT, isRealTime should be set to 'true'. 
         */
        public void setIsRealTime(boolean isRealTime) {
        	this.isRealTime = isRealTime;
        }
        
        /**
         * Get current mode
         * @return If the current input mode is RTT, return 'true'.
         */
        public boolean isRealTime() {
        	return this.isRealTime;
        }
        
        /**
         * Send RTT one character
         * @param in User input one character
         */
        public void sendRTT(char in) {
        	if (isRealTime == true && isRTTconnected == true) {
        		ua.sendRTT(in);
        	}
        		
        }
}
