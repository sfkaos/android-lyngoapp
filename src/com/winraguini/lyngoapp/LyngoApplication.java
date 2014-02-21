package com.winraguini.lyngoapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
//import com.parse.integratingfacebooktutorial.R;
import com.winraguini.lyngoapp.R;
import com.winraguini.lyngoapp.R.string;

public class LyngoApplication extends Application {

	static final String TAG = "MyApp";

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "DkkHOlOQkgY380RzJYysUGayxaNgHitwPgfz7zaO",
				"lwdIaRNMSkxZhLm8DWNFCRYFwCgfAEarH4silhgn");

		// Set your Facebook App Id in strings.xml
		ParseFacebookUtils.initialize(getString(R.string.app_id));
	}
}
