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
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

import android.util.Log;

public class SipController implements SipProviderListener, TransactionClientListener {
        private SipProvider sip;
        private UserAgent ua;

        private String serverID;
        private String serverIpAddress;
        private String serverPort;

        private String localIpAddress;
        private int defaultIncomingPort = 5060;
        private boolean isRealTime = true;
        
        SipController(String serverID, String ipAddress, String port) {
                SipStack.log_path = "/data/misc/tmp/";
                SipStack.debug_level = 7;

                this.serverID = serverID;
                this.serverIpAddress = ipAddress;
                this.serverPort = port;

                this.localIpAddress = this.getLocalIpAddress(); // for real device!

                sip = new SipProvider(this.localIpAddress, defaultIncomingPort);
                sip.addSipProviderListener(SipProvider.ANY, this);

                System.out.println("\n\n\nLocal Sip Addr = "+ localIpAddress + ":" + sip.getPort());

                // UserAgent
                ua = new UserAgent(sip, this.localIpAddress, "Android");
                ua.listen();
        }
        
        public void call() {
        		ua.call(this.serverIpAddress);
        }
        
        public void hangup() {
        		ua.hangup();
        }
        
        public void setIsRealTime(boolean isRealTime) {
        	this.isRealTime = isRealTime;
        }

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
        
        public void sendRTT(char in) {
        	if (isRealTime == true)
        		ua.sendRTT(in);
        }

        public void onTransProvisionalResponse(TransactionClient tc, Message resp) {
                // TODO Auto-generated method stub

        }

        public void onTransSuccessResponse(TransactionClient tc, Message resp) {
                // TODO Auto-generated method stub
                System.out.println("Message Successfully delivered!");
                Message req=tc.getRequestMessage();
                NameAddress recipient=req.getToHeader().getNameAddress();
                String subject=null;
                if (req.hasSubjectHeader()) subject=req.getSubjectHeader().getSubject();
                String result = resp.getStatusLine().getReason();
                System.out.println("Details:"+recipient+subject+result);
        }

        public void onTransFailureResponse(TransactionClient tc, Message resp) {
                // TODO Auto-generated method stub

        }

        
        public void onTransTimeout(TransactionClient tc) {
                // TODO Auto-generated method stub

        }

        public void onReceivedMessage(SipProvider sip_provider, Message message) {
                // TODO Auto-generated method stub
                System.out.println("Message Received!");
                System.out.println(message.getBody());
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
