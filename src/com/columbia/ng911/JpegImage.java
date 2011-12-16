package com.columbia.ng911;

import android.util.Log;

public class JpegImage {

	public static byte[] imageBytes;
	
	public static void setImageBytes(byte[] image)
	{
		imageBytes = image;
		Log.e("image in JpegImage: ",new String(image));
	}



}
