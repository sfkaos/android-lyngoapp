package com.winraguini.lyngoapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.parse.ParseUser;
import com.winraguini.lyngoapp.proxies.ParseProxyObject;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ProfileActivity extends Activity {
	private ParseProxyObject userObject = null;
	private ParseProxyObject userProfile = null;
	private JSONObject fbProfile = null;
	
	private ProfilePictureView userProfilePictureView;
	private TextView tvProfileName;
	private TextView tvProfileLocation;
	private View vIsOnlineIndicator;
	private TextView tvIsOnlineText;
	private String chatParticipantID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		setupView();
		setuserObject();		
		displayProfileInfo();
	}
	
	private void setupView() {
		userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
		tvProfileName = (TextView) findViewById(R.id.tvProfileUserName);
		tvProfileLocation = (TextView) findViewById(R.id.tvProfileLocation);
		vIsOnlineIndicator = (View) findViewById(R.id.vIsOnlineIndicator);
		tvIsOnlineText = (TextView) findViewById(R.id.tvIsOnlineText);
	}
	
	private void setuserObject() {
		Intent intent = getIntent();
		chatParticipantID = intent.getStringExtra("chatParticipantID");
		userObject = (ParseProxyObject) intent.getSerializableExtra("chatParticipant");
		userProfile = userObject.getParseObject("userProfile");
		fbProfile = userObject.getJSONObject("fbProfile");
		
		Log.d("DEBUG", "fbProfile is " + fbProfile);
	}
	
	private void displayProfileInfo() {
		if (fbProfile != null) {												
			try {
				if (fbProfile.getString("facebookId") != null) {					
					userProfilePictureView.setProfileId(fbProfile.get("facebookId").toString());
				} else {
					// Show the default, blank user profile picture
					userProfilePictureView.setProfileId(null);
				}											
			} catch (JSONException e) {
				Log.d(LyngoApplication.TAG,
						"Error parsing saved user data.");
			}
			tvProfileName.setText(userProfile.getString("name"));
			tvProfileLocation.setText(userProfile.getString("location"));	
			if (!userObject.getBoolean("isOnline")) {
				Drawable d = getResources().getDrawable(R.drawable.offline_indicator);
				vIsOnlineIndicator.setBackground(d);		
				tvIsOnlineText.setText("offline");
				//vIsOnlineIndicator.setBackgroundColor(Color.parseColor("#666666"));				
			} else {
				Drawable d = getResources().getDrawable(R.drawable.online_indicator);
				vIsOnlineIndicator.setBackground(d);		
				tvIsOnlineText.setText("online");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}
	
	public void onChatBtnClick(View v) {
		Log.d("DEBUG", "Button clicked");				
		Intent intent = new Intent(this, ChatActivity.class);	
		intent.putExtra("chatParticipantID",chatParticipantID);
		startActivity(intent);
	}

}
