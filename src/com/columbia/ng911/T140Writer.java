package com.columbia.ng911;

import java.io.IOException;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;

public class T140Writer extends OutputStream {
	private Handler mHandler;

	public T140Writer (Handler mHandler) {
		this.mHandler = mHandler;
	}
	
	@Override
	public void write(int oneByte) throws IOException {
		Message msg = new Message();
	    msg.arg1 = oneByte;
	    mHandler.sendMessage(msg);
	}
}
