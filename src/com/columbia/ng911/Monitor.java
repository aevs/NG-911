package com.columbia.ng911;

import java.util.Iterator;
import java.util.TimerTask;

import android.util.Log;

public class Monitor extends TimerTask{

	mysip sip;
	public Monitor(mysip sip)
	{
		this.sip = sip;
	}
	@Override 
	public void run() {
		// TODO Auto-generated method stub
		//Log.e("Thread List", sip.messagelist.toString());
		Iterator<MessageTime> itr = sip.messagelist.iterator();
		while(itr.hasNext())
		{	MessageTime mt = itr.next();
			mt.updateTime();
			if(mt.ts<=0)
			{
				Log.e("TIMED OUT", mt.message+"/"+mt.ts);
				itr.remove();
				sip.notifyTimeout(mt);
				//sip.messagelist.remove(new MessageTime(mt.tag,""));
			}
			Log.e("TIME", mt.message+"/"+mt.ts);
		}
		
		
		
		
		
	}

}
