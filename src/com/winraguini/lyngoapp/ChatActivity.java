package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class ChatActivity extends Activity {
	private String chatPartnerID = null;
	private String chatIDString = null;
	private ArrayList<ParseObject> chatMessages = null;
	private ChatAdapter adapter = null;
	private ListView lvChats = null;
	private EditText etChatMessage = null;
	private ParseObject chat = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		etChatMessage = (EditText) findViewById(R.id.etChatMessage);
		lvChats = (ListView) findViewById(R.id.lvChatMessages);
		chatMessages = new ArrayList<ParseObject>();
		adapter = new ChatAdapter(this, chatMessages);
		lvChats.setAdapter(adapter);
				
		populateChat();
	}
	
	public void populateChat() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		String currentUserObjectID = currentUser.getObjectId().toString();
		
		ArrayList<String> objectIDList = new ArrayList<String>();
		objectIDList.add(currentUserObjectID);
		objectIDList.add(getIntent().getStringExtra("chatPartnerID"));
		IgnoreCaseComparator icc = new IgnoreCaseComparator();

		java.util.Collections.sort(objectIDList,icc);
		
		chatIDString = objectIDList.get(0) + "-" + objectIDList.get(1);		
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chat");
		query.whereEqualTo("chatID", chatIDString);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
		    public void done(List<ParseObject> chatList, ParseException e) {
		        if (e == null) {
		        	if (chatList.size() > 0) {
		        		ParseObject chat = chatList.get(0);	
		        		setChat(chat);
		        		getChatMessages(chat);
		        	} else {
		        		//Create a new chat
		        		ParseObject newChat = new ParseObject("Chat");
		        		newChat.put("chatID", getChatIDString());
		        		newChat.saveInBackground();
		        		setChat(newChat);
		        	}
		            Log.d("score", "Retrieved " + chatList.size() + " chat");
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
		    }
		});
	}
	
	public void getChatMessages(ParseObject chat) {
		ParseQuery<ParseObject> chatQuery = ParseQuery.getQuery("ChatMessage");
		chatQuery.whereEqualTo("chattedOn", chat);		        				        	
		chatQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> chatMessages, ParseException e) {
				// TODO Auto-generated method stub
				if (e != null) {
					//If there is an error
					Log.d("DEBUG", e.getStackTrace().toString());
				} else {
					//No error
					if (chatMessages.size() > 0) {
						for (int i = 0; i < chatMessages.size(); i++) {
				    		ParseObject chatMessage = chatMessages.get(i);
				    		Log.d("DEBUG", "message " + chatMessage.getString("message"));
				    		adapter.add(chatMessages.get(i));
				    	}
					} else {
						Log.d("DEBUG", "NO chats!!!");
					}
				}
			}		        		    
		});
	}
	
	public String getChatIDString() {
		return chatIDString;
	}
	
	public void setChat(ParseObject chatObj) {
		chat = chatObj;
	}
	
	public ParseObject getChat() {
		return chat;
	}
	
	public void onSendClicked(View v) {
		if (etChatMessage.getText().length() > 0) {
			//ParseObject chatMessage
			Log.d("DEBUG", "chatting with text " + etChatMessage.getText().toString());
			ParseUser currentUser = ParseUser.getCurrentUser();
			
			ParseObject chatMessage = new ParseObject("ChatMessage");
			chatMessage.put("message", etChatMessage.getText().toString());
			chatMessage.put("chatParticipantID", currentUser.getObjectId().toString());
			chatMessage.put("chattedOn", getChat());									
			chatMessage.saveInBackground();			
			etChatMessage.setText("");
			
			updateChat(chatMessage);						
		} else {
			Log.d("DEBUG", "No Text");
		}
	}
	
	public void updateChat(ParseObject chatMessage) {
		//If the other user is online publish to PubNub
		
		adapter.add(chatMessage);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

}
