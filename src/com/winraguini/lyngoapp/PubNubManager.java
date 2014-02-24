package com.winraguini.lyngoapp;

import android.util.Log;

import com.pubnub.api.*;

import org.json.*;


public class PubNubManager {
	private static PubNubManager instance;
	
	private PubNubManager() {
	}
	
	public static PubNubManager getInstance() {
		if (null == instance) {
			instance = new PubNubManager();
		}
		return instance;
	}
	
}
