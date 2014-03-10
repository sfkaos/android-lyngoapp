//
// VideoCallActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.VideoCall;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ooVoo.oovoosample.ConferenceManager;
import com.ooVoo.oovoosample.ConferenceManager.SessionControlsListener;
import com.ooVoo.oovoosample.ConferenceManager.SessionListener;
import com.ooVoo.oovoosample.ConferenceManager.SessionParticipantsListener;
import com.ooVoo.oovoosample.SessionUIPresenter;
import com.ooVoo.oovoosample.Alerts.AlertsActivity;
import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.ParticipantHolder;
import com.ooVoo.oovoosample.Common.ParticipantVideoSurface;
import com.ooVoo.oovoosample.Common.ParticipantsManager;
import com.ooVoo.oovoosample.Common.PreviewSurfaceHolder;
import com.ooVoo.oovoosample.Common.Utils;
import com.ooVoo.oovoosample.Information.InformationActivity;
import com.ooVoo.oovoosample.Settings.UserSettings;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.IConferenceCore.CameraResolutionLevel;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.Utils.LogSdk;
import com.winraguini.lyngoapp.LyngoToast;
import com.winraguini.lyngoapp.R;

// Video presenter entity
public class VideoCallActivity extends Activity implements OnClickListener,
		SessionControlsListener, SessionListener, SessionParticipantsListener,
		SessionUIPresenter, View.OnTouchListener {
	
	private Boolean _initialized = false;
	private ConferenceManager mConferenceManager = null;
	private HashMap<Integer, ParticipantVideoSurface> _surfaces = new HashMap<Integer, ParticipantVideoSurface>();
	private ParticipantVideoSurface mParticipantsVideoSurfaces[];
	private Spinner mResSpinner;
	private PreviewSurfaceHolder mPreviewSurfaceHolder = null;
	private VCParticipantsController mVCParticipantsController = null; 
	private Handler handler = null;
	
	private static final int SINGLE_LANGUAGE_PRACTICE_TIME_LIMIT = 1000 * 60;
	private static final String SECOND_PRACTICE_LANGUAGE_MSG = "second_practice_language_msg";
	private static final String END_PRACTICE_MSG = "end_practice_msg";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		mConferenceManager = ConferenceManager
				.getInstance(getApplicationContext());
		LogSdk.d(Utils.getOoVooTag(), "savedInstanceState is null: "
				+ (savedInstanceState == null));
		handler = new Handler() {
			  @Override
			  public void handleMessage(Message msg) {				  
				  String aResponse = msg.getData().getString("message");

                  if (aResponse.equals(SECOND_PRACTICE_LANGUAGE_MSG)) {
                	  secondLanguagePracticeSession();
                  } else if (aResponse.equals(END_PRACTICE_MSG)) {
                	  endPracticeSession();
                  }     
			  }
		};
		initView();
	}
	
	private void secondLanguagePracticeSession() {
		Resources res = getResources();
//		LyngoToast.showToast(getApplicationContext(), res.getString(R.string.spanish_practice), Toast.LENGTH_SHORT);
		sendThreadedMessageDelayed(END_PRACTICE_MSG, SINGLE_LANGUAGE_PRACTICE_TIME_LIMIT);
	}
	
	private void endPracticeSession() {
		mConferenceManager.endOfCall();
		LyngoToast.showToast(getBaseContext(), "Your practice session is over.", Toast.LENGTH_SHORT);
	}

	protected void initView() {
		// Set layout
		setContentView(R.layout.video_call);
		
		LogSdk.d(Utils.getOoVooTag(), "setting cameraButton");
		Button cameraButton = (Button) findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(this);

		LogSdk.d(Utils.getOoVooTag(), "setting microphoneButton");
		Button microphoneButton = (Button) findViewById(R.id.microphoneButton);
		microphoneButton.setOnClickListener(this);

		LogSdk.d(Utils.getOoVooTag(), "setting speakersButton");
		Button speakersButton = (Button) findViewById(R.id.speakersButton);
		speakersButton.setOnClickListener(this);

		LogSdk.d(Utils.getOoVooTag(), "setting endOfCallButton");
		Button endOfCallButton = (Button) findViewById(R.id.endOfCallButton);
		endOfCallButton.setOnClickListener(this);

		LogSdk.d(Utils.getOoVooTag(), "setting resolutionSpinner");
		mResSpinner = (Spinner) findViewById(R.id.resolutionSpinner);
		ArrayList<ResolutionWrapper> values = new ArrayList<ResolutionWrapper>();
		values.add(new ResolutionWrapper(CameraResolutionLevel.ResolutionMedium, "Med"));
		values.add(new ResolutionWrapper(CameraResolutionLevel.ResolutionLow, "Low"));
		Utils.setSpinnerValues(this, mResSpinner, values);

		mResSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				ResolutionWrapper selectedRes = Utils
						.getSelectedSpinnerValue(mResSpinner);
				UserSettings settings = mConferenceManager.retrieveSettings();
				settings.Resolution = selectedRes.Level;
				mConferenceManager.setVideoResolution(settings.Resolution);
				mConferenceManager.persistSettings(settings);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		LogSdk.d(Utils.getOoVooTag(), "setting ParticipantVideoSurfaces");
		mVCParticipantsController = (VCParticipantsController)findViewById(R.id.participants_controller);
		
		mParticipantsVideoSurfaces = new ParticipantVideoSurface[3];
		mParticipantsVideoSurfaces[0] = (ParticipantVideoSurface)findViewById(R.id.user1_layout_id);
		mParticipantsVideoSurfaces[0].avatar = ((ImageView) findViewById(R.id.user1Avatar));
		mParticipantsVideoSurfaces[0].nameBox = ((TextView) findViewById(R.id.user1Name));
		mParticipantsVideoSurfaces[0].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.user1VideoSurface));

		mParticipantsVideoSurfaces[1] = (ParticipantVideoSurface)findViewById(R.id.user2_layout_id);
		mParticipantsVideoSurfaces[1].avatar = ((ImageView) findViewById(R.id.user2Avatar));
		mParticipantsVideoSurfaces[1].nameBox = ((TextView) findViewById(R.id.user2Name));
		mParticipantsVideoSurfaces[1].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.user2VideoSurface));

		mParticipantsVideoSurfaces[2] = (ParticipantVideoSurface)findViewById(R.id.user3_layout_id);
		mParticipantsVideoSurfaces[2].avatar = ((ImageView) findViewById(R.id.user3Avatar));
		mParticipantsVideoSurfaces[2].nameBox = ((TextView) findViewById(R.id.user3Name));
		mParticipantsVideoSurfaces[2].mVideoView = ((android.opengl.GLSurfaceView) findViewById(R.id.user3VideoSurface));
		
		mPreviewSurfaceHolder = (PreviewSurfaceHolder)findViewById(R.id.preview_layout_id);
		mPreviewSurfaceHolder.mNoVideoImageView = (ImageView)findViewById(R.id.myAvatar);
		mPreviewSurfaceHolder.mPreviewView = ((SurfaceView) findViewById(R.id.myVideoSurface));
		
		mPreviewSurfaceHolder.setOnTouchListener(this);
		mParticipantsVideoSurfaces[0].setOnTouchListener(this);
		mParticipantsVideoSurfaces[1].setOnTouchListener(this);
		mParticipantsVideoSurfaces[2].setOnTouchListener(this);
		
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setHomeButtonEnabled(false);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(false);
			ab.setDisplayUseLogoEnabled(false);
			ab.setIcon(R.drawable.ic_main);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.vc_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == null)
			return false;

		switch (item.getItemId()) {
			case android.R.id.home:
				mConferenceManager.endOfCall();
				finish();
				return true;
//			case R.id.menu_information:
//				openInfrormationView();
//				return true;
//			case R.id.menu_alerts:
//				openAlertsView();
//				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Start a new activity using the requested effects
	private void startActivity(Class<?> activityToStart) {
		// Maybe should use this flag just for Video Call activity?
		Intent myIntent = new Intent(this, activityToStart);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(myIntent);
	}

	@Override
	public void onClick(View v) {
		// Check which button was pressed
		switch (v.getId()) {
		case R.id.endOfCallButton: {
			mConferenceManager.endOfCall();
			break;
		}
		case R.id.cameraButton: {
			fireCameraEnabled(false);
			mConferenceManager.toggleCameraMute();
			break;
		}
		case R.id.microphoneButton: {
			fireMicrophoneEnabled(false);
			mConferenceManager.toggleMicrophoneMute();
			break;
		}
		case R.id.speakersButton: {
			fireSpeakersEnabled(false);
			mConferenceManager.toggleSpeakersMute();
			break;
		}
		default: {
			break;
		}
		}
	}

	private void openInfrormationView() {
		startActivity(InformationActivity.class);
	}

	@Override
	protected void onDestroy() {
		LogSdk.d(Utils.getOoVooTag(), "VideoCallActivity onDestroy");
		super.onDestroy();
	}

	private void openAlertsView() {
		startActivity(AlertsActivity.class);
	}

	// Called from model upon camera mute change
	@Override
	public void onSetCameraMuted(final boolean isMuted) {
		LogSdk.d(Utils.getOoVooTag(), "onSetCameraMuted");
		// Just GUI. SDK calls are in model
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireCameraMuted(isMuted);
			}
		});
	}

	private void fireCameraMuted(boolean isMuted) {
		final Button btn = (Button) (findViewById(R.id.cameraButton));
		final SurfaceView surface = ((SurfaceView) findViewById(R.id.myVideoSurface));

		btn.setSelected(isMuted ? true : false);
		surface.setVisibility(isMuted ? SurfaceView.INVISIBLE : SurfaceView.VISIBLE);
		addAlert("Camera mute set to: " + isMuted);
	}

	// Called from model upon microphone mute change
	@Override
	public void onSetMicrophoneMuted(final boolean isMuted) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireMicrophoneMuted(isMuted);
			}
		});
	}

	private void fireMicrophoneMuted(boolean isMuted) {
		Button btn = (Button) (findViewById(R.id.microphoneButton));
		btn.setSelected(isMuted ? true : false);
		addAlert("Microphone mute set to: " + isMuted);
	}

	// Called from model upon speakers mute change
	@Override
	public void onSetSpeakersMuted(final boolean isMuted) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireSpeakersMuted(isMuted);
			}
		});
	}

	private void fireSpeakersMuted(boolean isMuted) {
		// Just GUI. SDK calls are in model
		final Button btn = ((Button) (findViewById(R.id.speakersButton)));
		btn.setSelected(isMuted ? true : false);
		addAlert("Speakers mute set to: " + isMuted);
	}

	public void onSetCameraEnabled(final boolean isEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireCameraEnabled(isEnabled);
			}
		});
	}

	private void fireCameraEnabled(boolean isEnabled) {
		final Button btn = (Button) (findViewById(R.id.cameraButton));
		btn.setEnabled(isEnabled);
	}

	public void onSetMicrophoneEnabled(final boolean isEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireMicrophoneEnabled(isEnabled);
			}
		});
	}

	private void fireMicrophoneEnabled(boolean isEnabled) {
		Button btn = (Button) (findViewById(R.id.microphoneButton));
		btn.setEnabled(isEnabled);
	}

	public void onSetSpeakersEnabled(final boolean isEnabled) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fireSpeakersEnabled(isEnabled);
			}
		});
	}

	private void fireSpeakersEnabled(boolean isEnabled) {
		Button btn = ((Button) (findViewById(R.id.speakersButton)));
		btn.setEnabled(isEnabled);
	}

	// Add an alert to log
	private void addAlert(String text) {
		AlertsManager.getInstance().addAlert(text);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mConferenceManager.pauseSession();
	}

	@Override
	protected void onResume() {
		super.onResume();
		UserSettings settings = mConferenceManager.retrieveSettings();
		Utils.setSelectedSpinnerValue(mResSpinner, new ResolutionWrapper(settings.Resolution, "Doesnt Matter"));
		if (!_initialized) {
			_initialized = true;
			initSession(mParticipantsVideoSurfaces);
		}
		mConferenceManager.resumeSession();
	}

	public synchronized void initSession(ParticipantVideoSurface[] mParticipantsVideoSurfaces) {
		// Select devices
		try {
			mConferenceManager.addSessionControlsListener(this);
			mConferenceManager.addSessionListener(this);
			mConferenceManager.addSessionParticipantsListener(this);
			mConferenceManager.setSessionUIPresenter(this);

			ParticipantsManager mParticipantsManager = mConferenceManager.getParticipantsManager();
			_surfaces.clear();
			LogSdk.i(Utils.getOoVooTag(), "VideoCallActivity :: initSession -> mParticipantsVideoSurfaces length = " + mParticipantsVideoSurfaces.length );
			for (int i = 0; i < mParticipantsVideoSurfaces.length; i++) {
				mParticipantsManager.getHolder().addGLView(mParticipantsVideoSurfaces[i].mVideoView.getId());
				_surfaces.put(mParticipantsVideoSurfaces[i].mVideoView.getId(), mParticipantsVideoSurfaces[i]);
			}
			mConferenceManager.initSession(mParticipantsVideoSurfaces);
			mConferenceManager.setUIReadyState(true);
		} catch (Exception e) {
			LogSdk.e(Utils.getOoVooTag(), "", e);
		}
	}

	@Override
	public void onParticipantJoinedSession(final String sParticipantId, final int participantViewId, final String sOpaqueString) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConferenceManager.getParticipantsManager();
				if (mConferenceManager.getParticipantsManager().getParticipants().size() == ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL - 1) {
					
					Resources res = getResources();
//					LyngoToast.showToast(getApplicationContext(), res.getString(R.string.english_practice), Toast.LENGTH_SHORT);
					
					Runnable runnable = new Runnable() {
				        public void run() {
				        	sendThreadedMessageDelayed(SECOND_PRACTICE_LANGUAGE_MSG, SINGLE_LANGUAGE_PRACTICE_TIME_LIMIT);
				        }
			      };
			      
			      Thread mythread = new Thread(runnable);
			      mythread.start();
				}
				
				
				
				
				LogSdk.i(Utils.getOoVooTag(), "VideoCallActivity :: " + sParticipantId +" joined to conference;  {participantViewId = " + participantViewId +" }");
				if (participantViewId != -1 && _surfaces != null) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.setVisibility(View.VISIBLE);
						surface.showAvatar();
						surface.setName(sOpaqueString);
					}
					mVCParticipantsController.onResize();
				}
			}
		});
	}
	
	public void sendThreadedMessageDelayed(String msg, long delayMillis) {
		Message msgObj = handler.obtainMessage();
        Bundle b = new Bundle();        
        b.putString("message", msg);
        msgObj.setData(b);
        handler.sendMessageDelayed(msgObj, delayMillis);
   	}

	@Override
	public void onJoinSessionError(ConferenceCoreError error) {
	}

	@Override
	public void onJoinSessionWrongDataError() {
	}

	@Override
	public void onJoinSessionSucceeded() {
	}

	@Override
	public void onSessionIDGenerated(String sSessionId) {
	}

	@Override
	public void onSessionError(ConferenceCoreError error) {
	}

	@Override
	public void onLeftSession(ConferenceCoreError eErrorCode) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LogSdk.d(Utils.getOoVooTag(), "onSessionLeft (JAVA MF)");
				// // Just GUI. SDK calls are in model
