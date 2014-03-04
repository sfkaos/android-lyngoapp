package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.PushService;
//import com.parse.integratingfacebooktutorial.R;


public class LoginActivity extends SherlockFragmentActivity {
	private Button loginButton;
	private Dialog progressDialog;
	
	ArrayList<String> languagesArray = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.login);
		
		ParseAnalytics.trackAppOpened(getIntent());
		
		loginButton = (Button) findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginButtonClicked();
			}
		});
		//Hide the action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();
		
		// Check if there is a currently logged in user
		// and they are linked to a Facebook account.
		ParseUser currentUser = ParseUser.getCurrentUser();
		if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			// Go to the user info activity
			showUserDetailsActivity();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	private void onLoginButtonClicked() {
				LoginActivity.this.progressDialog = ProgressDialog.show(
					LoginActivity.this, "", "Logging in...", true);
			List<String> permissions = Arrays.asList("basic_info", "user_about_me",
					"user_relationships", "user_birthday", "user_location");
			ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
				@Override
				public void done(ParseUser user, ParseException err) {
					LoginActivity.this.progressDialog.dismiss();
					if (user == null) {
						Log.d(LyngoApplication.TAG,
								"Uh oh. The user cancelled the Facebook login.");
					} else if (user.isNew()) {
						Log.d(LyngoApplication.TAG,
								"User signed up and logged in through Facebook!");
						showUserDetailsActivity();
					} else {
						Log.d(LyngoApplication.TAG,
								"User logged in through Facebook!");
						showUserDetailsActivity();
					}
				}
			});

	}

	private void showUserDetailsActivity() {	
		Log.d("DEBUG", "Subscribing to " + ParseUser.getCurrentUser().getObjectId());
		PushService.subscribe(this, "tester", ActionBarActivity.class);
		Intent intent = new Intent(this, UserDetailsActivity.class);	
		startActivity(intent);
	}
}
