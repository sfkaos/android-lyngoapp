//
// SettingsActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Settings;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooVoo.oovoosample.ConferenceManager;
import com.winraguini.lyngoapp.R;
import com.ooVoo.oovoosample.VideoCall.MediaDeviceWrapper;

// Settings presenter entity
public class SettingsActivity extends Activity {

	private ConferenceManager mConferenceManager = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mConferenceManager = ConferenceManager.getInstance(getApplicationContext());	
		initView();		
	}
	
	protected void initView() {
		// Set layout
		setContentView( R.layout.settings);
		TextView sdk = (TextView) findViewById(R.id.sdkVersionText);
		sdk.setText(mConferenceManager.getSDKVersion());		
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setHomeButtonEnabled(true);
			ab.setTitle(R.string.settings);
			ab.setHomeButtonEnabled(true);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false);
			ab.setIcon(R.drawable.menu_ic_settings);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null)
			return false;

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
		
	// Sets the requested spinner's value
	private void setSelectedSpinnerValue(Spinner spinner, MediaDeviceWrapper valueToSet) {
		@SuppressWarnings("unchecked")
		ArrayAdapter<MediaDeviceWrapper> adapter = (ArrayAdapter<MediaDeviceWrapper>) spinner.getAdapter();
		int spinnerPosition = adapter.getPosition(valueToSet);
		spinner.setSelection(spinnerPosition);
	}

	// Gets the requested spinner's value
	private MediaDeviceWrapper getSelectedSpinnerValue(Spinner spinner) {
		return ((MediaDeviceWrapper) spinner.getSelectedItem());
	}

	@Override
	protected void onResume() {
		// Read & display user settings
		super.onResume();

		// Read settings
		UserSettings settings = mConferenceManager.retrieveSettings();

		// Fill spinners - get devices from model
		Spinner cameraSpinner = (Spinner) findViewById(R.id.cameraSpinner);
		setSpinnerValues(cameraSpinner, mConferenceManager.getCameras());

		Spinner microphoneSpinner = (Spinner) findViewById(R.id.microphoneSpinner);
		setSpinnerValues(microphoneSpinner, mConferenceManager.getMicrohones());

		Spinner speakersSpinner = (Spinner) findViewById(R.id.speakersSpinner);
		setSpinnerValues(speakersSpinner, mConferenceManager.getSpeakers());

		// Set spinners
		setSelectedSpinnerValue(cameraSpinner, new MediaDeviceWrapper(
				settings.CameraType));
		setSelectedSpinnerValue(microphoneSpinner, new MediaDeviceWrapper(
				settings.MicrophoneType));
		setSelectedSpinnerValue(speakersSpinner, new MediaDeviceWrapper(
				settings.SpeakersType));

	}

	// Sets the available spinner's values
	private void setSpinnerValues(Spinner spinner, List<MediaDeviceWrapper> values) {
		ArrayAdapter<MediaDeviceWrapper> adapter;
		adapter = new ArrayAdapter<MediaDeviceWrapper>(this, android.R.layout.simple_spinner_item, values);
		spinner.setAdapter(adapter);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Persist changes
		Spinner cameraSpinner = (Spinner) findViewById(R.id.cameraSpinner);
		Spinner microphoneSpinner = (Spinner) findViewById(R.id.microphoneSpinner);
		Spinner speakersSpinner = (Spinner) findViewById(R.id.speakersSpinner);

		UserSettings settingsToPersist = mConferenceManager.retrieveSettings();
		MediaDeviceWrapper cameraWrapper = getSelectedSpinnerValue(cameraSpinner);
		settingsToPersist.CameraType = cameraWrapper == null? 0 : cameraWrapper.getDeviceId();
		MediaDeviceWrapper micWrapper = getSelectedSpinnerValue(microphoneSpinner);
		settingsToPersist.MicrophoneType = micWrapper == null? 0 : micWrapper.getDeviceId();
		
		MediaDeviceWrapper speakersWrapper = getSelectedSpinnerValue(speakersSpinner);
		settingsToPersist.SpeakersType = speakersWrapper == null ? 0 : speakersWrapper.getDeviceId();
		
		mConferenceManager.persistSettings(settingsToPersist);
		
		try {
			mConferenceManager.loadDataFromSettings();
		} catch (Exception e) {
			AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
			TextView myMsg = new TextView(this);
			myMsg.setText("An Error occured while selecting devices");
			myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
			popupBuilder.setView(myMsg);
		}		
	}

	
}
