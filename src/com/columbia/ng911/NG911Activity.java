package com.columbia.ng911;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.provider.SipProvider;
import org.zoolu.sip.provider.SipStack;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyCharacterMap.KeyData;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NG911Activity extends Activity {
	
	private SipProvider sip;
	private static String TAG=NG911Activity.class.getName();
	
    /** Called when the activity is first created. */
	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Location location;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*
         * Check for network connection
         */
        connectivityManager=(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo.isAvailable()){
        	Toast.makeText(getApplicationContext(),"network is available",Toast.LENGTH_SHORT).show();
        }
        if(networkInfo.isConnected()){
        	Toast.makeText(getApplicationContext(),"network is connected",Toast.LENGTH_SHORT).show();

        }
        /*
         * Check for location via GPS or network provider
         */
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled){
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        	
        }else{
        	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        
        /* Send Message Test Button */
        SipStack.debug_level = 0;
    	//SipStack.log_path = "/data/data/com.columbia.ng911/files/";
    	sip = new SipProvider("10.211.55.3", 0);
    	
        Button sendMessageButton = (Button)findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			if(v.getId() == R.id.sendMessageButton){
    				
    				TextView tv = (TextView)findViewById(R.id.message);
    				String inputMessage = tv.getText().toString();
    				
    				Message msg = MessageFactory.createMessageRequest(sip,
    						new NameAddress(new SipURL("test@10.211.55.3")),
    						new NameAddress(new SipURL("test@10.211.55.2")), 
    						inputMessage, "text/plain", inputMessage);
    				sip.sendMessage(msg);
    			}
    		}
    	});
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
		Log.e(TAG,"onStart()");
		sendMessageEditText=(EditText)findViewById(R.id.message);
	
		sendMessageEditText.setOnKeyListener(rttTextListener);
		sendMessageEditText.addTextChangedListener(rttTextWatcher);
//		InputMethodManager iMM=(InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
//		iMM.showSoftInput(getCurrentFocus(),0 );
		super.onStart();
	}
	EditText sendMessageEditText;
	
	
	
	
	OnKeyListener rttTextListener= new OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			Log.e(TAG,"onKey() of rttTextListener called");
//			if(event.getAction()==KeyEvent.ACTION_DOWN){
				if(keyCode==KeyEvent.KEYCODE_0){
					Log.e(TAG,"caught 0");
				}
				char keyLabel=event.getDisplayLabel();
				String keyLabelString=String.valueOf(keyLabel);
				Log.e(TAG,"onKey() event keyLabel= "+keyLabelString+" keyCode= "+event.getKeyCode());
//				Toast.makeText(getApplicationContext(),keyLabel , 1000).show();
//			}
			return false;
		}
		
		
	};
	TextWatcher rttTextWatcher=new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			Log.e(TAG,"onTextChanged() new char sequence is "+s+" start="+start+" ,before= "+before+",count= "+count);
			Log.e(TAG,"new charSequence is ="+s.subSequence(start,start+count));
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			Log.e(TAG,"afterTextChanged "+s.toString());
		}
	};
	
	KeyListener l=new KeyListener() {
		
		@Override
		public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onKeyOther(View view, Editable text, KeyEvent event) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onKeyDown(View view, Editable text, int keyCode,
				KeyEvent event) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public int getInputType() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void clearMetaKeyState(View view, Editable content, int states) {
			// TODO Auto-generated method stub
			
		}
	};

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
}