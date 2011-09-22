package com.columbia.ng911;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

public class NG911Activity extends Activity {
    /** Called when the activity is first created. */
	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Location location;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        connectivityManager=(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo.isAvailable()){
        	Toast.makeText(getApplicationContext(),"network is available",Toast.LENGTH_SHORT).show();
        }
        if(networkInfo.isConnected()){
        	Toast.makeText(getApplicationContext(),"network is connected",Toast.LENGTH_SHORT).show();

        }
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled){
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        	
        }else{
        	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }
	
	LocationListener locationListener= new LocationListener(){

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),"location is"+location.toString()
					, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	};
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
}