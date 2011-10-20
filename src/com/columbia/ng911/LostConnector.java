/*
 * Jin Hyung Park (jp2105@columbia.edu)
 *
 * LostConnector
 *
 * Query LoST Server to get the nearest PSAP server infomation
 *
 */

package com.columbia.ng911;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class LostConnector {
	private static LostConnector lostConnector = new LostConnector(0, 0);
	private Context appContext;
	private boolean requestSent;
	private double geo_lat;
	private double geo_lng;
	private String addressLostServer = "http://ng911-lost1.cs.columbia.edu:8080/lost/LoSTServlet";
	
	public void setContext(Context appContext) {
		this.appContext = appContext;
	}
	
	public boolean requestSent() {
		return requestSent;
	}
	
	public void setLocation(double geo_lat, double geo_lng) {
		this.geo_lat = geo_lat;
		this.geo_lng = geo_lng;
	}
	
	private LostConnector(double geo_lat, double geo_lng) {
		this.requestSent = false;
		this.setLocation(geo_lat, geo_lng);
	}
	
	public static LostConnector getInstance() {
		return lostConnector;
	}
	
	private String makeLoSTRequest() {
		String relax_ng_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<findService" +
				" xmlns=\"urn:ietf:params:xml:ns:lost1\"" +
				" xmlns:p2=\"http://www.opengis.net/gml\"" +
				" serviceBoundary=\"reference\"" +
				" recursive=\"true\">" +
				" <location id=\"6020688f1ce1896d\" profile=\"geodetic-2d\">" +
				"  <p2:Point id=\"point1\" srsName=\"urn:ogc:def:crs:EPSG::4326\">" +
				"   <p2:pos>" + geo_lat + " " + geo_lng + "</p2:pos>" +
				"  </p2:Point>" +
				" </location>" +
				" <service>urn:service:sos</service>" +
				"</findService>";
		
		Log.e("[Lost]", "Query ::\n" + relax_ng_xml);
		
		return relax_ng_xml;
	}
	
	public void getPSAPD() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(addressLostServer);
		StringEntity se;
		HttpResponse response;
		
		try {
			se = new StringEntity(this.makeLoSTRequest(), HTTP.UTF_8);
			post.setEntity(se);
			
			response = client.execute(post);
			
			//Toast.makeText(appContext, EntityUtils.toString(response.getEntity()), 
			//		Toast.LENGTH_LONG).show();
			//this.requestSent = true;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
