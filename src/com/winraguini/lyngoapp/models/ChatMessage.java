package com.winraguini.lyngoapp.models;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseObject;


public class ChatMessage {
	private String chatParticipantID;
	private String message;
	
	public static ChatMessage fromJson(JSONObject jsonObject) {
		ChatMessage chatMessage = new ChatMessage();
        try {
        	chatMessage.setChatPartnerID(jsonObject.getString("chatParticipantID"));
        	chatMessage.setMessage(jsonObject.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return chatMessage;
    }
	
	public static ChatMessage fromParseObject(ParseObject parseObject) {
		ChatMessage chatMessage = new ChatMessage();        
        chatMessage.setChatPartnerID(parseObject.getString("chatParticipantID"));
        chatMessage.setMessage(parseObject.getString("message"));        
        return chatMessage;
    }

	public String getChatPartnerID() {
		return chatParticipantID;
	}

	public void setChatPartnerID(String chatParticipantID) {
		this.chatParticipantID = chatParticipantID;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
