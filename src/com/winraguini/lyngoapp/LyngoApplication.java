package com.winraguini.lyngoapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;
//import com.parse.integratingfacebooktutorial.R;

public class LyngoApplication extends Application {

	static final String TAG = "MyApp";

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "luwfhv0ejW9At1cS014iXE75JdD1eVGzDNhF1jUi",
				"A7H0jUx8mNWoFe1xmpeeYgTXp4Rntu5Hae8YeJg4");

		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.app_id));
		
		PushService.setDefaultPushCallback(this, LoginActivity.class); 
		
	}
}
