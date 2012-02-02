package com.columbia.ng911;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

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
import android.provider.OpenableColumns;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
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
	private Handler rttTimeOutHandler;
	private Handler rttCompleteTextHandler;
	private Handler messageNotSentHandler;
	private Handler msgEditTextHandler;
	private Handler imageSentHandler;

	private static mysip sip;
	private static String TAG = NG911Activity.class.getName();

	public static final String USER_NAME = "userName";
	public static final String USER_PHONE = "userPhone";
	public static final String USER_DATA_SAVED = "userDataSaved";
	private static final int PHOTO_RESULT = 4433;
	private boolean isUserNameSet = false;
	public static boolean killProcess = true;
	public static boolean isCaptureCam = false;
	private final String EARTHQUAKE = "Earthquake in the area";
	private final String FIRE = "Fire in the area, request fire engine";
	private final String MEDICAL_EMERGENCY = "Medical emergency, request ambulance";
	private final String FLOOD = "Flood alert";
	private final String GUNSHOTS = "Gunshots heard, request police assistance";
	private final String ROAD_ACCIDENT = "Road Accident, request ambulance";

	private final boolean FLAG_MESSAGE_FROM_USER = false;
	private final boolean FLAG_MESSAGE_FROM_911 = true;

	public static final int IMAGE_RECEIVED_RESULT = 39485439;

	private ArrayAdapter<String> arrayAdapter;
	private CustomArrayAdapter customArrayAdapter;

	private ListView chatWindowListView;
	private TextView rttResponseTextView;

	/** Called when the activity is first created. */
	LocationManager locationManager;
	ConnectivityManager connectivityManager;
	Location location;
	public boolean isConnected;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		SipStack.debug_level = 0;

		rttResponseTextView = (TextView) findViewById(R.id.rttResponseWindow);

		/**********************
		 * 
		 * Request for user data only the first time
		 * 
		 ************************/
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		if (!sharedPreferences.getBoolean(USER_DATA_SAVED, false)) {
			showAlertDialogForUserData();
		} else
			isUserNameSet = true;

		connectivityManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		Log.e("onCreate()", "called");

		/*******************************************
		 * Real Time Text or Normal Radio Button
		 *******************************************/
		RadioButton textTypeButton = (RadioButton) findViewById(R.id.RTP);
		textTypeButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						EditText input = (EditText) findViewById(R.id.message);
						if (arg1) {
							Log.e("RTT_BUTTON", "yes");
							// input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
							findViewById(R.id.rttResponseWindow).setVisibility(
									View.VISIBLE);
							findViewById(R.id.sendMessageButton).setVisibility(
									View.INVISIBLE);
						} else {
							// input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
							findViewById(R.id.rttResponseWindow).setVisibility(
									View.INVISIBLE);
							findViewById(R.id.sendMessageButton).setVisibility(
									View.VISIBLE);
							Log.e("RTT_BUTTON", "no");
						}
						sipController.setIsRealTime(arg1);
					}
				});

		// initialise array Adapter
		arrayAdapter = new ArrayAdapter<String>(this, R.layout.adapterunituser);
		customArrayAdapter = new CustomArrayAdapter(getApplicationContext(),
				R.id.adapterUnitText911);

		chatWindowListView = (ListView) findViewById(R.id.chatListView);
		chatWindowListView.setAdapter(customArrayAdapter);

		/*******************************************
		 * 
		 * Camera button
		 * 
		 *******************************************/
		ImageButton cameraButton = (ImageButton) findViewById(R.id.sendPhotoButton);
		cameraButton.setOnClickListener(cameraButtonOnClickListener);

		/****************
		 * Handler to finalize text with "." at the end of it
		 * 
		 ****************/
		rttTimeOutHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String incomingMessage = (String) msg.obj;

				customArrayAdapter.add("Me: " + incomingMessage,
						FLAG_MESSAGE_FROM_USER);
				sendMessageEditText.setText("");
				sipController.sendRTT('\r');
				sipController.sendRTT(T140Constants.LINE_FEED);
				Log.e("MAIN INCOMING: ", incomingMessage);
			}
		};
		/************************
		 * Alert user in RTT mode if text left incomplete
		 * 
		 *************************/
		rttCompleteTextHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(),
						"Please complete text..", Toast.LENGTH_LONG).show();

			}

		};
		/****************************
		 * 
		 * Handler to display message to user when image is sent.
		 * 
		 ***************************/
		imageSentHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				customArrayAdapter.addErrorMessage("Image Sent");
			}

		};
		/************
		 * Sip message time out handler
		 * 
		 ************/
		messageNotSentHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String incomingMessage = (String) msg.obj;
				Toast.makeText(NG911Activity.this,
						incomingMessage + " : failed", Toast.LENGTH_LONG)
						.show();
				customArrayAdapter.addErrorMessage("Sending failed: "
						+ incomingMessage);
			}
		};

		/*****************
		 * 
		 * Handler to push sip messages from ng911 server to bubble List.
		 * 
		 *****************/
		sipHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String incomingMessage = (String) msg.obj;
				// arrayAdapter.add("\n Server: " + incomingMessage);
				customArrayAdapter.add("911: " + incomingMessage,
						FLAG_MESSAGE_FROM_911);
				Log.e("MAIN INCOMING: ", incomingMessage);
			}
		};

		/********************************
		 * SipController Initialize
		 *******************************/
		msgEditTextHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int result = (int) msg.arg1;
				EditText editText = (EditText) findViewById(R.id.message);
				if (result == 1)
					editText.setEnabled(true);
				else
					editText.setEnabled(false);
			}
		};

		
		/********************
		 * 
		 * Handler for RTT messages
		 * 
		 *********************/
		t140Handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				char c = (char) msg.arg1;
				String tmp = t140IncomingCharSeq.toString();
				if (tmp.length() == 0)
					t140IncomingBuffer.append("\n 911: ");
				/************
				 * Temporary buffer needs to be written to middle textBox
				 */

				if (c == T140Constants.BACKSPACE) {
					tmp = t140IncomingCharSeq.toString();
					if (tmp.length() > 0)
						t140IncomingBuffer.deleteCharAt(tmp.length() - 1);
					tmp = t140IncomingCharSeq.toString();
					rttResponseTextView.setText(tmp);
				} else {
					t140IncomingBuffer.append(Character.toString(c));
					tmp = t140IncomingCharSeq.toString();
					rttResponseTextView.setText(tmp);
				}

				if ((int) msg.arg1 == 13) { // \n case
					t140IncomingBuffer.setLength(0);
					customArrayAdapter.add(rttResponseTextView.getText()
							.toString(), FLAG_MESSAGE_FROM_911);
					rttResponseTextView.setText("");
				}

				Log.e("T140Incoming", Integer.toString(msg.arg1));
			}
		};
		t140writer = new T140Writer(t140Handler);
		sipController = new SipController(this, "test", "128.59.22.88", "5080",
				t140writer, getDevicePhoneNumber(), getLocalIpAddress());

		sip = new mysip(sipController.getSharedSipProvider(), this,
				getLocalIpAddress(), sipHandler, messageNotSentHandler);

		/***********************
		 * 
		 * Initialize "Send" button and set click listener.
		 * 
		 ***********************/
		Button sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		sendMessageButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {


				if (v.getId() == R.id.sendMessageButton) {
					TextView tv = (TextView) findViewById(R.id.message);
					String inputMessage = tv.getText().toString();
					Log.e("inputMessageSIP: ", inputMessage);

					sip.send(inputMessage);
					customArrayAdapter.add("Me: " + inputMessage,
							FLAG_MESSAGE_FROM_USER);
					// }
					tv.setText("");
				}
			}
		});

		sendMessageEditText = (EditText) findViewById(R.id.message);
		sendMessageEditText.setOnKeyListener(rttTextListener);
		sendMessageEditText.addTextChangedListener(rttTextWatcher);
	}

	class RTTAutoConnectThread implements Runnable {
		public void run() {
			Message msg = new Message();
			msg.arg1 = 0;
			msgEditTextHandler.sendMessage(msg);

			while (isUserNameSet == false) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Log.e("RTTAuto", "Thread Sleep Error");
				}
			}

			while (sipController == null) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Log.e("RTTAuto", "Thread Sleep Error");
				}
			}

			while (Geolocation.getIsUpdated() != true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Log.e("RTTAuto", "Thread Sleep Error");
				}
			}

			sipController.call();

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				Log.e("RTTAuto", "Thread Sleep Error");
			}

			Message msg2 = new Message();
			msg2.arg1 = 1;
			msgEditTextHandler.sendMessage(msg2);
		}
	}
