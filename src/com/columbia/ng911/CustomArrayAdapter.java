package com.columbia.ng911;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		
		if(messagesList.get(position).isFrom911CallTaker){
			View view=inflater.inflate(R.layout.adapterunit911, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitText911);
//			textView.setBackgroundResource(R.drawable.backgroundgradientborder);
			textView.setBackgroundColor(Color.DKGRAY);
		}else{
			View view=inflater.inflate(R.layout.adapterunituser, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitTextUser);
//			textView.setBackgroundResource(R.drawable.background911);
			textView.setBackgroundColor(Color.LTGRAY);
		}
		textView.setText(messagesList.get(position).getMessage());
		return textView;
	}

	public void add(String object,boolean is911CallTaker) {
		// TODO Auto-generated method stub
		messagesList.add(new MessageItems(object,is911CallTaker));
		super.add(object);
	}
	
	class MessageItems{
		private String message;
		private boolean isFrom911CallTaker;
		public MessageItems(String message, boolean flag){
			this.message=message;
			isFrom911CallTaker=flag;
		}
		public String getMessage() {
			return message;
		}
		public boolean isFrom911CallTaker() {
			return isFrom911CallTaker;
		}
		
	}

	

}