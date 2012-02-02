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
/**
 * Class to implement custom array adapter to implement color-coded bubble list.
 * @author acs
 *
 */
public class CustomArrayAdapter extends ArrayAdapter<String> {

	
	ArrayList<MessageItems> messagesList=new ArrayList<MessageItems>();
	Context context;
/**
 * Constructor to initialize the custom arrayAdapter.
 * @param context The context to which the adapter should be bound.
 * @param textViewResourceId The TextView resource id.
 */
	public CustomArrayAdapter(Context context, int textViewResourceId)
	{
		super(context, textViewResourceId);
		this.context=context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater=LayoutInflater.from(context);
		
		TextView textView;
		/**
		 * This if-else set is used to inflate the view depending on whether the message is from the user,
		 * call-taker or is an error message. This is how the bubble List is color coded.
		 * 
		 */
		if(messagesList.get(position).isErrorMessage){
			View view=inflater.inflate(R.layout.adapterunituser, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitTextUser);
			textView.setBackgroundResource(R.drawable.errormessagebubble);
		}
		else if(messagesList.get(position).isFrom911CallTaker){
			View view=inflater.inflate(R.layout.adapterunit911, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitText911);
			textView.setBackgroundResource(R.drawable.serverbubble);
		}else{
			View view=inflater.inflate(R.layout.adapterunituser, null);
			textView=(TextView) view.findViewById(R.id.adapterUnitTextUser);
			textView.setBackgroundResource(R.drawable.userbubble);
		}
		textView.setText(messagesList.get(position).getMessage());
		return textView;
	}
/**
 * Adds new list item to the bubble list. Color coded depending on whether the
 * message is from user or the call taker.
 * 
 * @param object Message sent/received.
 * @param is911CallTaker True if the message is from the call taker, false otherwise.
 */
	public void add(String object,boolean is911CallTaker) {
		// TODO Auto-generated method stub
		messagesList.add(new MessageItems(object,is911CallTaker,false));
		super.add(object);
	}
	/**
	 * Displays error message to the user in a red bubble.
	 * 
	 * @param object The error message to be displayed to the user.
	 */
	public void addErrorMessage(String object){
		
		messagesList.add(new MessageItems(object,false,true));
		super.add(object);
	}
	/**
	 * Custom message item class in order to be able to  implement color coded bubble List.
	 */
	class MessageItems{
		private String message;
		private boolean isFrom911CallTaker;
		private boolean isErrorMessage=false;
		/**
		 * 
		 * @param message The message to be pushed to the bubble list.
		 * @param flag True if message is from Call Taker, false otherwise.
		 * @param isErrorMessage True if message is an error message, false otherwise.
		 */
		public MessageItems(String message, boolean flag,boolean isErrorMessage){
			this.message=message;
			this.isErrorMessage=isErrorMessage;
			isFrom911CallTaker=flag;
		}
		/**
		 * Getter to return the message 
		 * @return The message
		 */
		public String getMessage() {
			return message;
		}
		/**
		 * Checks if the message if from call taker.
		 * @return true is message is from Call taker, false otherwise
		 */
		public boolean isFrom911CallTaker() {
			return isFrom911CallTaker;
		}
		/**
		 * Checks if message is error message.
		 * @return true if message type is error, false otherwise
		 */
		public boolean isErrorMessage(){
			return isErrorMessage;
		}
		
	}

	

}