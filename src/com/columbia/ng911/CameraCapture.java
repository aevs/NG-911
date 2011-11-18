package com.columbia.ng911;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CameraCapture extends Activity {

	protected static final String JPEG_STRING = "jpegString";
	private static String TAG="CameraCapture";
	private Preview mPreview;
	Camera mCamera;

	private ImageButton takePictureButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mCamera = Camera.open();
		mPreview = new Preview(this, mCamera);
		setContentView(mPreview);

		// "take picture" button on camera preview surface
		takePictureButton = new ImageButton(getApplicationContext());
		RelativeLayout.LayoutParams imageButtonParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		imageButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);

		addContentView(takePictureButton, imageButtonParams);
		takePictureButton.setOnClickListener(takePictureClickListener);

	}

	OnClickListener takePictureClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);

		}
	};

	ShutterCallback shutterCallback = new ShutterCallback() {

		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),
					"Image has been captured..", 1000).show();

		}
	};
	PictureCallback rawCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub

//			Log.e(TAG+" onPictureTaken()", "Raw data is:------------- "
//					+ data.length + " -----------");
			
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub

			String jpegString=new String(data);
			Log.e(TAG+" onPictureTaken()", "Jpeg data is:********* "
					+ jpegString + " ************");

			// Display on screen..send to LoSt server
			Bitmap pictureTaken = BitmapFactory.decodeByteArray(data, 0,
					data.length);
			
			
			ContentValues contentValues=new ContentValues();
			contentValues.put(Images.Media.TITLE, "image");
			
			Uri uri=getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
					contentValues);
			
			OutputStream outputStream;
			try {
				outputStream = getContentResolver().openOutputStream(uri);
				boolean compressed=pictureTaken.compress(Bitmap.CompressFormat.JPEG,20,outputStream);
				Log.e(TAG,"picture successfully compressed at:"+uri+compressed);
				outputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
//			Log.e(TAG+ "onpictureTaken()","bitmap is: "+pictureTaken.toString());
//			ImageView imageView = new ImageView(getApplicationContext());
//			imageView.setImageBitmap(pictureTaken);
//			setContentView(imageView);
			
			Log.e(TAG,"jpeg data length "+data.length);
			
			Intent intent= new Intent();
			intent.putExtra(CameraCapture.JPEG_STRING, uri);
			setResult(NG911Activity.IMAGE_RECEIVED_RESULT, intent);
			finish();
			
		}
	};

	
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	SurfaceHolder mHolder;
	Camera mCamera;

	Preview(Context context, Camera camera) {
		super(context);

		this.mCamera = camera;
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		// mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
			// TODO: add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

}
