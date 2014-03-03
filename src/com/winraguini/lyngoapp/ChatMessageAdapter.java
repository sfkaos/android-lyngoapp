package com.winraguini.lyngoapp;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.winraguini.lyngoapp.models.ChatMessage;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
	private static final int ITEM_PARTICIPANT_1 = 0;
    private static final int ITEM_PARTICIPANT_2 = 1;
    private String currentChatParticipantID = null;
	final static int TYPE_MAX_COUNT = 2;
	
	public ChatMessageAdapter(Context context, List<ChatMessage>chatMessages) {		
		// TODO Auto-generated constructor stub
		super(context, 0, chatMessages);
	}
	
	@Override
    public int getItemViewType(int position) {
		//ParseObject chatMessage = getItem(position);
		//Based on chatMessage		
		ChatMessage chatMessage = getItem(position);
		Log.d("DEBUG", "chatMessage is " + chatMessage.getMessage());
		Log.d("DEBUG", "chatPartnerID is " + chatMessage.getChatPartnerID());
		if (chatMessage.getChatPartnerID().equalsIgnoreCase(getCurrentChatParticipantID())) {
			return ITEM_PARTICIPANT_2;
		} else {
			return ITEM_PARTICIPANT_1;
		}        
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	ViewHolder holder = null;
    	ChatMessage chatMessage = getItem(position);
        int type = getItemViewType(position);
        System.out.println("getView " + position + " " + convertView + " type = " + type);
        if (convertView == null) {
        	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//convertView = inflater.inflate(R.layout.user_item, null);        	
            holder = new ViewHolder();
            switch (type) {
                case ITEM_PARTICIPANT_1:
                    convertView = inflater.inflate(R.layout.chat_item_participant_1, null);
                    holder.textViewItem = (TextView)convertView.findViewById(R.id.tvChatMessage);
                    break;
                case ITEM_PARTICIPANT_2:
                    convertView = inflater.inflate(R.layout.chat_item_participant_2, null);
                    holder.textViewItem = (TextView)convertView.findViewById(R.id.tvChatMessage);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.textViewItem.setText(chatMessage.getMessage());
        return convertView;
    }
    
    
    
    public String getCurrentChatParticipantID() {
		return currentChatParticipantID;
	}

	public void setCurrentChatParticipantID(String currentChatParticipantID) {
		this.currentChatParticipantID = currentChatParticipantID;
	}



	static class ViewHolder {
        TextView textViewItem;
    }

}
