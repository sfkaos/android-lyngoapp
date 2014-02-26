package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.winraguini.lyngoapp.models.ChatMessage;

public class ChatActivity extends Activity {
	private String chatParticipantID = null;
	private String chatIDString = null;
	private ArrayList<ChatMessage> chatMessages = null;
	private ChatAdapter adapter = null;
	private ListView lvChats = null;
	EditText etChatMessage = null;
	private ParseObject chat = null;
	private ParseUser currentUser = null;
	ParseUser chatPartner;
	ParseObject chatMessage = null;
	ParseObject chatPartnerProfile = null;
	private Pubnub pubnub = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		chatParticipantID = getIntent().getStringExtra("chatParticipantID");
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.whereEqualTo("objectId", chatParticipantID);
		query.include("userProfile");
		query.findInBackground(new FindCallback<ParseUser>() {
		  public void done(List<ParseUser> objects, ParseException e) {
		    if (e == null) {
		        // The query was successful.
		    	if (objects.size() > 0) {
		    		ParseUser chatPartner = objects.get(0);
		    		setChatPartner(chatPartner);
		    		ParseObject profile = chatPartner.getParseObject("userProfile");
		    		setChatPartnerProfile(profile);		    	      
		    	}
		    } else {
		        // Something went wrong.
		    }
		  }
		});
		
		etChatMessage = (EditText) findViewById(R.id.etChatMessage);
		lvChats = (ListView) findViewById(R.id.lvChatMessages);		
		currentUser = ParseUser.getCurrentUser();
		currentUser.put("isOnline", true);
		currentUser.saveInBackground();		
		chatMessages = new ArrayList<ChatMessage>();
		adapter = new ChatAdapter(this, chatMessages);
		adapter.setCurrentChatParticipantID(currentUser.getObjectId());
		lvChats.setAdapter(adapter);
		populateChat();
		pubNub();
	}
	
	@SuppressLint("HandlerLeak")
	final Handler handler = new Handler(){
		  @Override
		  public void handleMessage(Message msg) {		    
		    super.handleMessage(msg);
		    JSONObject msgObj = (JSONObject) msg.obj;		    
		    ChatMessage chatMessage = ChatMessage.fromJson(msgObj);
			chatMessages.add(chatMessage);				
			adapter.notifyDataSetChanged();
			lvChats.setSelection(chatMessages.size() - 1);					    		    		    
		  }
	};
	
	public void pubNub() {
		pubnub = new Pubnub("pub-c-2af71fa0-01a7-4b0e-9e99-8e6d8f88774a", "sub-c-1cbac98e-9c27-11e3-9023-02ee2ddab7fe");
		
		try {
			  pubnub.subscribe(chatIDString, new Callback() {
			      @Override
			      public void connectCallback(String channel, Object message) {
			          Log.d("PUBNUB","SUBSCRIBE : CONNECT on channel:" + channel
			                     + " : " + message.getClass() + " : "
			                     + message.toString());
			      }

			      @Override
			      public void disconnectCallback(String channel, Object message) {
			          Log.d("PUBNUB","SUBSCRIBE : DISCONNECT on channel:" + channel
			                     + " : " + message.getClass() + " : "
			                     + message.toString());
			      }

			      public void reconnectCallback(String channel, Object message) {
			          Log.d("PUBNUB","SUBSCRIBE : RECONNECT on channel:" + channel
			                     + " : " + message.getClass() + " : "
			                     + message.toString());
			      }

			      @Override
			      public void successCallback(String channel, Object message) {
			          Log.d("PUBNUB","SUBSCRIBE : " + channel + " : "
			                     + message.getClass() + " : " + message.toString() +  ": got it!");
			          			          			          
			          Message msg = handler.obtainMessage();
			          msg.what = 1;
			          msg.obj = message;
			          msg.arg1 = 1;
			          handler.sendMessage(msg);			          
			      }

			      @Override
			      public void errorCallback(String channel, PubnubError error) {
			          Log.d("PUBNUB","SUBSCRIBE : ERROR on channel " + channel
			                     + " : " + error.toString());
			      }
			    }
			  );
			} catch (PubnubException e) {
			  Log.d("PUBNUB",e.toString());
			}
	}
	
	public void setChatPartner(ParseUser user) {
		chatPartner = user;
		Log.d("DEBUG", "chat partner is " + user.getUsername());		
	}
	
	public void setChatPartnerProfile(ParseObject profile) {
		chatPartnerProfile = profile;
		Log.d("DEBUG", "partner's name is " + chatPartnerProfile.get("name"));
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		currentUser.put("isOnline", false);
		currentUser.saveInBackground();
	}
	
	public void populateChat() {
		String currentUserObjectID = currentUser.getObjectId().toString();
		
		ArrayList<String> objectIDList = new ArrayList<String>();
		objectIDList.add(currentUserObjectID);
		objectIDList.add(chatParticipantID);
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
				    		adapter.add(ChatMessage.fromParseObject(chatMessages.get(i)));
				    	}
						lvChats.setSelection(chatMessages.size() - 1);
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
			
			chatMessage = new ParseObject("ChatMessage");
			chatMessage.put("message", etChatMessage.getText().toString());
			chatMessage.put("chatParticipantID", currentUser.getObjectId().toString());
			chatMessage.put("chattedOn", getChat());									
			chatMessage.saveInBackground();			
			etChatMessage.setText("");
			
			updateChat();						
		} else {
			Log.d("DEBUG", "No Text");
		}
	}
	
	public void addChatMessageToView() {
		adapter.add(ChatMessage.fromParseObject(chatMessage));
	}
	
	public void publishChatMessageToChannel() {
		//Publish chat message to channel
		Callback callback = new Callback() {
			  public void successCallback(String channel, Object response) {
			    Log.d("PUBNUB", "success" + response.toString());
			  }
			  public void errorCallback(String channel, PubnubError error) {
				Log.d("PUBNUB", "error" + error.toString());
			  }
		};
		
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("message", chatMessage.getString("message"));
			jsonObject.put("chatParticipantID", currentUser.getObjectId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		pubnub.publish(chatIDString, jsonObject , callback);
	}
	
	public void updateChat() {
		//If the other user is online publish to PubNub
		chatPartner.fetchInBackground(new GetCallback<ParseUser>() {

			@Override
			public void done(ParseUser chatPartner, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					if (chatPartner.getBoolean("isOnline")) {
						Log.d("DEBUG", "Partner is online just publish it");
						publishChatMessageToChannel();
					} else {
						Log.d("DEBUG", "Partner is offline update local screen");
						addChatMessageToView();
					}					
					lvChats.setSelection(chatMessages.size() - 1);
				} else {
					//There was an error
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

}
