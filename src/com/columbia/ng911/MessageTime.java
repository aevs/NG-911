package com.columbia.ng911;


public class MessageTime {

	public String tag;
	public String message;
	public int ts;
	
	public MessageTime(String id, String message)
	{
		this.tag = id;
		this.message = message;
		ts = 4000;
	}
	
	public void updateTime()
	{
		ts -= 400;
	}
	
	
	public boolean equals(Object o)
	{
		MessageTime mt = (MessageTime) o;
		if(mt.tag.equals(this.tag))
			return true;
		else
			return false;
	}

}