//				startActivity(MainActivity.class);
				// Kill the activity so it will not remain in the stack
				finish();
			}
		});
	}
	
	

	@Override
	public void finish() {
		if(mConferenceManager != null)
			mConferenceManager.setUIReadyState(false);
		super.finish();
	}

	@Override
	public SurfaceView getPreviewSurface() {
		return (SurfaceView) findViewById(R.id.myVideoSurface);
	}

	@Override
	public void initSurfaces() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (ParticipantVideoSurface surf : _surfaces.values()) {
					surf.showEmptyCell();
				}
			}
		});
	}

	@Override
	public void updateParticipantSurface(final int participantViewId, final String displayName, final boolean isVideoOn) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ParticipantVideoSurface surface = _surfaces.get(participantViewId);
				if (surface != null) {
					surface.setVisibility(View.VISIBLE);
					surface.setName(displayName);
					surface.showAvatar();
					if (isVideoOn) {
						surface.showVideo();
						surface.hideUserStatusInfo();
					}
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		mConferenceManager.endOfCall();
		finish();
		super.onBackPressed();
	}

	public void onParticipantVideoPaused(final int participantViewId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.showAvatar();
						surface.showUserStatusInfo();
					}
				}
			}
		});
	}

	public void onParticipantVideoTurnedOff(final ConferenceCoreError eErrorCode,
			final int participantViewId, final String sParticipantId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.showAvatar();
						surface.hideUserStatusInfo();
					}
				}
			}
		});
	}

	public void onParticipantVideoResumed(final int participantViewId,
			final String sParticipantId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces
							.get(participantViewId);
					if (surface != null) {
						surface.showVideo();
						surface.setName(sParticipantId);
					}
				}
			}
		});
	}

	public void onParticipantLeftSession(final int participantViewId, final String sParticipantId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.setVisibility(View.INVISIBLE);
						surface.showEmptyCell();
					}
					mVCParticipantsController.onResize();
				}
			}
		});
	}

	public void onParticipantVideoTurnedOn(ConferenceCoreError eErrorCode,
			String sParticipantId, FrameSize frameSize,
			final int participantViewId, final String displayName) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (participantViewId != -1) {
					ParticipantVideoSurface surface = _surfaces.get(participantViewId);
					if (surface != null) {
						surface.showVideo();
						surface.setName(displayName);
					}
				}
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v != null){
			if(v.getVisibility() == View.INVISIBLE)
				return false;
			
			if(v.getId() == R.id.preview_layout_id){
				mConferenceManager.switchUIFullMode(mPreviewSurfaceHolder.mPreviewView.getId());
			}
			else{
				if (_surfaces != null) {
					for(ParticipantVideoSurface surfaceHolder : _surfaces.values()){
						if(surfaceHolder.getId() == v.getId()){
							mConferenceManager.switchUIFullMode(surfaceHolder.mVideoView.getId());
							break;
						}						
					}
				}
			}
		}
		return false;
	}
	
	public void onFullModeChanged(final int participantViewId){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				if (participantViewId != -1 && _surfaces != null) {
					for(ParticipantVideoSurface surfaceHolder : _surfaces.values()){
						if(surfaceHolder.mVideoView.getId() == participantViewId){
							surfaceHolder.setVisibility(View.VISIBLE);
						}
						else{
							surfaceHolder.setVisibility(View.GONE);
						}							
					}
				}
				if(mPreviewSurfaceHolder.mPreviewView.getId() != participantViewId){
					mPreviewSurfaceHolder.setVisibility(View.GONE);
				}
				else{
					mPreviewSurfaceHolder.setVisibility(View.VISIBLE);
				}
				mVCParticipantsController.onModeUpdated(VCParticipantsController.FULL_MODE);
			}
		});
	}
	
	public void onMultiModeChanged(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (_surfaces != null) {
					for(ParticipantVideoSurface surfaceHolder : _surfaces.values()){
						if(mConferenceManager.isVideoRenderActive(surfaceHolder.mVideoView.getId()))
							surfaceHolder.setVisibility(View.VISIBLE);		
						else
							surfaceHolder.setVisibility(View.INVISIBLE);	
					}
				}
				mPreviewSurfaceHolder.setVisibility(View.VISIBLE);
				mVCParticipantsController.onModeUpdated(VCParticipantsController.MULTI_MODE);
			}
		});
	}
	
	public ParticipantHolder getParticipantHolder() {
		return ((mConferenceManager == null || mConferenceManager.getParticipantsManager() == null)? null : mConferenceManager.getParticipantsManager().getHolder());
	}
}
