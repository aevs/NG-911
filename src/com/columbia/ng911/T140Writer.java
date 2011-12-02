package com.columbia.ng911;

import java.io.IOException;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class T140Writer extends OutputStream {
	private Handler mHandler;

	public T140Writer (Handler mHandler) {
		this.mHandler = mHandler;
	}
	
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
