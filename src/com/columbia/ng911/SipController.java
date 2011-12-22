/*
 * Jin Hyung Park (jp2105@columbia.edu)
 *
 * SipController Class
 *
 * Manage SIP provider information, and send or receive text messages are done here
 *
 */

package com.columbia.ng911;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.message.BaseMessageFactory;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

import android.util.Log;
import android.widget.TextView;

public class SipController {
        private SipProvider sip;
        private UserAgent ua;

        private String serverIpAddress;
        private String serverPort;

        private String localIpAddress;
        private int defaultIncomingPort = 5060;
        private boolean isRealTime = true;
        
        private boolean isRTTconnected = false;
        
        private String phoneNumber;
        char prevChar;
        
        SipController(String serverID, String ipAddress, String port, T140Writer writer, String phoneNumber) {
                SipStack.log_path = "/data/misc/tmp/";
                SipStack.debug_level = 7;

                //this.serverID = serverID;
                this.serverIpAddress = ipAddress;
                this.serverPort = port;

                this.localIpAddress = this.getLocalIpAddress(); // for real device!

                sip = new SipProvider(this.localIpAddress, defaultIncomingPort);
                //sip.addSipProviderListener(SipProvider.ANY, this); // Listener will be mysip class by Pranay

                System.out.println("\n\n\nLocal Sip Addr = "+ localIpAddress + ":" + sip.getPort());

                this.phoneNumber = phoneNumber;
                
                // UserAgent
                String contact_url = "sip:Android("+phoneNumber+")@" + this.localIpAddress + ":" + sip.getPort();
                ua = new UserAgent(sip, this.localIpAddress, contact_url, writer);
                //ua.listen();
                prevChar = 0;
        }
        
        public SipProvider getSharedSipProvider() {
        	return sip;
        }
        
        public void call() {
        	if (isRTTconnected == false) {
	        	ua.hangup();
	        	ua.call(this.serverIpAddress, this.serverPort);
	        	isRTTconnected = true;
        	}
        }
        
        public void hangup() {
        	if (isRTTconnected) {
	        	ua.hangup();
	        	isRTTconnected = false;
        	}
        }
        
        public void setIsRealTime(boolean isRealTime) {
        	this.isRealTime = isRealTime;
        	/*
        	if (!isRTTconnected && isRealTime) {
        		this.call();
        	} else if (!isRealTime) {
        		this.hangup();
        	}
        	*/
        }
        
        public boolean isRealTime() {
        	return this.isRealTime;
        }

        /* send SIP message will be done in mysip.java
        public void send(String text) {
        	if (isRealTime == false) {
                Message msg = MessageFactory.createMessageRequest(sip,
                                new NameAddress(new SipURL("sip:"+ serverIpAddress)),
                                new NameAddress(new SipURL("sip:"+ localIpAddress)),
                                null, "text/plain", text + "\r\n");
                sip.sendMessage(msg);
                System.out.println("Sent");
        	} else {
        		ua.sendRTT('\n');
        	}
        }
        */
        
        public void sendRTT(char in) {
        	if (isRealTime == true && isRTTconnected == true) {
        		ua.sendRTT(in);
        	}
        		
        }

        public String getLocalIpAddress() { 
                try { 
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                                en.hasMoreElements();) { 
                                NetworkInterface intf = en.nextElement(); 
                                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
                                        enumIpAddr.hasMoreElements();) { 
                                        InetAddress inetAddress = enumIpAddr.nextElement(); 
                                        if (!inetAddress.isLoopbackAddress()) { 
                                                return inetAddress.getHostAddress().toString(); 
                                        } 
                                } 
                        } 
                } catch (SocketException ex) { 
                        Log.e("NETWORK", ex.toString()); 
                } 
                return null; 
        } 
}
