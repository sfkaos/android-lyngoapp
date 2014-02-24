package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
//import com.parse.integratingfacebooktutorial.R;

public class UserDetailsActivity extends Activity {

	private ProfilePictureView userProfilePictureView;
	private EditText userNameView;
	private EditText userLocationView;
	private TextView tvLearnLanguage;
	private TextView tvSpeakLanguage;

	private Button logoutButton;
	private ParseUser user;
	
	private String languageToLearn = null;
	private String languageISpeak = null;
	
	private ArrayList<String> languagesArray;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.userdetails);
		
		languagesArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.language_spinner_items)));

		userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
		userNameView = (EditText) findViewById(R.id.etName);
		userLocationView = (EditText) findViewById(R.id.etLocation );
		
		languageToLearn = (String) getIntent().getStringExtra("language_to_learn");
		languageISpeak = (String) getIntent().getStringExtra("language_i_speak");
		
		if (languageToLearn == null && languageISpeak == null) {
			
		}
		
		tvLearnLanguage = (TextView) findViewById(R.id.tvLearnLanguage);
		tvLearnLanguage.setText(languageToLearn);
		
		tvSpeakLanguage = (TextView) findViewById(R.id.tvSpeakLanguage);
		tvSpeakLanguage.setText(languageISpeak);
		
		logoutButton = (Button) findViewById(R.id.logoutButton);
		logoutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLogoutButtonClicked();
			}
		});
		
		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest();
		}
		
	}

	@Override
	public void onResume() {
		super.onResume();

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			// Check if the user is currently logged
			// and show any cached content
			updateViewsWithProfileInfo();
		} else {
			// If the user is not logged in, go to the
			// activity showing the login view.
			startLoginActivity();
		}
	}

	private void makeMeRequest() {
		Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (user != null) {
							// Create a JSON object to hold the profile info
							JSONObject userProfile = new JSONObject();
							
							try {
								// Populate the JSON object
								userProfile.put("facebookId", user.getId());
								
								userProfile.put("name", user.getName());
								if (user.getLocation().getProperty("name") != null) {
									userProfile.put("location", user
											.getLocation().getProperty("name"));
								}
								if (user.getProperty("gender") != null) {
									userProfile.put("gender",
											user.getProperty("gender"));
								}
								if (user.getBirthday() != null) {
									userProfile.put("birthday",
											user.getBirthday());
								}
								if (user.getProperty("relationship_status") != null) {
									userProfile
											.put("relationship_status",
													user
															.getProperty("relationship_status"));
								}
								
								
								//Don't forget to add bio
								//user.getProperty("bio")

								// Save the user profile info in a user property
								ParseUser currentUser = ParseUser.getCurrentUser();
								
								//Set user info
																
								if (user.getProperty("languages") != null) {									
									userProfile.put("languages", user.getProperty("languages"));									
								}
								
								currentUser.put("fbProfile", userProfile);
								
								currentUser.saveInBackground();
								
								// Show the user info
								updateViewsWithProfileInfo();
							} catch (JSONException e) {
								Log.d(LyngoApplication.TAG,
										"Error parsing returned user data.");
							}

						} else if (response.getError() != null) {
							if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
									|| (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
								Log.d(LyngoApplication.TAG,
										"The facebook session was invalidated.");
								onLogoutButtonClicked();
							} else {
								Log.d(LyngoApplication.TAG,
										"Some other error: "
												+ response.getError()
														.getErrorMessage());
							}
						}
					}
				});
		request.executeAsync();

	}

	private void updateViewsWithProfileInfo() {
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser.get("fbProfile") != null) {
									
			JSONObject userProfile = currentUser.getJSONObject("fbProfile");
			try {
				if (userProfile.getString("facebookId") != null) {
					String facebookId = userProfile.get("facebookId")
							.toString();
					userProfilePictureView.setProfileId(facebookId);
				} else {
					// Show the default, blank user profile picture
					userProfilePictureView.setProfileId(null);
				}
				if (userProfile.getString("name") != null) {
					userNameView.setText(userProfile.getString("name"));
				} else {
					userNameView.setText("");
				}
				if (userProfile.getString("location") != null) {
					userLocationView.setText(userProfile.getString("location"));
				} else {
					userLocationView.setText("");
				}
//				if (userProfile.getString("gender") != null) {
//					userGenderView.setText(userProfile.getString("gender"));
//				} else {
//					userGenderView.setText("");
//				}
//				if (userProfile.getString("birthday") != null) {
//					userDateOfBirthView.setText(userProfile
//							.getString("birthday"));
//				} else {
//					userDateOfBirthView.setText("");
//				}
//				if (userProfile.getString("relationship_status") != null) {
//					userRelationshipView.setText(userProfile
//							.getString("relationship_status"));
//				} else {
//					userRelationshipView.setText("");
//				}
			} catch (JSONException e) {
				Log.d(LyngoApplication.TAG,
						"Error parsing saved user data.");
			}

		}
		
		if (currentUser.getParseObject("userProfile") != null) {
			currentUser.getParseObject("userProfile").fetchIfNeededInBackground(new GetCallback<ParseObject>() {

				@Override
				public void done(ParseObject userProfile, ParseException arg1) {
					// TODO Auto-generated method stub
					if (userProfile != null) {
						if (userProfile.getString("name") != null) {
							userNameView.setText(userProfile.getString("name"));
						} else {
							userNameView.setText("");
						}
						if (userProfile.getString("location") != null) {
							userLocationView.setText(userProfile.getString("location"));
						} else {
							userLocationView.setText("");
						}
						if (userProfile.getString("languageToLearn") != null) {
							tvLearnLanguage.setText(userProfile.getString("languageToLearn"));
						} else {
							tvLearnLanguage.setText("");
						}
						if (userProfile.getString("languageToLearn") != null) {
							tvSpeakLanguage.setText(userProfile.getString("languageISpeak"));
						} else {
							tvSpeakLanguage.setText("");
						}
					}
				}
			});
			
			
			
		}
	}

	private void onLogoutButtonClicked() {
		// Log the user out
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	
	}
	
	private void saveNewProfile() {
		ParseUser currentUser = ParseUser.getCurrentUser();

		
	}
	
	public void onSave(View v) {
		// Save the user profile info in a user property
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		if (currentUser.getParseObject("userProfile") == null){
			
			ParseQuery<ParseObject> query = ParseQuery.getQuery("UserProfile");
			query.whereEqualTo("user", currentUser);
			 
			query.findInBackground(new FindCallback<ParseObject>() {
			  public void done(List<ParseObject> userProfiles, ParseException e) {
			    // commentList now has the comments for myPost
				  for (ParseObject userProfile : userProfiles) {
					  Log.d("DEBUG", "userProfile " + userProfile.getString("name"));
				  }
				  if (userProfiles.isEmpty()) {
					  ParseUser currentUser = ParseUser.getCurrentUser();
					  
					  ParseObject newProfile = new ParseObject("UserProfile");
					  newProfile.put("name", userNameView.getText().toString());
					  newProfile.put("location", userLocationView.getText().toString());			
						//Add bio in here
					  newProfile.put("languageToLearn", tvLearnLanguage.getText().toString());
					  newProfile.put("languageISpeak", tvSpeakLanguage.getText().toString());				
					  currentUser.put("userProfile", newProfile);										
					  currentUser.saveInBackground();
				  }
			  }
			});
		} else {
			ParseObject profile = currentUser.getParseObject("userProfile");
			profile.put("name", userNameView.getText().toString());
			profile.put("location", userLocationView.getText().toString());			
				//Add bio in here
			profile.put("languageToLearn", tvLearnLanguage.getText().toString());
			profile.put("languageISpeak", tvSpeakLanguage.getText().toString());	
			profile.saveInBackground();
		}
		
		Log.d("DEBUG", "Saving now.");
		
		//Now go to list view
		Intent intent = new Intent(this, LobbyActivity.class);
		startActivity(intent);
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