/**
 * Check if network connectivity is possible, display alert dialog box if not possible.
 * 
 */
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
			showAlertDialog("No Network Connectivity");
		}
	}

/**
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

/**
 * Get phone number of device from TelephonyManager
 * 
 */
	private String getDevicePhoneNumber() {

		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		Log.e("Telephone Number: ", "" + tm.getLine1Number());
		return tm.getLine1Number();
	}
/**
 * 
 * Display alert dialog box which prompts user for name and phone number.
 * 
 */
	private void showAlertDialogForUserData() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				NG911Activity.this);
		LayoutInflater factory = LayoutInflater.from(this);
		final View userDataView = factory.inflate(R.layout.userdata, null);
		alertDialogBuilder.setView(userDataView);
		alertDialogBuilder.setTitle("User data");
		
		//Action to be taken on clicking "save":
		//If user has entered name and phone number, save in sharedPreferences,
		// else retrieve email id from account details and telephone number from
		//Telephony manager and save in shared Preferences.
		alertDialogBuilder.setPositiveButton("Save",
				new DialogInterface.OnClickListener() {

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

							// Set undefined as default
							sharedPrefsEditor.putString(USER_NAME, "user");

						} else {
							sharedPrefsEditor.putString(USER_NAME, userName
									.getText().toString());
						}
						sharedPrefsEditor.putString(USER_PHONE,
								getDevicePhoneNumber());
						sharedPrefsEditor.putBoolean(USER_DATA_SAVED, true);
						
						//Finally commit value to sharedPreferences to be looked up across power cycles.
						sharedPrefsEditor.commit();

						isUserNameSet = true;

					}
				});

		//Action to be taken on clicking "close":
		//Retrieve email id and telephone number from phone and save in shared preferences.
		alertDialogBuilder.setNegativeButton("Close",
				new DialogInterface.OnClickListener() {

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
						isUserNameSet = true;

						dialog.dismiss();
					}
				});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

	}
