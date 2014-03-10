package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.ConferenceManager.SessionListener;
import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.Settings.UserSettings;
import com.ooVoo.oovoosample.VideoCall.VideoCallActivity;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.Utils.LogSdk;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.winraguini.lyngoapp.models.ChatMessage;

public class ChatActivity extends Activity implements SessionListener{
	private ConferenceManager mConferenceManager = null;
	private String chatParticipantID = null;
	private String chatIDString = null;
	private ArrayList<ChatMessage> chatMessages = null;
	private ChatMessageAdapter adapter = null;
	private ListView lvChats = null;
	EditText etChatMessage = null;
	private ParseObject chat = null;
	private ParseUser currentUser = null;
	ParseUser chatPartner;
	ParseObject currentUserProfile = null;
	ParseObject chatMessage = null;
	ParseObject chatPartnerProfile = null;
	private JSONObject fbProfile = null;
	private Pubnub pubnub = null;
	private ProfilePictureView userProfilePictureView;
	private TextView tvChatPartnerName;
	private Button btnSend = null;
	private ProgressDialog mWaitingDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initConferenceManager();
		
		tvChatPartnerName = (TextView) findViewById(R.id.tvChatPartnerName);
		userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
		etChatMessage = (EditText) findViewById(R.id.etChatMessage);
		lvChats = (ListView) findViewById(R.id.lvChatMessages);
		btnSend = (Button)findViewById(R.id.btnSend);
		btnSend.setEnabled(false);
		
		currentUser = ParseUser.getCurrentUser();
		currentUser.put("isOnline", true);
		currentUser.saveInBackground();
		currentUser.getParseObject("userProfile").fetchIfNeededInBackground(
				new GetCallback<ParseObject>() {
					@Override
					public void done(ParseObject userProfile, ParseException e) {
						// TODO Auto-generated method stub
						Log.d("DEBUG", "Huh?");
						if (e == null) {
							currentUserProfile = userProfile;
							getPartnerInfo();
						} else {
							Log.d("DEBUG",
									"There was an error retrieving your profile");
						}
					}
				});

