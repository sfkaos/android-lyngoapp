package com.winraguini.lyngoapp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class LobbyAdapter extends ArrayAdapter<ParseUser> {

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
	
//		ImageView imageView = (ImageView) view.findViewById(R.id.ivProfile);
//		ImageLoader.getInstance().displayImage(tweet.getUser().getProfileImageUrl(), imageView);
//		
		TextView nameView = (TextView) view.findViewById(R.id.tvName);
		Button btnChat = (Button) view.findViewById(R.id.btnChat);		
		btnChat.setTag(user.getObjectId().toString());
//		String formattedName = "<b>" + tweet.getUser().getName() + "</b>" + "<small><font color='#777777>@" +
//		tweet.getUser().getScreenName() + "</font></small>";
		ParseObject profile = user.getParseObject("userProfile");
		if (profile != null && profile.getString("name") != null) {
			nameView.setText(profile.getString("name"));
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
