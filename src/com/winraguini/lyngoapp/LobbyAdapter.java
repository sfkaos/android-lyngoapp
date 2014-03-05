package com.winraguini.lyngoapp;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.ProfilePictureView;
import com.parse.ParseObject;
import com.parse.ParseUser;

@SuppressLint("NewApi")
public class LobbyAdapter extends ArrayAdapter<ParseUser> {
	private ProfilePictureView userProfilePictureView;
	
	public LobbyAdapter(Context context, List<ParseUser> users) {
		super(context, 0, users);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.user_item, null);
		}
		
		ParseUser user = getItem(position);
		userProfilePictureView = (ProfilePictureView) view.findViewById(R.id.userProfilePicture);
		
		View onlineIndicator = (View) view.findViewById(R.id.vIsOnlineIndicator);
		Drawable d = null;
		if (user.getBoolean("isOnline")) {
			d = getContext().getResources().getDrawable(R.drawable.online_indicator);
			//vIsOnlineIndicator.setBackgroundColor(Color.parseColor("#666666"));				
		} else {
			d = getContext().getResources().getDrawable(R.drawable.offline_indicator);
		}
		onlineIndicator.setBackground(d);
		
		if (user.get("fbProfile") != null) {
									
			JSONObject userProfile = user.getJSONObject("fbProfile");
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
		TextView nameView = (TextView) view.findViewById(R.id.tvName);
		TextView locationView = (TextView) view.findViewById(R.id.tvLocation);
		TextView languageView = (TextView) view.findViewById(R.id.tvLanguages);
		
		ParseObject profile = user.getParseObject("userProfile");
		if (profile != null) {
			if (profile.getString("name") != null) {
				nameView.setText(profile.getString("name"));
			}
			if (profile.getString("location") != null) {
				locationView.setText(profile.getString("location"));
			}
			String languageISpeak = "";
			String languageToLearn = "";
			if (user.getString("languageISpeak") != null) {
				languageISpeak = user.getString("languageISpeak");
			}
			if (user.getString("languageToLearn") != null) {
				languageToLearn = user.getString("languageToLearn");
			}
			String formattedLanguageString = "";
			if (languageISpeak.length() > 0 && languageToLearn.length() > 0) {
				formattedLanguageString = "I speak " + "<font color='#3ACE74'>" + languageISpeak + "</font>. I want to learn <font color='#3ACE74'>" + languageToLearn + ".";
				languageView.setText(Html.fromHtml(formattedLanguageString));
			}
			
		}
		
		
//		
//		TextView timestampView = (TextView) view.findViewById(R.id.tvTimestamp);
//		String dateString = tweet.getTimestamp();
//		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
//		Date convertedDate = new Date();
//	    try {
//	    	convertedDate = dateFormat.parse(dateString);
//	    } catch (ParseException e) {
//	        // TODO Auto-generated catch block
//	        e.printStackTrace();
//	    }
//	    
//	    long now = System.currentTimeMillis();
//	    String result = (String) DateUtils.getRelativeTimeSpanString(convertedDate.getTime(), now, DateUtils.FORMAT_ABBREV_ALL);
//	    timestampView.setText(result);
//		
//		TextView bodyView = (TextView) view.findViewById(R.id.tvBody);
//		bodyView.setText(Html.fromHtml(tweet.getBody()));
		
		return view;
	}
	
}
