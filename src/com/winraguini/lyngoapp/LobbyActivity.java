package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class LobbyActivity extends Activity {
	private LobbyAdapter adapter;
	private ListView lvUsers;
	private ArrayList<ParseUser> users;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		lvUsers = (ListView)findViewById(R.id.lvUsers);
		users = new ArrayList<ParseUser>();
		adapter = new LobbyAdapter(this, users);
		lvUsers.setAdapter(adapter);
		getUsers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lobby, menu);
		return true;
	}
	
	private void getUsers() {
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.include("userProfile");
		query.findInBackground(new FindCallback<ParseUser>(){
			ArrayList<ParseUser> userList = new ArrayList<ParseUser>();
			public void done(List<ParseUser> objects, ParseException e) {
			    if (e == null) {
			        // The query was successful.
			    	for (int i = 0; i < objects.size(); i++) {
			    		ParseUser user = objects.get(i);
			    		Log.d("DEBUG", "user " + user.getUsername());
			    		adapter.add(objects.get(i));
			    	}
			    } else {
			        // Something went wrong.
			    }
			    
			  }
		});
	}
	
	public void onChatClicked(View v) {
		Toast.makeText(this, "Start chatting with " + v.getTag().toString(), Toast.LENGTH_SHORT).show();	
		Intent intent = new Intent(this, ChatActivity.class);		
		intent.putExtra("chatPartnerID", v.getTag().toString());
		startActivity(intent);		
	}


}
