package com.columbia.ng911;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.zoolu.sip.provider.SipStack;

import se.omnitor.protocol.t140.T140Constants;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class NG911Activity extends Activity {
	/* For SIP */
	private Handler sipHandler;
	private SipController sipController;
	private Handler t140Handler;
	private T140Writer t140writer;
	private StringBuffer t140IncomingBuffer = new StringBuffer("");
	private CharSequence t140IncomingCharSeq = t140IncomingBuffer;

	private static mysip sip;
	private static String TAG = NG911Activity.class.getName();

	public static final String USER_NAME = "userName";
	public static final String USER_PHONE = "userPhone";
	public static final String USER_DATA_SAVED = "userDataSaved";
	private static final int PHOTO_RESULT = 4433;
	
	private final String EARTHQUAKE="Earthquake in the area";
	private final String FIRE="Fire in the area, request fire engine";
	private final String MEDICAL_EMERGENCY="Medical emergency, request ambulance";
	private final String FLOOD="Flood alert";
	private final String GUNSHOTS="Gunshots heard, request police assistance";
	private final String ROAD_ACCIDENT="Road Accident, request ambulance";

	public static final int IMAGE_RECEIVED_RESULT = 39485439;

	private ArrayAdapter<String> arrayAdapter;
	private ListView chatWindowListView;

	/** Called when the activity is first created. */
	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Location location;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SipStack.debug_level = 0;

		/**********************
		 * 
		 * Request for user data only the first time
		 * 
		 ************************/
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (!sharedPreferences.getBoolean(USER_DATA_SAVED, false)) {
			showAlertDialogForUserData();
		}

		connectivityManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		alertIfNoNetwork();

		/*******************************************
		 * Real Time Text or Normal Radio Button
		 *******************************************/
		RadioButton textTypeButton = (RadioButton) findViewById(R.id.RTP);
		textTypeButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (arg1){
							Log.e("RTT_BUTTON", "yes");
							findViewById(R.id.rttResponseWindow).setVisibility(View.VISIBLE);
						}
						else{
							findViewById(R.id.rttResponseWindow).setVisibility(View.INVISIBLE);
							Log.e("RTT_BUTTON", "no");
						}
						sipController.setIsRealTime(arg1);
					}
				});

		// initialise array Adapter
		arrayAdapter = new ArrayAdapter<String>(this, R.layout.adapterunit);
		chatWindowListView = (ListView) findViewById(R.id.chatListView);
		chatWindowListView.setAdapter(arrayAdapter);

		/*******************************************
		 * 
		 * Camera button
		 * 
		 *******************************************/
		ImageButton cameraButton = (ImageButton) findViewById(R.id.sendPhotoButton);
		cameraButton.setOnClickListener(cameraButtonOnClickListener);

		sipHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String incomingMessage = (String) msg.obj;
				arrayAdapter.add("\n Server: " + incomingMessage);
				Log.e("MAIN INCOMING: ", incomingMessage);
			}
		};

		/********************************
		 * SipController Initialize
		 *******************************/
		t140Handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				char c = (char) msg.arg1;
				String tmp = t140IncomingCharSeq.toString();
				if (tmp.length() == 0)
					t140IncomingBuffer.append("\n Server: ");
				else
					arrayAdapter.remove(tmp);

				t140IncomingBuffer.append(Character.toString(c));
				tmp = t140IncomingCharSeq.toString();
				arrayAdapter.add(tmp);

				if ((int) msg.arg1 == 13) // \n case
					t140IncomingBuffer.setLength(0);

				Log.e("T140Incoming", Integer.toString(msg.arg1));
			}
		};
		t140writer = new T140Writer(t140Handler);
		sipController = new SipController("test", "128.59.22.88", "5080",
				t140writer);

		sip = new mysip(sipController.getSharedSipProvider(), this,
				getLocalIpAddress(), sipHandler);

