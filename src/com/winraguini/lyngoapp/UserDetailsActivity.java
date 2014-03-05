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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


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
import com.parse.SaveCallback;
//import com.parse.integratingfacebooktutorial.R;
import com.parse.SaveCallback;

public class UserDetailsActivity extends Activity {

	private ProfilePictureView userProfilePictureView;
	private EditText etName;
	private EditText etLocation;
	private EditText etAbout;
	private Spinner spLearnLanguage;
	private Spinner spSpeakLanguage;

	private ParseUser user;
	private ParseUser currentUser;
	private String languageToLearn = null;
	private String languageISpeak = null;
	private Button findPartnerButton = null;
	
	private ArrayList<String> languagesArray;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userdetails);
		currentUser = ParseUser.getCurrentUser();
		languagesArray = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.language_spinner_items)));

		userProfilePictureView = (ProfilePictureView) findViewById(R.id.userProfilePicture);
		etName = (EditText) findViewById(R.id.etName);
		etLocation = (EditText) findViewById(R.id.etLocation );
		etAbout = (EditText) findViewById(R.id.etAbout);
		findPartnerButton = (Button) findViewById(R.id.btnSave);
		setupSpinners();
		loadSpinners();
				
	
		// Fetch Facebook user info if the session is active
		Session session = ParseFacebookUtils.getSession();
		if (session != null && session.isOpened()) {
			makeMeRequest();
		}
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.user_details, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		  switch (item.getItemId()) {
		    case R.id.logout:
		    	onLogoutButtonClicked();
		      return true;		    
		    default:
		      return super.onOptionsItemSelected(item);
		  }
	}
	

	public void setupSpinners() {
		spSpeakLanguage = (Spinner) findViewById(R.id.spISpeak);
		spSpeakLanguage.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {				
				String languageValue = (String) parentView.getItemAtPosition(position);				
				
				if (position > 0) {
					languageISpeak = languageValue;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		spLearnLanguage = (Spinner) findViewById(R.id.spIWantToLearn);
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
		
	}
	
	public void loadSpinners() {
		
		if (languageToLearn == null) {
			//Then get it from my profile
			if (currentUser != null && currentUser.getString("languageToLearn") != null && !currentUser.getString("languageToLearn").isEmpty()) {
				//Set language
				languageToLearn = currentUser.getString("languageToLearn"); 
			}
		}
						
		
		if (languageISpeak == null) {
			if (currentUser != null && currentUser.getString("languageISpeak") != null && !currentUser.getString("languageISpeak").isEmpty()) {
				//Set language
				languageISpeak = currentUser.getString("languageISpeak"); 
			}
		}
		
		if (getIntent().getStringExtra("language_to_learn") != null) {
			languageToLearn = getIntent().getStringExtra("language_to_learn");
		}
		
		if (getIntent().getStringExtra("language_i_speak") != null) {
			languageISpeak = (String) getIntent().getStringExtra("language_i_speak");
		}
						
		if (languageToLearn != null) {
			spLearnLanguage.setSelection(languagesArray.indexOf(languageToLearn));
		}
		
		if (languageISpeak != null) {
			spSpeakLanguage.setSelection(languagesArray.indexOf(languageISpeak));
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
								if (user.getLocation() != null && user.getLocation().getProperty("name") != null) {
									userProfile.put("location", user
											.getLocation().getProperty("name"));
								}
								if (user.getProperty("gender") != null) {
									userProfile.put("gender",
											user.getProperty("gender").toString());
								}
								if (user.getBirthday() != null) {
									userProfile.put("birthday",
											user.getBirthday());
								}
								if (user.getProperty("relationship_status") != null) {
									userProfile
											.put("relationship_status",
													user
															.getProperty("relationship_status").toString());
								}
								
								if (user.getProperty("bio") != null) {
									userProfile.put("bio",user.getProperty("bio").toString());
									
									Log.d("DEBUG", "Bio is " + user.getProperty("bio").toString());
								}
								
								
								//Don't forget to add bio
								//user.getProperty("bio")

								// Save the user profile info in a user property
								ParseUser currentUser = ParseUser.getCurrentUser();																								

								
								currentUser.put("fbProfile", userProfile);
								
								currentUser.saveInBackground();
								currentUser.saveInBackground(new SaveCallback() {									
									@Override
									public void done(ParseException arg0) {
										// Show the user info
										updateViewsWithProfileInfo();
									}
								});
								
								
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
		
		//Update the profile picture
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
						Log.d("DEBUG", "userProfile is not NULL man");
						if (userProfile.getString("name") != null) {
							Log.d("DEBUG", "Setting etName to" + userProfile.getString("name") );
							etName.setText(userProfile.getString("name"));
						} else {
							Log.d("DEBUG", "Setting etName to nothing" );
							etName.setText("");
						}
						if (userProfile.getString("location") != null) {
							etLocation.setText(userProfile.getString("location"));
						} else {
							etLocation.setText("");
						}
						if (userProfile.getString("about") != null) {
							etAbout.setText(userProfile.getString("about"));
						}
					} else {
						Log.d("DEBUG", "userProfile is null");
					}
				}
			});
								
		} else {
			
			if (currentUser.get("fbProfile") != null) {									
				JSONObject userProfile = currentUser.getJSONObject("fbProfile");
				try {
								
					if (userProfile.getString("name") != null) {
						etName.setText(userProfile.getString("name"));
					}										
					if (userProfile.getString("location") != null) {
						etLocation.setText(userProfile.getString("location"));
					} 					
					if (userProfile.getString("about") != null) {
						etAbout.setText(userProfile.getString("about"));
					}										
				} catch (JSONException e) {
					Log.d(LyngoApplication.TAG,
							"Error parsing saved user data.");
				}

			}
		}
	
	}

	private void onLogoutButtonClicked() {
		// Log the user out
        ParseFacebookUtils.getSession().closeAndClearTokenInformation();
		ParseUser.logOut();

		// Go to the login view
		startLoginActivity();
	
	}
	
	private void saveNewProfile() {
		ParseUser currentUser = ParseUser.getCurrentUser();

		
	}
	
	public void onSave(View v) {
		// Save the user profile info in a user property
				
		findPartnerButton.setEnabled(false);
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		if (currentUser.getParseObject("userProfile") == null){
			
			ParseQuery<ParseObject> query = ParseQuery.getQuery("UserProfile");
			query.whereEqualTo("user", currentUser);
			 
			query.findInBackground(new FindCallback<ParseObject>() {
			  public void done(List<ParseObject> userProfiles, ParseException e) {
			    // commentList now has the comments for myPost
				  for (ParseObject userProfile : userProfiles) {
					  if (userProfile.getString("name") != null) {
						  Log.d("DEBUG", "userProfile " + userProfile.getString("name"));  
					  }					  
				  }
				  if (userProfiles.isEmpty()) {
					  ParseUser currentUser = ParseUser.getCurrentUser();
					  
					  ParseObject newProfile = new ParseObject("UserProfile");
					  newProfile.put("name", etName.getText().toString());
					  newProfile.put("location", etLocation.getText().toString());
					  newProfile.put("about", etAbout.getText().toString());
					  currentUser.put("userProfile", newProfile);	
						//Add bio in here
					  currentUser.put("languageToLearn", languageToLearn);
					  currentUser.put("languageISpeak", languageISpeak);									  									
					  currentUser.saveInBackground(new SaveCallback() {
							
							@Override
							public void done(ParseException arg0) {
								// TODO Auto-generated method stub
								//Now go to list view
								showLobby();					
							}
						});
				  }
			  }
			});
		} else {
			ParseObject profile = currentUser.getParseObject("userProfile");
			profile.put("name", etName.getText().toString());
			profile.put("location", etLocation.getText().toString());	
			profile.put("about", etAbout.getText().toString());
			profile.saveInBackground();
				//Add bio in here
			currentUser.put("languageToLearn", languageToLearn);
			currentUser.put("languageISpeak", languageISpeak);
			currentUser.saveInBackground();
			currentUser.saveInBackground(new SaveCallback() {				
				@Override
				public void done(ParseException arg0) {
					// TODO Auto-generated method stub
					//Now go to list view
					showLobby();					
				}
			});
			
		}					
	}
	
	private void showLobby() {
		Intent intent = new Intent(this, ActionBarActivity.class);
		startActivity(intent);
	}

	private void startLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
