package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
//import com.parse.integratingfacebooktutorial.R;


public class LoginActivity extends SherlockFragmentActivity {

	private Button loginButton;
	private Dialog progressDialog;
	private Spinner spLearnLanguage; 
	private Spinner spSpeakLanguage;
	private String languageToLearn = null;
	private String languageIspeak = null;
	
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
		
		
		spSpeakLanguage = (Spinner) findViewById(R.id.spKnowLanguages);
		spSpeakLanguage.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {				
				String languageValue = (String) parentView.getItemAtPosition(position);								
				if (position > 0) {
					languageIspeak = languageValue;					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		spLearnLanguage = (Spinner) findViewById(R.id.spLanguages);
		spLearnLanguage.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {				
				String languageValue = (String) parentView.getItemAtPosition(position);								
				if (position > 0) {
					languageToLearn = languageValue;					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
//		//Hide the action bar
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

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	private void onLoginButtonClicked() {
		if (languageToLearn != null && languageIspeak != null) {
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
		} else {
			//Add a good error 
			Toast.makeText(this, "Please update your languages.", Toast.LENGTH_SHORT).show();			
		}
		
	}

	private void showUserDetailsActivity() {
		Intent intent = new Intent(this, UserDetailsActivity.class);		
		intent.putExtra("language_to_learn", languageToLearn);
		intent.putExtra("language_i_speak", languageIspeak);
		startActivity(intent);
	}
}