		chatMessages = new ArrayList<ChatMessage>();
		adapter = new ChatMessageAdapter(this, chatMessages);
		adapter.setCurrentChatParticipantID(currentUser.getObjectId());
		lvChats.setAdapter(adapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat, menu);
		return true;
	}

	
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		  switch (item.getItemId()) {
		    case R.id.video_chat:
		    	startVideoChatActivity();
		      	return true;
		    default:		    	
		    	return super.onOptionsItemSelected(item);		    		   
		  }		  
	}
	
	public void onSaveInstanceState(Bundle savedState) {
		super.onSaveInstanceState(savedState);
		Log.d("DEBUG", "onSaveInstanceState");
	}
	
	private void initConferenceManager() {
		LogSdk.setLogLevel(Log.INFO);
		LogSdk.i("DEBUG", "Init ConferenceManager");
		mConferenceManager = ConferenceManager.getInstance(this);
		
		mConferenceManager.initConference();
	}
	
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			}
		} catch (Exception e) {
			LogSdk.d(Utils.getOoVooTag(),
					"An exception while trying to find internet connectivity: "
							+ e.getMessage());
			// probably connectivity problem so we will return false
		}
		return false;
	}
	
	private void onJoinSession(){
		if (!isOnline()) {
			Utils.ShowMessageBox(this, "No Internet",
					"There is no internet connectivity, Turn WIFI on and try again");
			return;
		}
		saveSettings();		
		// Join session
		//mJoinButton.setEnabled(false);
		showWaitingMessage();
		mConferenceManager.joinSession();		
	}
	
	private void saveSettings() {
		UserSettings settingsToPersist = mConferenceManager.retrieveSettings();
		settingsToPersist.AppId = "12349983350832";//mAppIdView.getText().toString();
		settingsToPersist.AppToken = "MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI2PJJ2CkqxpP6U10%2BEUDCDBQq%2FpOLPRhaJMR2rocxtm5Q3K6gDj04s9mIZggOfcguwhefa0rTcoEnv%2BOb7nctZq%2BHJz8BRLMWz8Mxj6%2FUfeXl5raMIItFcNfiTqwOPNU%3D";//mTokenTextView.getText().toString();
		settingsToPersist.SessionID = chatIDString;
		settingsToPersist.UserID = android.os.Build.SERIAL;
		String displayName = "Partner";
		if (currentUserProfile.getString("name") != null) {
			displayName = currentUserProfile.getString("name"); 
		}
		settingsToPersist.DisplayName = displayName;

		// Save changes
		mConferenceManager.persistSettings(settingsToPersist);
	};
	
	
	@Override
	protected void onResume() {
		super.onResume();
		currentUser.put("isOnline", true);
		currentUser.saveInBackground();
		LogSdk.i("DEBUG", "onResume ->");
		mConferenceManager.addSessionListener(this);
		// Read settings
		UserSettings settings = mConferenceManager.retrieveSettings();
		try {
			// Fill views
//			mAppIdView.setText(settings.AppId);
//			mTokenTextView.setText(settings.AppToken);
//			mSessionIdView.setText(settings.SessionID);
//			mDisplayNameView.setText(settings.DisplayName);

			// reseting the resolution config
			settings.Resolution = CameraResolutionLevel.ResolutionMedium;
			LogSdk.i("DEBUG", "persistSettings ->");
			mConferenceManager.persistSettings(settings);

			LogSdk.i("DEBUG", "<- persistSettings");

			LogSdk.i("DEBUG", "loadDataFromSettings ->");
			mConferenceManager.loadDataFromSettings();
			LogSdk.i("DEBUG", "<- loadDataFromSettings");

		} catch (Exception e) {
			AlertsManager.getInstance().addAlert(
					"An Error occured while trying to select Devices");
		}
	}

	@Override
	public void onBackPressed() {
		if (mConferenceManager != null)
			mConferenceManager.leaveSession();
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// mModel.unregisterFromEvents();
		currentUser.put("isOnline", false);
		currentUser.saveInBackground();
		mConferenceManager.removeSessionListener(this);
		saveSettings();
	}

	private void switchToVideoCall() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//mJoinButton.setEnabled(true);
				hideWaitingMessage(); 
				startActivity(VideoCallActivity.class);
			}
		});
	}
	
	private void showWaitingMessage() {
		 mWaitingDialog = new ProgressDialog(this);
			mWaitingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mWaitingDialog.setMessage(getResources().getText(R.string.please_wait));
			mWaitingDialog.setIndeterminate(true);
			mWaitingDialog.setCancelable(false);
			mWaitingDialog.setCanceledOnTouchOutside(false);
			mWaitingDialog.show();
	}
	
	public void hideWaitingMessage() {
		try {
			if (mWaitingDialog != null) {
				mWaitingDialog.dismiss();
			}
			mWaitingDialog = null;
		} catch (Exception ex) {
		}
	}
	
	// Start a new activity using the requested effects
	private void startActivity(Class<?> activityToStart) {
		// Maybe should use this flag just for Video Call activity?
		Intent myIntent = new Intent(this, activityToStart);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(myIntent);
	}

	public void showErrorMessage(final String titleToShow,
			final String msgToShow) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//mJoinButton.setEnabled(true);
				hideWaitingMessage(); 
				Utils.ShowMessageBox(ChatActivity.this, titleToShow, msgToShow);
			}
		});
	}

	@Override
	public void onSessionError(ConferenceCoreError error) {
		String errorMsg = "An Error occured";
		showErrorMessage("Error", errorMsg);
	}

	@Override
	public void onSessionIDGenerated(String sSessionId) {
		LogSdk.d(Utils.getOoVooTag(), "OnSessionIdGenerated called with: "
				+ sSessionId);
	}

	@Override
	public void onJoinSessionSucceeded() {
		switchToVideoCall();
	}

	@Override
	public void onJoinSessionError(final ConferenceCoreError error) {
		LogSdk.e("DEBUG", "onJoinSessionError: " + error);
		
		switch (error) {
		case AlreadyInSession:
			showErrorMessage("Join Session", "Already in Session");
			break;
		case ConferenceIdNotValid:
			showErrorMessage("Join Session", "Conference id is not valid");
			break;
		case ClientIdNotValid:
			showErrorMessage("Join Session", "User id is not valid");
			break;
		case ServerAddressNotValid:
			showErrorMessage("Join Session", "Server address is not valid");
			break;
		case DuplicateClientId:
			showErrorMessage("Failed to join session", "Client already exist");
			break;
		case GroupQuotaExceeded:
			showErrorMessage("Failed to join session",
					"Group reached it's max size");
			break;
		case NotAuthorized:
			showErrorMessage("Join Session",
					"Error while trying to join session. Not Authorized!");
			break;
		case NotInitialized:
			showErrorMessage("Join Session",
					"Error while trying to join session. Not Initialized!");
			break;
		case NoAvs:
			showErrorMessage("Join Session",
					"Error while trying to join session. No AVS!");
			break;
		default:
			showErrorMessage("Join Session",
					"Error while trying to join session");
			break;
		}

	}

	@Override
	public void onJoinSessionWrongDataError() {
		showErrorMessage("Join Session", "Display Name should not be empty");
	}

	@Override
	public void onLeftSession(ConferenceCoreError error) {
		
	}
	

	private void getPartnerInfo() {
		setChatParticipantID(getIntent().getStringExtra("chatParticipantID"));
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

						fbProfile = chatPartner.getJSONObject("fbProfile");
						ParseObject profile = chatPartner
								.getParseObject("userProfile");
						setUpPartnerInfo(fbProfile, profile);
						setChatPartnerProfile(profile);
						populateChat();
						pubNub();
						btnSend.setEnabled(true);
					}
				} else {
					// Something went wrong.

				}
			}
		});
	}

	private void setUpPartnerInfo(JSONObject fbProfile, ParseObject profile) {
		if (fbProfile != null) {
			try {
				if (fbProfile.getString("facebookId") != null) {
					userProfilePictureView.setProfileId(fbProfile.get(
							"facebookId").toString());
				} else {
					// Show the default, blank user profile picture
					userProfilePictureView.setProfileId(null);
				}
			} catch (JSONException error) {
				Log.d(LyngoApplication.TAG, "Error parsing saved user data.");
			}
		}
		if (profile != null) {
			if (profile.getString("name") != null) {
				tvChatPartnerName.setText(profile.getString("name"));
			}
		}
	}

	@SuppressLint("HandlerLeak")
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			JSONObject msgObj = (JSONObject) msg.obj;
			int messageType = 0;
			String message = null;
			try {
				messageType = msgObj.getInt("messageType");
				message = msgObj.getString("message");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (messageType) {
			case 1:
				ChatMessage chatMessage = ChatMessage.fromJson(msgObj);
				chatMessages.add(chatMessage);
				adapter.notifyDataSetChanged();
				lvChats.setSelection(chatMessages.size() - 1);
				break;
			case 2:
				String chatParticipantID = null;
				String chatPartnerName = null;
				try {
					if (msgObj.getString("chatParticipantID") != null) {
						chatParticipantID = msgObj
								.getString("chatParticipantID");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (chatParticipantID.equalsIgnoreCase(getChatParticipantID())) {
					ParseObject chatPartnerProfile = getChatPartnerProfile();
					if (chatPartnerProfile.getString("name") != null) {
						chatPartnerName = chatPartnerProfile.getString("name")
								+ " has ";
					} else {
						chatPartnerName = "Someone";
					}
				} else {
					chatPartnerName = "You have ";
				}

				Toast.makeText(getBaseContext(),
						chatPartnerName + " " + message, Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				break;
			}

		}
	};

	public void pubNub() {
		pubnub = new Pubnub("pub-c-2af71fa0-01a7-4b0e-9e99-8e6d8f88774a",
				"sub-c-1cbac98e-9c27-11e3-9023-02ee2ddab7fe");

		try {
			pubnub.subscribe(chatIDString, new Callback() {
				@Override
				public void connectCallback(String channel, Object message) {
					Log.d("PUBNUB",
							"SUBSCRIBE : CONNECT on channel:" + channel + " : "
									+ message.getClass() + " : "
									+ message.toString());
				}

				@Override
				public void disconnectCallback(String channel, Object message) {
					Log.d("PUBNUB", "SUBSCRIBE : DISCONNECT on channel:"
							+ channel + " : " + message.getClass() + " : "
							+ message.toString());
				}

				public void reconnectCallback(String channel, Object message) {
					Log.d("PUBNUB", "SUBSCRIBE : RECONNECT on channel:"
							+ channel + " : " + message.getClass() + " : "
							+ message.toString());
				}

				@Override
				public void successCallback(String channel, Object message) {
					Log.d("PUBNUB",
							"SUBSCRIBE : " + channel + " : "
									+ message.getClass() + " : "
									+ message.toString() + ": got it!");
					JSONObject jsonMessage = (JSONObject) message;
					Message msg = handler.obtainMessage();
					try {
						msg.what = jsonMessage.getInt("messageType");
						msg.obj = jsonMessage;
						msg.arg1 = 0;

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					handler.sendMessage(msg);

				}

				@Override
				public void errorCallback(String channel, PubnubError error) {
					Log.d("PUBNUB", "SUBSCRIBE : ERROR on channel " + channel
							+ " : " + error.toString());
				}
			});
		} catch (PubnubException e) {
			Log.d("PUBNUB", e.toString());
		}
	}

	public void setChatPartner(ParseUser user) {
		chatPartner = user;
	}

	public ParseUser getChatPartner() {
		return chatPartner;
	}

	public void setChatPartnerProfile(ParseObject profile) {
		chatPartnerProfile = profile;
	}

	public ParseObject getChatPartnerProfile() {
		return chatPartnerProfile;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		currentUser.put("isOnline", true);
		currentUser.saveInBackground();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		currentUser.put("isOnline", false);
		currentUser.saveInBackground();
		publishLeaveMessageToChannel();
	}

	public void populateChat() {
		String currentUserObjectID = currentUser.getObjectId().toString();

		ArrayList<String> objectIDList = new ArrayList<String>();
		objectIDList.add(currentUserObjectID);
		objectIDList.add(chatParticipantID);
		IgnoreCaseComparator icc = new IgnoreCaseComparator();

		java.util.Collections.sort(objectIDList, icc);

		chatIDString = objectIDList.get(0) + "-" + objectIDList.get(1);
		Log.d("DEBUG", "chatID is" + chatIDString);
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Chatter");
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
						// Create a new chat

						ParseObject newChat = new ParseObject("Chatter");
						newChat.put("chatID", chatIDString);
						Log.d("DEBUG", "chatIDString is " + chatIDString);
						newChat.put("partner1", ParseUser.createWithoutData(ParseUser.class, currentUser.getObjectId()));
						newChat.put("partner2", ParseUser.createWithoutData(ParseUser.class, getChatPartner().getObjectId()));
						newChat.saveInBackground(new SaveCallback() {							
							@Override
							public void done(ParseException e) {
								// TODO Auto-generated method stub
								if (e == null) {
									Log.d("DEBUG", "Chat was saved");
								} else {
									Log.d("DEBUG", "Chat was nooooooooot saved BECAUSE ");
									e.printStackTrace();
								}
							}
						});						
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
					// If there is an error
					Log.d("DEBUG", e.getStackTrace().toString());
				} else {
					// No error
					if (chatMessages.size() > 0) {
						for (int i = 0; i < chatMessages.size(); i++) {
							ParseObject chatMessage = chatMessages.get(i);
							Log.d("DEBUG",
									"message "
											+ chatMessage.getString("message"));
							adapter.add(ChatMessage
									.fromParseObject(chatMessages.get(i)));
						}
						lvChats.setSelection(chatMessages.size() - 1);
					} else {
						Log.d("DEBUG", "NO chats!!!");
					}
				}
			}
		});
	}

	public String getChatParticipantID() {
		return chatParticipantID;
	}

	public void setChatParticipantID(String chatPartnerID) {
		chatParticipantID = chatPartnerID;
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
			btnSend.setEnabled(false);
			// ParseObject chatMessage
			Log.d("DEBUG", "chatting with text "
					+ etChatMessage.getText().toString());
			ParseUser currentUser = ParseUser.getCurrentUser();

			chatMessage = new ParseObject("ChatMessage");
			chatMessage.put("message", etChatMessage.getText().toString());
			chatMessage.put("chatParticipantID", currentUser.getObjectId()
					.toString());			
			chatMessage.put("chattedOn", getChat());
			chatMessage.saveInBackground();

			if (currentUserProfile.getString("name") != null) {
				getChat()
						.put("chattedBy", currentUserProfile.getString("name"));
				getChat().put("chattedByID", currentUser.getObjectId());
			}
			getChat().put("partner1ID", currentUser.getObjectId());
			getChat().put("partner2ID", getChatParticipantID());
			getChat().put("lastMessage", etChatMessage.getText().toString());
			getChat().put("lastMessageTime", System.currentTimeMillis());
			getChat().saveEventually();

			etChatMessage.setText("");
			updateChat();
			btnSend.setEnabled(true);
		} else {
			Log.d("DEBUG", "No Text");
		}
	}

	public void addChatMessageToView() {
		Log.d("DEBUG", "Pushed note to " + chatParticipantID);
		ParsePush push = new ParsePush();
		push.setChannel("lyngo-channel-"+ getChatParticipantID());
		Log.d("DEBUG", "Pushing message to " + "lyngo-channel-"+ getChatParticipantID());
		// push.setData(data);
		String currentUserName = "Someone";
		if (currentUserProfile.getString("name") != null) {
			currentUserName = currentUserProfile.getString("name");
		}
		push.setMessage(currentUserName + " wants to practice " + currentUser.getString("languageToLearn") + " with you.");
		push.sendInBackground();
		adapter.add(ChatMessage.fromParseObject(chatMessage));
	}
	
	public void publishStatusMessageToChannel(String message) {
		// Publish chat message to channel
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
			jsonObject.put("messageType", 2);
			jsonObject.put("message", message);
			jsonObject.put("chatParticipantID", currentUser.getObjectId());
		} catch (JSONException e) {
					// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pubnub.publish(chatIDString, jsonObject, callback);		
	}

	public void publishLeaveMessageToChannel() {
		publishStatusMessageToChannel("left the chat.");
	}
	
	public void publishJoinedVideoChatMessageToChannel() {
		publishStatusMessageToChannel("joined video chat.");
	}

	public void publishChatMessageToChannel() {
		// Publish chat message to channel
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
			jsonObject.put("messageType", 1);
			if (chatMessage.getString("message") != null) {
				jsonObject.put("message", chatMessage.getString("message"));
			}
			jsonObject.put("chatParticipantID", currentUser.getObjectId());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pubnub.publish(chatIDString, jsonObject, callback);
	}

	public void updateChat() {
		// If the other user is online publish to PubNub
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
					// There was an error
				}
			}
		});
	}
	
	private void startVideoChatActivity() {
		Log.d("DEBUG", "Start video chatting");		
		onJoinSession();
		publishJoinedVideoChatMessageToChannel();
	}

}
