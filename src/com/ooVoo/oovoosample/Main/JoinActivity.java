//
// MainActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Main;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.ConferenceManager.SessionListener;
import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.Settings.SettingsActivity;
import com.ooVoo.oovoosample.Settings.UserSettings;
import com.ooVoo.oovoosample.VideoCall.VideoCallActivity;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.Utils.LogSdk;
import com.pubnub.api.*;
import org.json.*;

import com.winraguini.lyngoapp.R;
import com.winraguini.lyngoapp.UserDetailsActivity;

// Main presenter entity
public class JoinActivity extends Activity implements OnClickListener,
		SessionListener {

	private static final String TAG = JoinActivity.class.getName();
	private ConferenceManager mConferenceManager = null;
	private EditText mAppIdView = null;
	private TextView mTokenTextView = null;
	private EditText mSessionIdView = null;
	private TextView mDisplayNameView = null;
	private Button mJoinButton = null;
	private ProgressDialog mWaitingDialog = null;
	private Pubnub pubnub = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		initView();
		initConferenceManager();
		
		pubNub();
	}
	
	public void pubNub() {
	 pubnub = new Pubnub("pub-c-2af71fa0-01a7-4b0e-9e99-8e6d8f88774a", "sub-c-1cbac98e-9c27-11e3-9023-02ee2ddab7fe");
		
		try {
			  pubnub.subscribe("demo", new Callback() {

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
			                     + message.getClass() + " : " + message.toString());
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
 

	protected void initView() {
		LogSdk.i(TAG, "Setup views ->");
		// Set layout
		setContentView(R.layout.main);
		// Register for button press
		Object obj = findViewById(R.id.joinButton1);
		mJoinButton = (Button) obj;
		mJoinButton.setOnClickListener(this);

		// Retrieve and display SDK version
		mAppIdView = (EditText) findViewById(R.id.appIdText);
		mTokenTextView = (TextView) findViewById(R.id.tokenText);
		mSessionIdView = (EditText) findViewById(R.id.sessionIdText);
		mDisplayNameView = (TextView) findViewById(R.id.displayNameText);
				
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setIcon(R.drawable.ic_main);
		}		
		LogSdk.i(TAG, "<- Setup views");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null)
			return false;

		switch (item.getItemId()) {			
			case R.id.profile_settings:
				startActivity(UserDetailsActivity.class);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initConferenceManager() {
		LogSdk.setLogLevel(Log.INFO);
		LogSdk.i(TAG, "Init ConferenceManager");
		mConferenceManager = ConferenceManager.getInstance(this);
		
		mConferenceManager.initConference();
	}

	public String getAppVersion() {
		String versionName = new String();
		try {
			versionName = this.getPackageManager().getPackageInfo(
					this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			LogSdk.e(TAG, "", e);
		}
		return versionName;
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

	@Override
	public void onClick(View v) {	
		if(v == null)
			return;
		switch(v.getId()){
			case R.id.joinButton1:
				onJoinSession();
				break;
		}
		
	}
	
	private void onJoinSession(){
		Callback callback = new Callback() {
			  public void successCallback(String channel, Object response) {
			    Log.d("PUBNUB", "success" + response.toString());
			  }
			  public void errorCallback(String channel, PubnubError error) {
				Log.d("PUBNUB", "error" + error.toString());
			  }
			};
		pubnub.publish("demo", "Hello World !!" , callback);
		
		
		if (!isOnline()) {
			Utils.ShowMessageBox(this, "No Internet",
					"There is no internet connectivity, Turn WIFI on and try again");
			return;
		}
		saveSettings();		
		// Join session
		mJoinButton.setEnabled(false);
		showWaitingMessage();
		mConferenceManager.joinSession();		
	}

//	@Override
//	protected Class<?> getLeftActivity() {
//		return SettingsActivity.class;
//	}

	@Override
	protected void onResume() {
		super.onResume();
		LogSdk.i(TAG, "onResume ->");
		mConferenceManager.addSessionListener(this);
		// Read settings
		UserSettings settings = mConferenceManager.retrieveSettings();
		try {
			// Fill views
			mAppIdView.setText(settings.AppId);
			mTokenTextView.setText(settings.AppToken);
			mSessionIdView.setText(settings.SessionID);
			mDisplayNameView.setText(settings.DisplayName);

			// reseting the resolution config
			settings.Resolution = CameraResolutionLevel.ResolutionMedium;
			LogSdk.i(TAG, "persistSettings ->");
			mConferenceManager.persistSettings(settings);

			LogSdk.i(TAG, "<- persistSettings");

			LogSdk.i(TAG, "loadDataFromSettings ->");
			mConferenceManager.loadDataFromSettings();
			LogSdk.i(TAG, "<- loadDataFromSettings");

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
		mConferenceManager.removeSessionListener(this);
		saveSettings();
	}

	private void saveSettings() {
		UserSettings settingsToPersist = mConferenceManager.retrieveSettings();
		settingsToPersist.AppId = "12349983350832";//mAppIdView.getText().toString();
		settingsToPersist.AppToken = "MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAI2PJJ2CkqxpP6U10%2BEUDCDBQq%2FpOLPRhaJMR2rocxtm5Q3K6gDj04s9mIZggOfcguwhefa0rTcoEnv%2BOb7nctZq%2BHJz8BRLMWz8Mxj6%2FUfeXl5raMIItFcNfiTqwOPNU%3D";//mTokenTextView.getText().toString();
		settingsToPersist.SessionID = mSessionIdView.getText().toString();
		settingsToPersist.UserID = android.os.Build.SERIAL;
		settingsToPersist.DisplayName = "sfkaos";//mDisplayNameView.getText().toString();

		// Save changes
		mConferenceManager.persistSettings(settingsToPersist);
	};

	private void switchToVideoCall() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mJoinButton.setEnabled(true);
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
				mJoinButton.setEnabled(true);
				hideWaitingMessage(); 
				Utils.ShowMessageBox(JoinActivity.this, titleToShow, msgToShow);
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
		LogSdk.e(TAG, "onJoinSessionError: " + error);
		
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

}
