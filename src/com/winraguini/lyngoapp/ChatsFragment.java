package com.winraguini.lyngoapp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ChatsFragment extends SherlockFragment {
	private ChatAdapter adapter;
	private ListView lvChats;
	private ArrayList<ParseObject> chats;
	private ParseUser currentUser;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Defines the xml file for the fragment
		View view = inflater.inflate(R.layout.fragment_chats, container, false);
		// Setup handles to view objects here				
		return view;
    }
	
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);	
		currentUser = ParseUser.getCurrentUser();
		lvChats = (ListView)getActivity().findViewById(R.id.lvChats);
		chats = new ArrayList<ParseObject>();
		setupChats();
		getAllChats();		
	}
	
	private void getAllChats() {
		ParseQuery<ParseObject> chatsPartner1 = ParseQuery.getQuery("Chatter");
		chatsPartner1.whereEqualTo("partner1", currentUser);
		 
		ParseQuery<ParseObject> chatsPartner2 = ParseQuery.getQuery("Chatter");
		chatsPartner2.whereEqualTo("partner2", currentUser);
		 
		List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
		queries.add(chatsPartner1);
		queries.add(chatsPartner2);
		 
		ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
		mainQuery.include("partner1");
		mainQuery.include("partner2");
		mainQuery.include("partner1.userProfile");
		mainQuery.include("partner2.userProfile");
		
		mainQuery.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> chats, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					adapter.addAll(chats);
				} else {
					Log.d("DEBUG", "There was an error getting your current chats");
				}
			}
		});		
	}
	
	private void setupChats() {
		adapter = new ChatAdapter(getActivity(), chats);
		lvChats.setAdapter(adapter);
	}
	
//	private void setUpUsers() {
//		lvUsers.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
//				// TODO Auto-generated method stub
//				ParseUser user = users.get(pos);
//				Intent intent = new Intent(getActivity(), ChatActivity.class);	
//				intent.putExtra("chatParticipantID",user.getObjectId());
//				startActivity(intent);
//			}
//		});		
//	}
//	
//	private void getUsers() {
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
//	}
//	
//	public void onChatClicked(View v) {
//		Toast.makeText(getActivity(), "Start chatting with " + v.getTag().toString(), Toast.LENGTH_SHORT).show();	
//		Intent intent = new Intent(getActivity(), ChatActivity.class);		
//		intent.putExtra("chatParticipantID", v.getTag().toString());
//		startActivity(intent);		
//	}
	
}
