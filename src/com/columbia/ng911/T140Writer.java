package com.columbia.ng911;

import java.io.IOException;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * T140Writer is the helper class to send RTT
 *
 */
public class T140Writer extends OutputStream {
	private Handler mHandler;

	/**
	 * T140Writier Initializer Method
	 * @param mHandler Event Message Handler to be notified current receiving Real-Time-Text 
	 */
	public T140Writer (Handler mHandler) {
		this.mHandler = mHandler;
	}
	
	/**
	 * OutputStream Overriding method to display received RTT
	 */
	@Override
	public void write(int oneByte) throws IOException {
                if (oneByte < 0) {
                        Log.e("T140-ERROR", "Receving T140 Error Occur");
                        return;
                }
                Message msg = new Message();
                msg.arg1 = oneByte;
                mHandler.sendMessage(msg);
	}
}