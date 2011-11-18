package com.columbia.ng911;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.header.Header;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipResponses;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipProviderListener;
import org.zoolu.sip.provider.SipStack;
import org.zoolu.sip.transaction.TransactionClient;
import org.zoolu.sip.transaction.TransactionClientListener;
import org.zoolu.sip.transaction.TransactionServer;

import android.app.Activity;
import android.util.Log;

public class mysip extends Activity implements SipProviderListener, TransactionClientListener{


	String pidflo;
	   /** Remote user. */
	   NameAddress remote_user;
	   SipProvider sip;
	   NG911Activity NG911;
	    String ip;
	    public mysip(String add, NG911Activity ng911)
	    {	NG911 = ng911;
	    	ip = add;
	    	sip = new SipProvider(add,7070);
	    	//sip.setDefaultTransport(SipProvider.PROTO_TCP);
	    	//Log.e("SIP", sip.getDefaultTransport());
	    	SipStack.debug_level = 0;
			System.out.println("SIP PORT:"+ sip.getPort());
			//sip.addSipProviderListener(SipProvider.ANY, this);
			sip.addSipProviderListener(SipProvider.ANY, this);
			pidflo = "Content-Type: application/pidf+xml"
					+"Content-ID: <alice@atlanta.example.com>"
			+"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\""
              	+"xmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\""
              	+"xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\""
              	+"xmlns:cl=\"urn:ietf:params:xml:ns:pidf:geopriv10:civicAddr\""
              	+"xmlns:gml=\"http://www.opengis.net/gml\""
              	+"entity=\"pres:alice@atlanta.example.com\">"
			+"<dm:device id=\"point2d\">"
			+"<gp:geopriv>"
			+"<gp:location-info>"
			+"<gml:Point srsName=\"urn:ogc:def:crs:EPSG::4326\">"
			+"<gml:pos>-34.407 150.883</gml:pos>"
            +"</gml:Point>"
            +"</gp:location-info>"
            +"<gp:usage-rules/>"
            +"<gp:method>802.11</gp:method>"
            +"</gp:geopriv>"
            +"<dm:deviceID>mac:1234567890ab</dm:deviceID>"
        +"<dm:timestamp>2011-11-22T20:57:29Z</dm:timestamp>"
        +"</dm:device>"
        +"</presence>";
	    }


		
		public void send(String text)
		{	/*
			Message msg = MessageFactory.createMessageRequest(sip,
					new NameAddress(new SipURL("pranay: 128.59.22.88:5080")),
					new NameAddress(new SipURL("android@"+"192.168.2.3"+":7070")), 
					text, "multipart/mixed; boundary=boundary1", "--boundary1--"+text+"--boundary1--"+pidflo+"--boundary1--");
			*/
			
			Message msg = MessageFactory.createMessageRequest(sip,
					new NameAddress(new SipURL("anuj1@128.59.22.88:5080")),
					new NameAddress(new SipURL("android@"+"192.168.2.5"+":7070")), 
					text, "text/plain", text);
			
			Header h1 = new Header("Geolocation","<cid:android@192.168.2.3>");
			msg.addHeaderAfter(h1, "Call-ID");
			Header h2 = new Header("Geolocation-Routing", "yes");
			msg.addHeaderAfter(h2, "Geolocation");
			Header h3 = new Header("Accept", "text/plain, application/pidf+xml");
			msg.addHeaderAfter(h3, "Geolocation-Routing");
			
			sip.sendMessage(msg);
			
			
			
			
			System.out.println("Sent");
		}
		
		public void onReceivedMessage(SipProvider sip_provider, Message msg) {
			// TODO Auto-generated method stub
			(new TransactionServer(sip_provider,msg,null)).respondWith(MessageFactory.createResponse(msg,200,SipResponses.reasonOf(200),null));
			System.out.println("Message Received from MYSIP: ");
			System.out.println("Body: "+msg.getBody());
			System.out.println("First Line: "+msg.getFirstLine());
//			NG911.displayIncoming(msg.getBody());
			
		}



		@Override
		public void onTransProvisionalResponse(TransactionClient tc,
				Message resp) {
			// TODO Auto-generated method stub
			
		}



		@Override
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



		@Override
		public void onTransFailureResponse(TransactionClient tc, Message resp) {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void onTransTimeout(TransactionClient tc) {
			// TODO Auto-generated method stub
			
		}
}