/**
 * Alert dialog to be popped if app needs to be exited due to either lack of network connectivity,
 * or lack of PSAP server. Prompt user with a button to call 911 instead which allows the user to directly call 911.
 * User can therefore only either call 911 or exit the app and cannot continue to use the app.
 * @param reason The reason because of which the app cannot continue further eg."No network connectivity"
 */
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
/********************************************
 * 	35354 to be changed to 911 when deployed. Currently on garbage value so that
 * 911 does not get called during test.
 *******************************************/
						callIntent.setData(Uri.parse("tel:" + 35354));
/*******************************************/						
						callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(callIntent);
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
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();
	}
/**
 * 
 * Quick response templates. Presented on clicking the menu button on phone.
 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.earthquake:
			sip.send(EARTHQUAKE);
			customArrayAdapter.add("Me: TEMPLATE: " + EARTHQUAKE,
					FLAG_MESSAGE_FROM_USER);
			break;
		case R.id.fire:
			sip.send(FIRE);
			customArrayAdapter.add("Me: TEMPLATE: " + FIRE,
					FLAG_MESSAGE_FROM_USER);
			break;
		case R.id.flood:
			sip.send(FLOOD);
			customArrayAdapter.add("Me: TEMPLATE: " + FLOOD,
					FLAG_MESSAGE_FROM_USER);
			break;
		case R.id.gunshots:
			sip.send(GUNSHOTS);
			customArrayAdapter.add("Me: TEMPLATE: " + GUNSHOTS,
					FLAG_MESSAGE_FROM_USER);
			break;
		case R.id.roadAccident:
			sip.send(ROAD_ACCIDENT);
			customArrayAdapter.add("Me: TEMPLATE: " + ROAD_ACCIDENT,
					FLAG_MESSAGE_FROM_USER);
			break;
		case R.id.medicalEmergency:
			sip.send(MEDICAL_EMERGENCY);
			customArrayAdapter.add("Me: TEMPLATE: " + MEDICAL_EMERGENCY,
					FLAG_MESSAGE_FROM_USER);
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}
/**
 * 
 * Runnable class to send the image in a separate thread to prevent slowing down the UI thread.
 * @author acs
 *
 */
	class SendImage implements Runnable {
		String imageString;
		Handler handler;

		SendImage(String imageString, Handler handler) {
			this.imageString = imageString;
			this.handler = handler;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			sip.sendImage(imageString);
			handler.sendEmptyMessage(2);
		}

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


					try {
						Uri uri = (Uri) data.getExtras().get(
								CameraCapture.JPEG_STRING);
						byte[] imageBytes = readBytesFromUriInputStream(uri);
						JpegImage.imageBytes = imageBytes;
						String imageString = new String(imageBytes);
						new Thread(new SendImage(imageString, imageSentHandler))
								.start();
						customArrayAdapter.addErrorMessage("Sending Image..");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						customArrayAdapter
								.addErrorMessage("Error Sending Image");
						e.printStackTrace();

					}



				}
			}
		}
	}

	public byte[] readBytesFromUriInputStream(Uri uri) throws IOException {

		InputStream inputStream = getContentResolver().openInputStream(uri);

		// this dynamically extends to take the bytes you read
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		// we need to know how may bytes were read to write them to the
		// byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}

		// and then we can return your byte array.
		return byteBuffer.toByteArray();
	}

	public byte[] getBytesFromFile(Uri uri) throws IOException {

		// FileInputStream fis=openFileInput()
		File file = new File(uri.getEncodedPath());
		InputStream is = new FileInputStream(file);
		// InputStream is = getContentResolver().openInputStream(
		// uri);

		// Get the size of the file
		long length = file.length();
		Log.e("File Length: ", "" + length);
		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	OnClickListener cameraButtonOnClickListener = new OnClickListener() {
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			NG911Activity.killProcess = false;
			NG911Activity.isCaptureCam = true;

			Intent intent = new Intent(getBaseContext(), CameraCapture.class);
			startActivityForResult(intent, IMAGE_RECEIVED_RESULT);
		}
	};

	static boolean flagLostSent = false;
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			LostConnector lostConnector = LostConnector.getInstance();
			lostConnector.setContext(getApplicationContext());
			lostConnector.setLocation(location.getLatitude(),
					location.getLongitude());

			Geolocation
					.updateGeolocatoin(String.valueOf(location.getLongitude()),
							String.valueOf(location.getLatitude()),
							getLocalIpAddress());

			Log.e("Geolocation: ", "" + String.valueOf(location.getLongitude())
					+ String.valueOf(location.getLatitude()));
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
		Log.e(TAG, "onPause() killProcess = " + killProcess
				+ ", isCaptureCam = " + isCaptureCam);

		if (!killProcess && isCaptureCam) {
			Log.e("CAP", "Start Taking Pic");
			killProcess = true;
		} else if (killProcess && isCaptureCam) {
			Log.e("CAP", "Closing Taking Pic");
			isCaptureCam = false;
		}

		if (killProcess && !isCaptureCam) {
			Log.e("CAP", "APP Quit");
			android.os.Process.killProcess(android.os.Process.myPid());
		}

		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Thread rttAutoConnectThread = new Thread(new RTTAutoConnectThread());
		rttAutoConnectThread.start();
		super.onResume();
	}

	public void displayIncoming(String message) {
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
						String ipAddress = inetAddress.getHostAddress()
								.toString();
						Log.e("Ipaddress: ", ipAddress);
						return ipAddress;
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
			if (keyCode == 66 && !sipController.isRealTime()) {
				TextView tv = (TextView) findViewById(R.id.message);
				String inputMessage = tv.getText().toString();
				Log.e("inputMessageSIP: ", inputMessage);

				if (!inputMessage.startsWith("\n")) {
					sip.send(inputMessage);
					customArrayAdapter.add("Me: " + inputMessage,
							FLAG_MESSAGE_FROM_USER);
				}
				// }
				tv.setText("");
			}
			if (keyCode == 67)
				sipController.sendRTT((char) 0x08);
			return false;
		}

	};

	boolean isTimerTaskScheduled = false;
	boolean isCompleteTextTimerTaskScheduled = false;
	Timer timeOutTimer = new Timer();
	Timer completeTextTimer = new Timer();

	TextWatcher rttTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			Log.e(TAG, "onTextChanged() new char sequence is " + s + " start="
					+ start + " ,before= " + before + ",count= " + count);
			Log.e(TAG,
					"new charSequence is ="
							+ s.subSequence(start, start + count));

			RadioButton rb = (RadioButton) findViewById(R.id.RTP);
			if (rb.isChecked()) {
				// RTT send
				if (count > 0) {
					if ((int) s.charAt(start) != 10)
						sipController.sendRTT(s.charAt(start));
					else {
						sipController.sendRTT('\r');
					}

				}

				if (isTimerTaskScheduled) {
					timeOutTimer.cancel();
					timeOutTimer.purge();
					timeOutTimer = new Timer();
				}
				if (isCompleteTextTimerTaskScheduled) {
					completeTextTimer.cancel();
					completeTextTimer.purge();
					completeTextTimer = new Timer();
				}
				if (sendMessageEditText.getLineCount() > 1) {

					customArrayAdapter.add("User: "
							+ sendMessageEditText.getText().toString(),
							FLAG_MESSAGE_FROM_USER);
					sipController.sendRTT(T140Constants.LINE_FEED);
					sendMessageEditText.setText("");

				} else if (count > 0
						&& String.valueOf(s.charAt(start)).equals(".")) {

					timeOutTimer.schedule(new TimeOutTimerTask(
							rttTimeOutHandler), 4000);
					isTimerTaskScheduled = true;

				} else {
					completeTextTimer.schedule(new CompleteTextTimerTask(
							rttCompleteTextHandler), 10000);
					isCompleteTextTimerTaskScheduled = true;
				}
			}
		}

		class CompleteTextTimerTask extends TimerTask {
			Handler handler;

			public CompleteTextTimerTask(Handler handler) {
				this.handler = handler;
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				handler.sendMessage(msg);
				isCompleteTextTimerTaskScheduled = false;
			}

		}

		class TimeOutTimerTask extends TimerTask {
			Handler handler;

			public TimeOutTimerTask(Handler handler) {
				this.handler = handler;
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (sendMessageEditText.getText().toString().length() > 0) {
					// customArrayAdapter.add("911: "+sendMessageEditText.getText().toString(),FLAG_MESSAGE_FROM_911);
					Message msg = new Message();
					msg.obj = sendMessageEditText.getText().toString();
					handler.sendMessage(msg);
					isTimerTaskScheduled = false;
				}
			}
		};

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
