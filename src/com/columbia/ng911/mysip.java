package com.columbia.ng911;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

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
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class mysip implements SipProviderListener {

	String pidflo;
	String lon;
	String lat;
	public List<MessageTime> messagelist = new ArrayList<MessageTime>();

	/** Remote user. */
	NameAddress remote_user;
	SipProvider sip;
	NG911Activity NG911;
	Monitor mon;
	String ip;
	SipProvider siptcp;
	Handler sipHandler;
	Handler messageNotSentHandler;

	// public mysip(String add, NG911Activity ng911)
	public mysip(SipProvider sip_provider, NG911Activity ng911, String ip,
			Handler sipHandler,Handler messageNotSentHandler) {
		
		NG911 = ng911;
		this.sipHandler = sipHandler;
		this.ip = ip;
		this.messageNotSentHandler=messageNotSentHandler;
		
		mon = new Monitor(this);
		Timer timer1 = new Timer();
		timer1.schedule(mon, 0, 400);
		// sip = new SipProvider(add,7070);
		sip = sip_provider;
		// sip.setDefaultTransport(SipProvider.PROTO_TCP);
		// Log.e("SIP", sip.getDefaultTransport());
		SipStack.debug_level = 0;
		//Log.e("IP:", ip);
		// sip.addSipProviderListener(SipProvider.ANY, this);
		sip.addSipProviderListener(SipProvider.ANY, this);
		
	}

	// To send images
	public void sendImage(String image) {
		sip.setDefaultTransport(SipProvider.PROTO_TCP);

		Message msg = MessageFactory.createMessageRequest(sip, new NameAddress(
				new SipURL("test@128.59.22.88:5080")), new NameAddress(
				new SipURL("android@" + ip + ":" + sip.getPort())),
				"Camera Image", "image/jpeg", image);

		sip.sendMessage(msg);
		Log.e("Content-Length: ", ""
				+ msg.getContentLengthHeader().getContentLength());

	}

	// To send text data
	public void send(String text) {
		Message msg;
		String header = "MIME-Version: 1.0\nContent-ID: <android@192.168.2.6>\nContent-Type: text/plain\nContent-Transfer-Encoding: 8bit\n\n";
		sip.setDefaultTransport(SipProvider.PROTO_UDP);
		if (Geolocation.getIsUpdated()) {
			msg = MessageFactory.createMessageRequest(sip, new NameAddress(
					new SipURL("test@128.59.22.88:5080")), new NameAddress(
					new SipURL("android@" + ip + ":" + sip.getPort())), text,
					"multipart/mixed;boundary=\"--=boundary1=\"",
					"----=boundary1=\n" + header + text + "\n----=boundary1=\n"
							+ Geolocation.getGeolocation()
							+ "----=boundary1=--\n");

			Header h1 = new Header("Geolocation", "<cid:android@192.168.2.6>");
			msg.addHeaderAfter(h1, "Call-ID");
			Header h2 = new Header("Geolocation-Routing", "yes");
			msg.addHeaderAfter(h2, "Geolocation");
			Header h3 = new Header("Accept", "text/plain, application/pidf+xml");
			msg.addHeaderAfter(h3, "Geolocation-Routing");
//			Geolocation.setIsUpdated(false);
			Log.e("sending: ", "geolocation");
		} else {
			msg = MessageFactory.createMessageRequest(sip, new NameAddress(
					new SipURL("test@128.59.22.88:5080")), new NameAddress(
					new SipURL("android@" + ip + ":" + sip.getPort())), text,
					"text/plain", text);

			Log.e("sending: ", "text");
		}

		msg.removeFromHeader();
		String tag = SipProvider.pickTag();
		Log.e("TAG", tag);

//		To be used for sending the Correct header to PSAP

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(NG911.getApplicationContext());
		String phoneNumber = prefs.getString(NG911Activity.USER_PHONE,
				"undefined");
		String userName = prefs.getString(NG911Activity.USER_NAME, "undefined");
		Header h0 = new Header("From", userName+" <tel:"+phoneNumber+">;tag=" + tag);
		msg.addHeaderAfter(h0, "To");

		//Comment when server accepts userName
//		Header h0 = new Header("From", "<sip:android@" + ip + ":"
//				+ sip.getPort() + ">;tag=" + tag);
//		msg.addHeaderAfter(h0, "To");


//		Header h1 = new Header("Contact", "<sip:" + "phoneNumber" + "@" + ip
//				+ ":" + sip.getPort() + ">;tag=" + tag);
//		msg.addHeaderAfter(h1, "From");

		sip.sendMessage(msg);

		// msg.toString()

		MessageTime mt = new MessageTime(tag, text);
		messagelist.add(mt);

		System.out.println("Sent");
	}

	public void notifyTimeout(MessageTime mt) {
		// NG911.notifyTimeout(mt);
		android.os.Message msgos = new android.os.Message();
		msgos.obj = mt.message;
		messageNotSentHandler.sendMessage(msgos);
	}

	public void onReceivedMessage(SipProvider sip_provider, Message msg) {
		// TODO Auto-generated method stub

		try {
			Log.e("onReceived message: ",""+msg.getBody());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (msg.isMessage()) // This is a new MESSAGE from PSAP
		{
			(new TransactionServer(sip_provider, msg, null))
					.respondWith(MessageFactory.createResponse(msg, 200,
							SipResponses.reasonOf(200), null));
			Log.e("Incoming", msg.getBody());
//			NG911.displayIncoming(msg.getBody());
			android.os.Message msgos = new android.os.Message();
			msgos.obj = msg.getBody();
			Log.e("Incoming msgOS", "msgData "+msgos.getData().toString());
			sipHandler.sendMessage(msgos);

			
		} else if (msg.isResponse()) // This is ACK message.
		{
			Log.e("SIP", "ACK Received from MYSIP: ");
			// Log.e("SIP","Body: "+msg.getBody());
			Log.e("SIP", "First Line: " + msg.getFirstLine());
			Log.e("SIP", msg.getFromHeader().getName());
			String tagr = msg.getFromHeader().getTag();
			if (messagelist.remove(new MessageTime(tagr, "")))
				Log.e("SIP", "Removed tag: " + tagr);
		} else {
			Log.e("SIP", "First Line second: " + msg.getFirstLine());

		}
		// NG911.displayIncoming(msg.getBody());

	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

}
