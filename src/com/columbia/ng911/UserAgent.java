/*
 * Jin Hyung Park (jp2105@columbia.edu)
 *
 * UserAgent Class
 * 
 * Contains information to connect SIP server, to make a call, and to receive a call
 */

package com.columbia.ng911;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.zoolu.sdp.MediaDescriptor;
import org.zoolu.sdp.MediaField;
import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sdp.AttributeField;

import android.util.Log;

public class UserAgent extends CallListenerAdapter {
        protected SipProvider sip_provider;
        protected Call call;
        protected String from_url;
        protected String contact_url;
        
        private AppController appController;
        
        protected int t140_remote_port;

        /** Local sdp */
        protected String local_session = null;
        
        private void initSessionDescriptor() { 
            SessionDescriptor sdp = new SessionDescriptor(
                            this.contact_url,
                            //this.sip_provider.getViaAddress());
                            this.contact_url);
  
            local_session = sdp.toString();
            
            Log.e("SIP:LOCAL_SDP", local_session);
        } 

        public UserAgent (SipProvider sip_provider, String from_url, String contact_url) {
                this.sip_provider = sip_provider;
                this.from_url = from_url;
                this.contact_url = contact_url;
                this.initSessionDescriptor();
        }

        public boolean call(String target_url) {
                call = new Call(sip_provider, from_url, contact_url, this);


                return true;
        }
        
        public void hangup() {
        		call.hangup();
        		call.cancel();
        		call.bye();
        		call.listen();
        		
        		appController.stop();
        }
        
        public void sendRTT(char in) {
        	appController.processInput(in);
        }

        private void sessionProduct(SessionDescriptor remote_sdp) {
                SessionDescriptor local_sdp = new SessionDescriptor(local_session);
                SessionDescriptor new_sdp = new SessionDescriptor(local_sdp
                                .getOrigin(), local_sdp.getSessionName(), local_sdp
                                .getConnection(), local_sdp.getTime());
                
                new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
                new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp
                                .getMediaDescriptors());
                
                MediaDescriptor remote_m = remote_sdp.getMediaDescriptor("text");
                MediaField remote_m_field = remote_m.getMedia();
                
                t140_remote_port = remote_m_field.getPort();
                
                AttributeField a1 = new AttributeField("rtpmap", "99 red/1000");
                AttributeField a2 = new AttributeField("fmtp", "99 98/98/98");
                AttributeField a3 = new AttributeField("rtpmap", "98 t140/1000");
                Vector<AttributeField> v = new Vector<AttributeField>();
                v.add(a1); v.add(a2); v.add(a3);
                
                new_sdp.addMedia(new MediaField("text", 7077, 0, 
                		remote_m_field.getTransport(), remote_m_field.getFormats()), v);
                
                local_session = new_sdp.toString();
                if (call!=null) call.setLocalSessionDescriptor(local_session);

                Log.e("SIP:UA - new SDP", new_sdp.toString());
                Log.e("SIP:UA - remote text port = ", Integer.toString(t140_remote_port));
                
                //To Testing RTP Sesstion for RTT (Real Time Text)
                appController = new AppController("temp_id", 
                		new BufferedWriter(new OutputStreamWriter(System.out)), 
                		new BufferedWriter(new OutputStreamWriter(System.out)));
            	appController.start("127.0.0.1", 7077, this.from_url, t140_remote_port, 1, 1, 1);
        }

        public boolean listen() {
                call = new ExtendedCall(sip_provider, from_url,
                                contact_url, "android-test",
                                "realm", "password", this);

                call.listen();

                return true;
        }
        
        public void onCallIncoming(Call call, NameAddress callee,
                NameAddress caller, String sdp, Message invite) {
        	Log.e("SIP:UA", "Call Incoming! -> sdp ?" + sdp + "\n" + "-> invite ?"+invite+"\n");

        	SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
	        try {
	                sessionProduct(remote_sdp);
	        } catch (Exception e) { 
	                // only known exception is no codec
	                Log.e("SIP:UA", e.toString());
	                return;
	        }
        
	        call.ring();
	        call.accept(this.local_session);
        }
        
        public void onCallRinging(Call call, Message resp) {
        	Log.e("SIP:UA - onCallRinging - ", resp.toString());
        }
        
        public void onCallAccepted(Call call, String sdp, Message resp) {
        	Log.e("SIP:UA - onCallAccepted - ", resp.toString());
        }
}