//		Button callButton = (Button) findViewById(R.id.call);
//		callButton.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				System.out.println("Outgoing call");
//				sipController.call();
//			}
//		});
//
//		Button hangButton = (Button) findViewById(R.id.hang);
//		hangButton.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				System.out.println("Hang up call");
//				sipController.hangup();
//			}
//		});

		Button sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		sendMessageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// Intent for starting preview..does not take photos yet.

				if (v.getId() == R.id.sendMessageButton) {
					TextView tv = (TextView) findViewById(R.id.message);
					String inputMessage = tv.getText().toString();

					// sipController.send(inputMessage);

					if (sipController.isRealTime())
						sipController.sendRTT(T140Constants.CR_LF);
					else
						sip.send(inputMessage);
					tv.setText("");

					// chatWindowTextView.append("\n User: "+inputMessage);
					arrayAdapter.add("\n User: " + inputMessage);

				}
			}
		});

		/*
		 * Jin : I moved these from onStart(), because onStart() called several
		 * times after closing camera
		 */
		sendMessageEditText = (EditText) findViewById(R.id.message);
		sendMessageEditText.setOnKeyListener(rttTextListener);
		sendMessageEditText.addTextChangedListener(rttTextWatcher);
	}

	private void alertIfNoNetwork() {
		NetworkInfo[] networkInfoTrial = connectivityManager
				.getAllNetworkInfo();
		boolean isConnected = false;
		for (int i = 0; i < networkInfoTrial.length; i++) {
			if (networkInfoTrial[i].isConnected()) {
				isConnected = true;
			}
		}
		if (isConnected == false) {
			Toast.makeText(getApplicationContext(), "network is not available",
					Toast.LENGTH_LONG).show();
			// AlertDialog connectedToNetwork=new
			// AlertDialog(getBaseContext(),false,);
			showAlertDialog("No Network Connectivity");

		}
	}

	/**********
	 * Get account name registered with phone- generally email id
	 * 
	 */
	private String getAccountName() {
		AccountManager accountManager = AccountManager
				.get(getApplicationContext());
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Log.e("Account NAME", " " + accounts[0].name);

		return accounts[0].name;
	}

	/**********
	 * Get phone number of device from TelephonyManager
	 * 
	 */
	private String getDevicePhoneNumber() {

		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Log.e("Telephone Number: ", "" + tm.getLine1Number());
		return tm.getLine1Number();
	}

	private void showAlertDialogForUserData() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				NG911Activity.this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View userDataView = factory.inflate(R.layout.userdata, null);
		alertDialogBuilder.setView(userDataView);
		alertDialogBuilder.setTitle("User data");
		alertDialogBuilder.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						EditText userName = (EditText) userDataView
								.findViewById(R.id.userName);
						SharedPreferences sharedPrefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						Editor sharedPrefsEditor = sharedPrefs.edit();

						// If text fields are blank, retrieve and store value
						// from device
						if (userName.getText().toString().equals("")) {
							sharedPrefsEditor.putString(USER_NAME,
									getAccountName());
						} else {
							sharedPrefsEditor.putString(USER_NAME, userName
									.getText().toString());
						}
							sharedPrefsEditor.putString(USER_PHONE,
									getDevicePhoneNumber());
						sharedPrefsEditor.putBoolean(USER_DATA_SAVED, true);
						sharedPrefsEditor.commit();

					}
				});

		alertDialogBuilder.setNegativeButton("Close",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						SharedPreferences sharedPrefs = PreferenceManager
								.getDefaultSharedPreferences(getApplicationContext());
						Editor sharedPrefsEditor = sharedPrefs.edit();
						sharedPrefsEditor
								.putString(USER_NAME, getAccountName());
						sharedPrefsEditor.putString(USER_PHONE,
								getDevicePhoneNumber());
						sharedPrefsEditor.commit();

						dialog.dismiss();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

	}

	private void showAlertDialog(String reason) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				NG911Activity.this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setTitle(reason);
		alertDialogBuilder.setMessage("Exiting app, call 911?");

		alertDialogBuilder.setPositiveButton("Call 911",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Intent callIntent = new Intent(Intent.ACTION_CALL);
						callIntent.setData(Uri.parse("tel:" + 35354));
						callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(callIntent);
						// finish();
					}
				});

		alertDialogBuilder.setNegativeButton("Exit",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						finish();
					}
				});
		// AlertDialog.Builder
		// alertDialogBuilder=initializeAlertDialog("No Network Connectivity",
		// "Exiting app..Call 911?");
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}

	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()){
			case R.id.earthquake:
				sip.send(EARTHQUAKE);
				arrayAdapter.add("User:TEMPLATE: " + EARTHQUAKE);
				break;
			case R.id.fire:
				sip.send(FIRE);
				arrayAdapter.add("User:TEMPLATE: "+FIRE);
				break;
			case R.id.flood:
				sip.send(FLOOD);
				arrayAdapter.add("User:TEMPLATE: " + FLOOD);
				break;
			case R.id.gunshots:
				sip.send(GUNSHOTS);
				arrayAdapter.add("User:TEMPLATE: "+GUNSHOTS);
				break;
			case R.id.roadAccident:
				sip.send(ROAD_ACCIDENT);
				arrayAdapter.add("User:TEMPLATE:"+ROAD_ACCIDENT);
				break;
			case R.id.medicalEmergency:
				sip.send(MEDICAL_EMERGENCY);
				arrayAdapter.add("User:TEMPLATE: "+MEDICAL_EMERGENCY);
				break;
				
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			if (!data.getExtras().isEmpty()) {
				if (requestCode == PHOTO_RESULT) {

					Bitmap photoResult = (Bitmap) data.getExtras().get("data");
					ImageView imageView = new ImageView(
							this.getApplicationContext());
					imageView.setImageBitmap(photoResult);
					setContentView(imageView);

				}
				if (requestCode == IMAGE_RECEIVED_RESULT) {
					// byte[] jpegByteArray=(byte[])
					// data.getExtras().get(CameraCapture.JPEG_STRING);
					Uri uri = (Uri) data.getExtras().get(
							CameraCapture.JPEG_STRING);
					try {
						InputStream is = getContentResolver().openInputStream(
								uri);
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);

						StringBuilder sb = new StringBuilder();

						String read = br.readLine();
						int i = 0;
						while (read != null) {
							read = br.readLine();
							sb.append(read);
						}
						String jpegString = sb.toString();

						sip.sendImage(jpegString);

						Log.e(TAG, "jpegString is: " + jpegString);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Log.e(TAG, "Received image result from cameraCapture class");

					// Bitmap pictureTaken =
					// BitmapFactory.decodeByteArray(jpegByteArray, 0,
					// jpegByteArray.length);
					// Log.e(TAG+
					// "onpictureTaken()","bitmap is: "+pictureTaken.toString());
					// ImageView imageView = new
					// ImageView(getApplicationContext());
					// imageView.setImageBitmap(pictureTaken);
					// setContentView(imageView);

				}
			}
		}
	}

	OnClickListener cameraButtonOnClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			// TODO Auto-generated method stub

			Intent intent = new Intent(getBaseContext(), CameraCapture.class);
			startActivityForResult(intent, IMAGE_RECEIVED_RESULT);

			// Android Camera API
			// Intent intent = new Intent(
			// android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			// startActivityForResult(intent, PHOTO_RESULT);

		}
	};

	static boolean flagLostSent = false;
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			/*
			 * Toast.makeText(getApplicationContext(),"location is"+location.
			 * toString() , Toast.LENGTH_LONG).show();
			 */
			// if(!flagLostSent){
			LostConnector lostConnector = LostConnector.getInstance();
			lostConnector.setContext(getApplicationContext());
			lostConnector.setLocation(location.getLatitude(),
					location.getLongitude());

			Geolocation.updateGeolocatoin(
					String.valueOf(location.getLongitude()),
					String.valueOf(location.getLatitude()));

			Log.e("Geolocation: ",""+ String.valueOf(location.getLongitude())+
					String.valueOf(location.getLatitude()));
			/**********
			 * Exit app based on PSAP server response
			 *********/
			if (lostConnector.requestSent() == false) {
				String serverIp = lostConnector.getPSAPD();
				if (serverIp == null) {
					showAlertDialog("Could not reach PSAP server");
				} else if (serverIp.equals(LostConnector.NO_RESPONSE)) {
					showAlertDialog(serverIp);
				} else if (serverIp.equals("")) {
					showAlertDialog("No PSAP server nearby");
				}
			}
			flagLostSent = true;
			// }
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

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
		// locationManager.removeUpdates(locationListener);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public void displayIncoming(String message) {
		// System.out.println("\n PSAP: "+message);
		Toast.makeText(getApplicationContext(), "Received:" + message,
				Toast.LENGTH_LONG).show();

	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("NETWORK", ex.toString());
		}
		return null;
	}

	public void notifyTimeout(MessageTime mt) {
		// TODO : Code to update UI if the delivery of a message failed.
		Log.e("Timeout", mt.message + "/" + mt.tag);
		Toast.makeText(getApplicationContext(),
				mt.message + ": Not Delivered!", Toast.LENGTH_LONG).show();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStart()");

		try {
			alertIfNoNetwork();
			/**************************
			 * 
			 * Check for location via GPS or network provider
			 * 
			 **************************/
			locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			boolean isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (isGPSEnabled) {
				locationManager
						.requestLocationUpdates(LocationManager.GPS_PROVIDER,
								1000, 0, locationListener);

			} else {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 1000, 0,
						locationListener);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// InputMethodManager
		// iMM=(InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
		// iMM.showSoftInput(getCurrentFocus(),0 );
		super.onStart();
	}

	EditText sendMessageEditText;

	OnKeyListener rttTextListener = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onKey() of rttTextListener called");

			if (keyCode == KeyEvent.KEYCODE_0) {
				Log.e(TAG, "caught 0");
			}

			char keyLabel = event.getDisplayLabel();
			String keyLabelString = String.valueOf(keyLabel);
			Log.e(TAG,
					"onKey() event keyLabel= " + keyLabelString + " keyCode= "
							+ event.getKeyCode() + " Unicode= "
							+ event.getUnicodeChar());
			if (keyCode == 67)
				sipController.sendRTT((char) 0x08);
			return false;
		}

	};
	TextWatcher rttTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onTextChanged() new char sequence is " + s + " start="
					+ start + " ,before= " + before + ",count= " + count);
			Log.e(TAG,
					"new charSequence is ="
							+ s.subSequence(start, start + count));

			// RTT send
			if (count > 0)
				sipController.sendRTT(s.charAt(start));

			// chatWindowTextView.setText(s);
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			Log.e(TAG, "afterTextChanged " + s.toString());
		}
	};

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		locationManager.removeUpdates(locationListener);
	}
}