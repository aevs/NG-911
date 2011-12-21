package com.columbia.ng911;

public class Geolocation {
	static String pidflo;
	private static boolean isUpdated=false;
	//A static function to get the PIDF-LO. Now this function can be called from wherever as needed.
	
	public static void updateGeolocatoin(String lon, String lat)
	{
		pidflo ="MIME-Version: 1.0\n"
				+"Content-ID: <android@192.168.2.6>\n"
				+"Content-Type: application/pidf+xml\n"
				+"Content-Transfer-Encoding: 8bit\n\n"
		+"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+"<presence xmlns=\"urn:ietf:params:xml:ns:pidf\" "
	      	+"xmlns:dm=\"urn:ietf:params:xml:ns:pidf:data-model\" "
	      	+"xmlns:gp=\"urn:ietf:params:xml:ns:pidf:geopriv10\" "
	      	+"xmlns:cl=\"urn:ietf:params:xml:ns:pidf:geopriv10:civicAddr\" "
	      	+"xmlns:gml=\"http://www.opengis.net/gml\" "
	      	+"entity=\"sip:pranaydalmia@android.com\">"
		+"<tuple id=\"id23430\">"
	    +"<status>"  	
		+"<gp:geopriv>"
		+"<gp:location-info>"
		+"<gml:Point srsName=\"urn:ogc:def:crs:EPSG::4326\">"
		+"<gml:pos>"+lat+" "+lon+"</gml:pos>"
	    +"</gml:Point>"
	    +"</gp:location-info>"
	    +"<gp:usage-rules/>"
	    +"</gp:geopriv>"
	    +"</status>"
	    +"<contact priority=\"0.8\">sip:pranaydalmia@android.com</contact>"
	    +"<timestamp>2011-11-22T20:57:29Z</timestamp>"
	    +"</tuple>"
     	+"</presence>\n";
		
		
		isUpdated= true;
	}
	
	public static boolean getIsUpdated()
	{
		return isUpdated;
	}
	
	
	public static void setIsUpdated(boolean flag)
	{
		isUpdated = flag;
	}
	public static String getGeolocation()
	{
		isUpdated = false;
		return pidflo;
	}
}
