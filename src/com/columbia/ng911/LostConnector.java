/*
 * Jin Hyung Park (jp2105@columbia.edu)
 *
 * LostConnector
 *
 * Query LoST Server to get the nearest PSAP server infomation
 *
 */

package com.columbia.ng911;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

public class LostConnector {
	private static LostConnector lostConnector = new LostConnector(0, 0);
	private Context appContext;
	private boolean requestSent;
	private double geo_lat;
	private double geo_lng;
	private String addressLostServer = "http://ng911-lost1.cs.columbia.edu:8080/lost/LoSTServlet";
	public static String NO_RESPONSE="No Response from LoST server";

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

//		Log.e("[Lost]", "Query ::\n" + relax_ng_xml);

		return relax_ng_xml;
	}

	public String getPSAPD() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(addressLostServer);
		StringEntity se;
		HttpResponse response;

		try {
			se = new StringEntity(this.makeLoSTRequest(), HTTP.UTF_8);
			post.setEntity(se);

			response = client.execute(post);
			if(response==null){
				return NO_RESPONSE;
			}
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
			String trial = sb.toString();
			int urlStartIndex = trial.indexOf("<uri>");
			int urlEndIndex = trial.indexOf("</uri>");
			String serverIp = trial.substring(urlStartIndex + 5, urlEndIndex);
			Log.e("LostConnector", "*********** " + serverIp + "***********");
			return serverIp;
			
			
			/*****
			 * 
			 * Using Dom
			 * 
			 *****/
//				DocumentBuilderFactory factory = DocumentBuilderFactory
//						.newInstance();
//				factory.setNamespaceAware(true);
//				DocumentBuilder builder = factory.newDocumentBuilder();
//				Document doc = builder.parse(is);
//				doc.getDocumentElement().normalize();
//
//				NodeList nodeList=doc.getElementsByTagName("uri");
//				Node node=nodeList.item(0);
//				Log.e("Node Name:",""+node.getNodeName());
//				Log.e("Node Value:",""+node.getNodeValue());
//				
//				NodeList nodeList1=doc.getElementsByTagName("serviceBoundary");
//				Node node1=nodeList1.item(0);
//				Log.e("Node Name:",""+node1.getNodeName());
//				Log.e("Node Value:",""+node1.getNodeValue());
//
//				NodeList nodeList2=doc.getElementsByTagName("locationValidation");
//				Node node2=nodeList2.item(0);
//				Log.e("Node Name:",""+node2.getNodeName());
//				Log.e("Node Value:",""+node2.getNodeValue());
				
				
//				Log.e("LostConnector: Xml Parser","uri:"+uri);
				/*************
				 * Using xPath
				 * 
				 */
				
//				XPathFactory xPFactory = XPathFactory.newInstance();
//				XPath xPath = xPFactory.newXPath();
//				XPathExpression expr = xPath.compile("/findServiceResponse/uri");
//
//				Object output = expr.evaluate(doc, XPathConstants.NODESET);
//				NodeList nodes = (NodeList) output;
//				for (int j = 0; j < nodes.getLength(); j++) {
//					Log.e("Xpath", "xPath uri is: " + nodes.item(j).getNodeValue());
//				}
			

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
