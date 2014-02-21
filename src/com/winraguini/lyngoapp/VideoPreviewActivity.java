package com.winraguini.lyngoapp;

//This activity will test saving videos as Parse files and then playing them back as a "playlist"


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.winraguini.lyngoapp.R;
import com.winraguini.lyngoapp.R.layout;
import com.winraguini.lyngoapp.R.menu;
import com.winraguini.lyngoapp.R.raw;

public class VideoPreviewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_preview);
		ArrayList<InputStream> arrayList = new ArrayList<InputStream>();
		arrayList.add(this.getResources().openRawResource(R.raw.a_video_test));
		arrayList.add(this.getResources().openRawResource(R.raw.a_video_test2));
		arrayList.add(this.getResources().openRawResource(R.raw.a_video_test3));
		int i = 0;
		for (InputStream s : arrayList) {
			Log.d("DEBUG", "Processing video");
			processVideoFiles(s, i);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video_preview, menu);
		return true;
	}
	
	public void processVideoFiles(InputStream is, int i) {
		//InputStream is = this.getResources().openRawResource(R.raw.a_video_test);
		try {
			byte[] byteArray = convertStreamToByteArray(is);
			ParseFile file = new ParseFile("resume.3gp6", byteArray);
			file.saveInBackground();
			
			ParseObject video = new ParseObject("Video");
			video.put("title", "title " + i);
			video.put("videoFile", file);
			video.saveInBackground();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("DEBUG", "Could not save file");
			e.printStackTrace();
		}
		
	}
	
	public static byte[] convertStreamToByteArray(InputStream is) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    byte[] buff = new byte[10240];
	    int i = Integer.MAX_VALUE;
	    while ((i = is.read(buff, 0, buff.length)) > 0) {
	        baos.write(buff, 0, i);
	    }

	    return baos.toByteArray(); // be sure to close InputStream in calling function
	}

}
