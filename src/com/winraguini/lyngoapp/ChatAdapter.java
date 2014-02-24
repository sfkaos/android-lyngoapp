package com.winraguini.lyngoapp;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class ChatAdapter extends ArrayAdapter<ParseObject> {
	private static final int ITEM_PARTICIPANT_1 = 0;
    private static final int ITEM_PARTICIPANT_2 = 1;
    
	final static int TYPE_MAX_COUNT = 2;
	
	public ChatAdapter(Context context, List<ParseObject> chatMessages) {
		super(context, 0, chatMessages);
		// TODO Auto-generated constructor stub
	}
	
	@Override
    public int getItemViewType(int position) {
		//ParseObject chatMessage = getItem(position);
		//Based on chatMessage		
        return ITEM_PARTICIPANT_1;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	
    	ViewHolder holder = null;
    	ParseObject chatMessage = getItem(position);
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
        holder.textViewItem.setText(chatMessage.getString("message"));
        return convertView;
    }
    
    
    
    static class ViewHolder {
        TextView textViewItem;
    }

}
