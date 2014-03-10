package com.winraguini.lyngoapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
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
	private TextView tvLanguages;
	private TextView tvAbout;
	
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
		tvLanguages = (TextView) findViewById(R.id.tvLanguages);
		tvAbout = (TextView) findViewById(R.id.tvAbout);
	}

	private void setuserObject() {
		Intent intent = getIntent();
		chatParticipantID = intent.getStringExtra("chatParticipantID");
		userObject = (ParseProxyObject) intent
				.getSerializableExtra("chatParticipant");
		userProfile = userObject.getParseObject("userProfile");
		fbProfile = userObject.getJSONObject("fbProfile");

		Log.d("DEBUG", "fbProfile is " + fbProfile);
	}

	private void displayProfileInfo() {
		if (fbProfile != null) {
			try {
				if (fbProfile.getString("facebookId") != null) {
					userProfilePictureView.setProfileId(fbProfile.get(
							"facebookId").toString());
				} else {
					// Show the default, blank user profile picture
					userProfilePictureView.setProfileId(null);
				}
			} catch (JSONException e) {
				Log.d(LyngoApplication.TAG, "Error parsing saved user data.");
			}
		}
		if (userProfile.getString("name") != null) {
			tvProfileName.setText(userProfile.getString("name"));
		}
		if (userProfile.getString("location") != null) {
			tvProfileLocation.setText(userProfile.getString("location"));
		}
		if (!userObject.getBoolean("isOnline")) {
			Drawable d = getResources().getDrawable(
					R.drawable.offline_indicator);
			vIsOnlineIndicator.setBackground(d);
			tvIsOnlineText.setText("offline");
			tvIsOnlineText.setTextColor(getResources().getColor(
					R.color.light_gray_text_color));
			// vIsOnlineIndicator.setBackgroundColor(Color.parseColor("#666666"));
		} else {
			Drawable d = getResources()
					.getDrawable(R.drawable.online_indicator);
			vIsOnlineIndicator.setBackground(d);
			tvIsOnlineText.setText("online");
			tvIsOnlineText.setTextColor(getResources().getColor(
					R.color.lyngo_primary_color));
		}

		String languageISpeak = "";
		String languageToLearn = "";
		if (userObject.getString("languageISpeak") != null) {
			languageISpeak = userObject.getString("languageISpeak");
		}
		if (userObject.getString("languageToLearn") != null) {
			languageToLearn = userObject.getString("languageToLearn");
		}
		String formattedLanguageString = "";
		if (languageISpeak.length() > 0 && languageToLearn.length() > 0) {
			formattedLanguageString = "<font color='#666666'>I speak</font> " + "<font color='#3ACE74'>"
					+ languageISpeak
					+ "</font><font color='#666666'>. I want to learn </font><font color='#3ACE74'>"
					+ languageToLearn + "<font color='#666666'>.</font>";
			tvLanguages.setText(Html.fromHtml(formattedLanguageString));
		}
		if (userProfile.getString("about") != null) {
			tvAbout.setText(userProfile.getString("about"));
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
		Log.d("DEBUG", "chatParticipantID is " + chatParticipantID);
		intent.putExtra("chatParticipantID", chatParticipantID);
		startActivity(intent);
	}

}
