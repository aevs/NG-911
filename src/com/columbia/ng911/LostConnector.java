package com.columbia.ng911;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/** 
 *
 * LostConnector
 *
 * Query LoST Server to get the nearest PSAP server information
 *
 */
public class LostConnector {
	private static LostConnector lostConnector = new LostConnector(0, 0);
	private boolean requestSent;
	private double geo_lat;
	private double geo_lng;
	private String addressLostServer = "http://ng911-lost1.cs.columbia.edu:8080/lost/LoSTServlet";
	public static String NO_RESPONSE="No Response from LoST server";

	/**
	 * LostConnector Class
	 * 
	 * This class get the PSAP Server IP from LoST server based on current locations from GPS.
	 * Also, LostConnector is the singleton class.
	 * 
	 * @param geo_lat    GPS latitude value
	 * @param geo_lng    GPS Longitude value
	 */
	private LostConnector(double geo_lat, double geo_lng) {
		this.requestSent = false;
		this.setLocation(geo_lat, geo_lng);
	}

	/**
	 * Get LostConnector shared instance
	 * 
	 * @return LostConnector shared instance
	 */
	public static LostConnector getInstance() {
		return lostConnector;
	}
	

	/**
	 * Getter Method to get requestSent boolean value.
	 * 
	 * @return If the PSAP query is sent, requestSent will be 'true'. Otherwise, 'false'.
	 */
	public boolean requestSent() {
		return requestSent;
	}

	/**
	 * Set the current location at LostConnector class.
	 * 
	 * @param geo_lat     GPS Latitude value
	 * @param geo_lng     GPS Longitude value
	 */
	public void setLocation(double geo_lat, double geo_lng) {
		this.geo_lat = geo_lat;
		this.geo_lng = geo_lng;
	}

	/**
	 * Make Query String.
	 * 
	 * @return XML Query String with current location information
	 */
	private String makeLoSTRequest() {
		String relax_ng_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<findService"
				+ " xmlns=\"urn:ietf:params:xml:ns:lost1\""
				+ " xmlns:p2=\"http://www.opengis.net/gml\""
				+ " serviceBoundary=\"reference\""
				+ " recursive=\"true\">"
				+ " <location id=\"6020688f1ce1896d\" profile=\"geodetic-2d\">"
				+ "  <p2:Point id=\"point1\" srsName=\"urn:ogc:def:crs:EPSG::4326\">"
				+ "   <p2:pos>" + geo_lat + " " + geo_lng + "</p2:pos>"
				+ "  </p2:Point>" + " </location>"
				+ " <service>urn:service:sos</service>" + "</findService>";
		return relax_ng_xml;
	}

	/**
	 * Function to get the PSAP server IP
	 * 
	 * @return PSAP server IP address
	 */
	public String getPSAPD() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(addressLostServer);
		StringEntity se;
		HttpResponse response;

		try {
			/*
			 * Send XML Query to LoST Server 
			 */
			se = new StringEntity(this.makeLoSTRequest(), HTTP.UTF_8);
			post.setEntity(se);

			response = client.execute(post);
			if(response==null)
				return NO_RESPONSE;
			
			InputStream is = response.getEntity().getContent();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			String read = br.readLine();
			while (read != null) {
				Log.e("Line ",""+read);
				sb.append(read);
				read = br.readLine();
			}
			
			/*
			 * Parsing the recevied file, and get the PSAP server IP
			 */
			String trial = sb.toString();
			int urlStartIndex = trial.indexOf("<uri>");
			int urlEndIndex = trial.indexOf("</uri>");
			String serverIp = trial.substring(urlStartIndex + 5, urlEndIndex);
			Log.e("LostConnector", "*********** " + serverIp + "***********");
			
			return serverIp;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
