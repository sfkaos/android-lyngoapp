package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.winraguini.lyngoapp.proxies.ParseProxyObject;


public class LobbyFragment extends SherlockFragment {
	private LobbyAdapter adapter;
	private ListView lvUsers;
	private ArrayList<ParseUser> users;
	private ParseObject currentUserProfile = null;  
	private ParseUser currentUser = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		if (savedInstanceState == null) {
			Log.d("DEBUG","onCreate bundle IS null");
		}
		
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Defines the xml file for the fragment
		View view = inflater.inflate(R.layout.fragment_lobby, container, false);
		// Setup handles to view objects here				
		return view;
    }
	
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);	
		currentUser = ParseUser.getCurrentUser();
		currentUser.put("isOnline", false);
		currentUser.saveInBackground();
		lvUsers = (ListView)getActivity().findViewById(R.id.lvUsers);
		users = new ArrayList<ParseUser>();
		adapter = new LobbyAdapter(getActivity(), users);
		lvUsers.setAdapter(adapter);
		setUpUsers();
		
		if (currentUser != null) {
			if (currentUser.getParseObject("userProfile") != null) {
				currentUserProfile = currentUser.getParseObject("userProfile");
				getUsers();
			} else {
				currentUser.getParseObject("userProfile").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
					@Override
					public void done(ParseObject userProfile, ParseException e) {
						// TODO Auto-generated method stub
						Log.d("DEBUG", "Huh?");
						if (e == null) {
							currentUserProfile = userProfile;
							getUsers();
						} else {
							Log.d("DEBUG", "There was an error retrieving your profile");
						}
					}
				});
				
			}							
		} else {
			Log.d("DEBUG", "There is no current user.");
		}		
				
	}
	
	private void setUpUsers() {
		lvUsers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
				// TODO Auto-generated method stub
				ParseUser user = users.get(pos);
				ParseProxyObject userProxy = new ParseProxyObject(user);
				Intent intent = new Intent(getActivity(), ProfileActivity.class);	
				intent.putExtra("chatParticipant",userProxy);
				intent.putExtra("chatParticipantID", user.getObjectId());
				startActivity(intent);
			}
		});		
	}
	
	private void getUsers() {
		if (currentUser.getString("languageToLearn") != null && currentUser.getString("languageToLearn") != null) {
			ParseQuery<ParseUser> languageISpeakQuery = ParseUser.getQuery();		
			languageISpeakQuery.whereEqualTo("languageISpeak", currentUser.getString("languageToLearn"));
			
			ParseQuery<ParseUser> languageToLearnQuery = ParseUser.getQuery();
			languageToLearnQuery.whereEqualTo("languageToLearn", currentUser.getString("languageToLearn"));				
			 
			List<ParseQuery<ParseUser>> userQueries = new ArrayList<ParseQuery<ParseUser>>();
			userQueries.add(languageISpeakQuery);
			userQueries.add(languageToLearnQuery);
			 
			ParseQuery<ParseUser> mainQuery = ParseQuery.or(userQueries);		
			Log.d("DEBUG", "getting users");
			mainQuery.include("userProfile");
			mainQuery.findInBackground(new FindCallback<ParseUser>() {
			  public void done(List<ParseUser> results, ParseException e) {
				  if (e == null) {
					  for (ParseUser user : results) {
						  if (!user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
							  ParseObject userProfile = user.getParseObject("userProfile");
							  Log.d("DEBUG", userProfile.getString("name") + " matched with you.");
							  adapter.add(user);
						  }
					  }					    					       
				  } else {
					  Log.d("DEBUG", "There was an issue getting the right users" + e.getLocalizedMessage());
				  }
			    // results has the list of players that win a lot or haven't won much.
			  }
			});
		}


	}
	
	
}
