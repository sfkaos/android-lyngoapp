package com.winraguini.lyngoapp.models;

import java.util.List;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


@ParseClassName("Chat")
public class Chat extends ParseObject{
	
  public Chat(){

  }

  public String getChatID(){
     return getString("chatID");
  }
  
  public void setChatID(String chatID) {
	  put("chatID", chatID);
  }

  public ParseUser getPartner1() {
	 return getParseUser("partner1");
  }
  
  public void setPartner1(ParseUser user) {
	  put ("partner1", user);
  }
  
  public ParseUser getPartner2() {
	  return getParseUser("partne2");
  }
  
  public void setPartner2(ParseUser user) {
	  put ("partner2", user);
  }
  
//  public String getLastMessage() {
//	  ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatMessage");
//	  query.whereEqualTo("chattedOn", this);	  
//	  query.findInBackground(new FindCallback<ParseObject>() {
//		
//		@Override
//		public void done(List<ParseObject> arg0, ParseException arg1) {
//			// TODO Auto-generated method stub
//			return "";
//		}
//	});
//
//	  
//  }
  
}