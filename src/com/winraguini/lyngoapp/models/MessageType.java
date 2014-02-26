package com.winraguini.lyngoapp.models;



public enum MessageType {
	   CHAT_MESSAGE_TYPE(1),
	   STATUS_MESSAGE_TYPE(2);
	   
	   private int value;
	   
	   private MessageType(int value) {
	      this.value = value;
	   }
	   public int getValue() {
	      return value;
	   }
}
