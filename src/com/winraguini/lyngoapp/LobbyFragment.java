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
		lvUsers = (ListView)getActivity().findViewById(R.id.lvUsers);
		users = new ArrayList<ParseUser>();
		adapter = new LobbyAdapter(getActivity(), users);
		lvUsers.setAdapter(adapter);
		setUpUsers();
		
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
		
		
				
	}
	
	private void setUpUsers() {
		lvUsers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
				// TODO Auto-generated method stub
				ParseUser user = users.get(pos);
				Intent intent = new Intent(getActivity(), ChatActivity.class);	
				intent.putExtra("chatParticipantID",user.getObjectId());
				startActivity(intent);
			}
		});		
	}
	
	private void getUsers() {
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
		
		
		
//		ParseQuery<ParseUser> query = ParseUser.getQuery();
//		query.include("userProfile");
//		query.findInBackground(new FindCallback<ParseUser>(){
//			ArrayList<ParseUser> userList = new ArrayList<ParseUser>();
//			public void done(List<ParseUser> objects, ParseException e) {
//			    if (e == null) {
//			        // The query was successful.
//			    	for (int i = 0; i < objects.size(); i++) {
//			    		ParseUser user = objects.get(i);
//			    		Log.d("DEBUG", "user " + user.getUsername());
//			    		adapter.add(objects.get(i));
//			    	}
//			    } else {
//			        // Something went wrong.
//			    }
//			    
//			  }
//		});
	}
	
	public void onChatClicked(View v) {
		Toast.makeText(getActivity(), "Start chatting with " + v.getTag().toString(), Toast.LENGTH_SHORT).show();	
		Intent intent = new Intent(getActivity(), ChatActivity.class);		
		intent.putExtra("chatParticipantID", v.getTag().toString());
		startActivity(intent);		
	}
	
	
	
}
