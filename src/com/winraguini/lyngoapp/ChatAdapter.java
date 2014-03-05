package com.winraguini.lyngoapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class ChatAdapter extends ArrayAdapter<ParseObject> {
	private ProfilePictureView userProfilePictureView;
	private ParseUser chatPartner = null;
	private static Handler handler = null;

	public ChatAdapter(Context context, List<ParseObject> chats) {
		super(context, 0, chats);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.chat_item, null);
		}

		ParseObject chat = getItem(position);

		ParseUser partner1 = chat.getParseUser("partner1");
		ParseUser partner2 = chat.getParseUser("partner2");

		if (partner1.getObjectId().equals(
				ParseUser.getCurrentUser().getObjectId())) {
			chatPartner = partner2;
		} else {
			chatPartner = partner1;
		}

		userProfilePictureView = (ProfilePictureView) view
				.findViewById(R.id.userProfilePicture);

		if (chatPartner.get("fbProfile") != null) {

			JSONObject userProfile = chatPartner.getJSONObject("fbProfile");
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
				Log.d(LyngoApplication.TAG, "Error parsing saved user data.");
			}
		}
		TextView nameView = (TextView) view.findViewById(R.id.tvName);
		TextView messageView = (TextView) view.findViewById(R.id.tvLastChatMessage);
		TextView timestampView = (TextView) view.findViewById(R.id.tvChatTimestamp);
		
		// Button btnChat = (Button) view.findViewById(R.id.btnChat);
		// btnChat.setTag(user.getObjectId().toString());
		// String formattedName = "<b>" + tweet.getUser().getName() + "</b>" +
		// "<small><font color='#777777>@" +
		// tweet.getUser().getScreenName() + "</font></small>";

		ParseObject profile = chatPartner.getParseObject("userProfile");
		if (profile != null && profile.getString("name") != null) {
			nameView.setText(profile.getString("name"));
		}

		if (chat.getString("lastMessage") != null && !chat.getString("lastMessage").isEmpty()) {
			String lastMessage = chat.getString("lastMessage");
			if (chat.getString("chattedByID") != null) {
				if (chat.getString("chattedByID").equals(ParseUser.getCurrentUser().getObjectId())) {
					lastMessage = "<bold>You: </bold>" + lastMessage;
				} else {
					if (chat.getString("chattedBy") != null) {
						lastMessage = "<bold>" + chat.getString("chattedBy") + ": </bold>" + lastMessage; 
					}
				}
			}
			messageView.setText(Html.fromHtml(lastMessage));
		}
		
		if (chat.getLong("lastMessageTime") > 0) {
			long timestamp = chat.getLong("lastMessageTime");
			
			Calendar cal = Calendar.getInstance();
			TimeZone tz = cal.getTimeZone();

			/* debug: is it local time? */
			Log.d("Time zone: ", tz.getDisplayName());

			/* date formatter in local timezone */
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			sdf.setTimeZone(tz);

			/* print your timestamp and double check it's the date you expect */
			String localTime = sdf.format(new Date(timestamp)); // I assume your timestamp is in seconds and you're converting to milliseconds?
			Log.d("Time: ", localTime);
			
			
			long now = System.currentTimeMillis();
			String result = (String) DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.FORMAT_ABBREV_ALL);
			timestampView.setText(result);
			
		} else {
			Log.d("DEBUG", "CHAT updatedAt is null");
		}
		
		return view;
	}

}
