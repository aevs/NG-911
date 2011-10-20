/*
 * Jin Hyung Park (jp2105@columbia.edu)
 *
 * UserAgent Class
 * 
 * Contains information to connect SIP server, to make a call, and to receive a call
 */

package com.columbia.ng911;

import org.zoolu.sdp.SessionDescriptor;
import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.call.Call;
import org.zoolu.sip.call.CallListenerAdapter;
import org.zoolu.sip.call.ExtendedCall;
import org.zoolu.sip.call.SdpTools;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.provider.SipProvider;

import android.util.Log;

public class UserAgent extends CallListenerAdapter {
        protected SipProvider sip_provider;
        protected Call call;
        protected String from_url;
        protected String contact_url;

        /** Local sdp */
        protected String local_session = null;

        public UserAgent (SipProvider sip_provider, String from_url, String contact_url) {
                this.sip_provider = sip_provider;
                this.from_url = from_url;
                this.contact_url = contact_url;
        }

        public boolean call(String target_url) {
                call = new Call(sip_provider, from_url, contact_url, this);


                return true;
        }

        public void onCallIncoming(Call call, NameAddress callee,
                        NameAddress caller, String sdp, Message invite) {
                Log.e("SIP:UA", "Call Incoming!\n");

                SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
                try {
                        sessionProduct(remote_sdp);
                } catch (Exception e) { 
                        // only known exception is no codec
                        Log.e("SIP:UA", e.toString());
                        return;
                }
        }

        private void sessionProduct(SessionDescriptor remote_sdp) {
                SessionDescriptor local_sdp = new SessionDescriptor(local_session);
                SessionDescriptor new_sdp = new SessionDescriptor(local_sdp
                                .getOrigin(), local_sdp.getSessionName(), local_sdp
                                .getConnection(), local_sdp.getTime());
                new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
                new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp
                                .getMediaDescriptors());

                local_session = new_sdp.toString();
                if (call!=null) call.setLocalSessionDescriptor(local_session);

                Log.e("SIP:UA - new SDP", new_sdp.toString());
        }

        public boolean listen() {
                call = new ExtendedCall(sip_provider, from_url,
                                contact_url, "android-test",
                                "realm", "password", this);

                call.listen();

                return true;
        }
}
