package com.columbia.ng911;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomArrayAdapter extends ArrayAdapter<String> {

	
	ArrayList<MessageItems> messagesList=new ArrayList<MessageItems>();
	Context context;
	public CustomArrayAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
		this.context=context;
		// TODO Auto-generated constructor stub
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
//		LayoutInflater inflater=getLayoutInflater();
		LayoutInflater inflater=LayoutInflater.from(context);
		
		TextView textView;
		if(messagesList.get(position).isErrorMessage){
			View view=inflater.inflate(R.layout.adapterunituser, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitTextUser);
			textView.setBackgroundResource(R.drawable.errormessagebubble);
		}
		else if(messagesList.get(position).isFrom911CallTaker){
			View view=inflater.inflate(R.layout.adapterunit911, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitText911);
			textView.setBackgroundResource(R.drawable.serverbubble);
//			textView.setBackgroundColor(Color.DKGRAY);
		}else{
			View view=inflater.inflate(R.layout.adapterunituser, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitTextUser);
			textView.setBackgroundResource(R.drawable.userbubble);
//			textView.setBackgroundColor(Color.LTGRAY);
		}
		textView.setText(messagesList.get(position).getMessage());
		return textView;
	}

	public void add(String object,boolean is911CallTaker) {
		// TODO Auto-generated method stub
		messagesList.add(new MessageItems(object,is911CallTaker,false));
		super.add(object);
	}
	public void addErrorMessage(String object){
		
		messagesList.add(new MessageItems(object,false,true));
		super.add(object);
	}
	class MessageItems{
		private String message;
		private boolean isFrom911CallTaker;
		private boolean isErrorMessage=false;
		public MessageItems(String message, boolean flag,boolean isErrorMessage){
			this.message=message;
			this.isErrorMessage=isErrorMessage;
			isFrom911CallTaker=flag;
		}
		public String getMessage() {
			return message;
		}
		public boolean isFrom911CallTaker() {
			return isFrom911CallTaker;
		}
		public boolean isErrorMessage(){
			return isErrorMessage;
		}
		
	}

	

}