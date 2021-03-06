//
// ParticipantHolder.java
// 
// Created by ooVoo on July 22, 2013
//
// © 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.opengl.GLSurfaceView;
import android.text.TextUtils;
import com.oovoo.core.Utils.LogSdk;
import android.util.SparseArray;

import com.ooVoo.oovoosample.SessionUIPresenter;
import com.oovoo.core.ConferenceCore;
import com.oovoo.core.ClientCore.VideoChannelPtr;
import com.oovoo.core.ui.VideoRenderer;

public class ParticipantHolder {
	
	public static final short VIDEO_ON 				= 0;
	public static final short VIDEO_MUTED_BY_USER 	= 1;
	public static final short VIDEO_PAUSED_DUE_QOS 	= 2;
	
	public class RenderViewData {
		private int _view_id;
		private Boolean _video_on;
		private VideoRenderer _render;
		private String _user;
		private boolean isFullMode = false;
		
		public RenderViewData(int view_id) {
			_view_id = view_id;
			_video_on = false;
			_render = null;
			_user = null;
		}

		public Boolean isVideoOn() {
			return _video_on;
		}

		public void setVideoStateOn(Boolean on, short state) {
			_video_on = on;
		}
		
		public void updateVideoState(short state){
		}
	}

	public class VideoParticipant {
		private String _participantId;
		private String _opaqueString;
		private Integer _viewId;

		public VideoParticipant(String sParticipantId, String sOpaqueString, Integer viewId) {
			setOpaqueString(sOpaqueString);
			setViewId(viewId);
			setParticipantId(sParticipantId);
		}

		String getOpaqueString() {
			return _opaqueString;
		}

		void setOpaqueString(String _opaqueString) {
			this._opaqueString = _opaqueString;
		}

		Integer getViewId() {
			return _viewId;
		}

		void setViewId(Integer _viewId) {
			this._viewId = _viewId;
		}

		public String getParticipantId() {
			return _participantId;
		}

		public void setParticipantId(String _participantId) {
			this._participantId = _participantId;
		}
	}

	private static final String TAG = ParticipantHolder.class.getSimpleName();
	private SparseArray<RenderViewData> _renders = new SparseArray<RenderViewData>();
	private Map<String, VideoParticipant> _users = new HashMap<String, VideoParticipant>();
	private boolean _on_pause = true;
	private boolean _on_full_mode = false;
	

	public int getNumOfVideosOn() {
		int rc = 0;
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			if (d.isVideoOn())
				rc++;
		}
		return rc;
	}
	
	public int getActiveRenders() {
		int rc = 0;
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			if (!TextUtils.isEmpty(d._user))
				rc++;
		}
		return rc;
	}
	

	public Collection<VideoParticipant> getParticipants() {
		return _users.values();
	}

	public void setVideoStateOn(String participant, Boolean on, short state) {
		Integer view_id = _users.get(participant).getViewId();
		if (view_id != null && view_id != -1) {
			_renders.get(view_id).setVideoStateOn(on, state);
		}
	}

	public boolean isRenderActive(int viewId) {
		RenderViewData mRenderViewData = _renders.get(viewId);
		if (mRenderViewData != null && mRenderViewData._user != null)
			return true;
		return false;
	}

	public Boolean isVideoOn(String participant) {
		Integer view_id = _users.get(participant).getViewId();
		if (view_id != null && view_id != -1) {
			return _renders.get(view_id).isVideoOn();
		}
		return false;
	}

	public void addGLView(int view_id) {
		_renders.put(view_id, new RenderViewData(view_id));
		LogSdk.d(TAG, "adding GLview " + view_id + " total = " + _renders.size());
	}

	public String findRenderIdByViewId(int view_id) {

		for (int i = 0; i < _renders.size(); i++) {
			int key = _renders.keyAt(i);
			if (key == view_id) {
				RenderViewData data = _renders.valueAt(i);
				return data._user;
			}
		}
		return null;
	}

	public void updateGLViews(SessionUIPresenter presenter) {
//		_on_pause = false;
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			LogSdk.d(TAG, "updating GLview " + d._view_id + " for user " + d._user);

			GLSurfaceView glview = (GLSurfaceView) presenter.findViewById(d._view_id);
			if (glview == null) {
				LogSdk.e(TAG, "NULL GL view!!! " + d._view_id);
				continue;
			}
			try {
				glview.setEGLContextClientVersion(2);
				d._render = new VideoRenderer(glview);
				glview.setRenderer(d._render);
				glview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);// .RENDERMODE_CONTINUOUSLY);
			} catch (IllegalStateException e) {
			}
		}
	}

	public String getParticipantByViewId(int view_id) {
		String user = null;
		for (Map.Entry<String, VideoParticipant> e : _users.entrySet()) {
			if (e.getValue().getViewId() == view_id) {
				user = e.getKey();
				break;
			}
		}
		return user;
	}

	public void moveToFullMode(int viewIdForFullMode) {
		_on_full_mode = true;
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			if (d._view_id != viewIdForFullMode) {
				String user = getParticipantByViewId(d._view_id);
				LogSdk.d(TAG, "pausing GLview " + d._view_id + " for user " + user);
				if (d._video_on) {					
					ConferenceCore.instance().receiveParticipantVideoOff(user);
					LogSdk.d(TAG, "send turn video Off for user " + user);
				}
				d.isFullMode = false;
			} else {
				d.isFullMode = true;
			}
		}
	}

	public void moveToMultiMode(int viewIdForFullMode) {
		_on_full_mode = false;
		int numActiveRenders = 0;
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			if (d._view_id != viewIdForFullMode) {
				d.isFullMode = false;
				if (d._video_on && d._user != null) {
					numActiveRenders++;
					ConferenceCore.instance().receiveParticipantVideoOn(d._user);
					LogSdk.d(TAG, "resume GLview " + d._view_id + " for user "
							+ d._user);
				}
			}
			else{
				numActiveRenders++;
			}
		}
		
		if(numActiveRenders < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL && _users.size() > numActiveRenders){
			fillEmptyRenders();
		}
	}
	
	private void fillEmptyRenders(){
		for (Map.Entry<String, VideoParticipant> e : _users.entrySet()) {
			VideoParticipant participant = e.getValue();
			if (participant.getViewId() == -1) {				
				RenderViewData d = getFreeRender();
				if (d != null){
					participant.setViewId(d._view_id);
					LogSdk.i(Utils.getOoVooTag(), "Add free render for " + participant._participantId + " as " + d._view_id);					
					d._video_on = true;
					d._user = participant.getParticipantId();
					ConferenceCore.instance().receiveParticipantVideoOn(participant.getParticipantId());					
				} else {
					break;
				}
			}
		}	
	}

	public void Pause() {
		LogSdk.d(TAG, "Pause: on_pause=" + _on_pause);
		_on_pause = true;
		
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			String user = getParticipantByViewId(d._view_id);

			LogSdk.d(TAG, "pausing GLview " + d._view_id + " for user " + user);
			if (d._video_on) {
				ConferenceCore.instance().receiveParticipantVideoOff(user);
				LogSdk.d(TAG, "send turn video Off for user " + user);
				d.updateVideoState(VIDEO_PAUSED_DUE_QOS);
				
				// d._render.channel().Disconnect();
				// d._render = null;

			}
		}
	}

	public void addParticipant(String sParticipantId, String sOpaqueString) {
		if (_users.containsKey(sParticipantId)) {
			LogSdk.w(TAG, "add Participant failed! Participant " + sParticipantId + " already exist");
		}
		_users.put(sParticipantId, new VideoParticipant(sParticipantId, sOpaqueString, -1));			
	}
	
	public int prepareParticipantAsActiveRender(String sParticipantId){
		int numActiveRenders = getActiveRenders();
		LogSdk.i(Utils.getOoVooTag(), "Active renders = " + numActiveRenders);	
		if((!_on_full_mode && numActiveRenders < ParticipantsManager.MAX_ACTIVE_PARTICIPANTS_IN_CALL)){
			LogSdk.i(Utils.getOoVooTag(), "Add joined user to active renders " + sParticipantId);			
			RenderViewData d = getFreeRender();
			if (d != null){
				_users.get(sParticipantId).setViewId(d._view_id);
				LogSdk.i(Utils.getOoVooTag(), "Add free render for " + sParticipantId + " as " + d._view_id);					
				d._video_on = false;
				d._user = sParticipantId;
				return d._view_id;
			} else {
				LogSdk.i(Utils.getOoVooTag(), "No available GLView found for Participant " + sParticipantId);
			}
		}
		else{
			LogSdk.i(Utils.getOoVooTag(), "ConferenceManager.OnParticipantJoinedSession - can't add user to active surfaces");			
		}
		return -1;
	}

	private RenderViewData getFreeRender() {
		RenderViewData rd = null;
		LogSdk.i(Utils.getOoVooTag(), "RENDERS Size = " + _renders.size());
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
//			if (!d._video_on && d._user == null) {
			if (TextUtils.isEmpty(d._user)) {
				rd = d;
				break;
			}
		}
		return rd;
	}

	public boolean removeParticipant(String sParticipantId) {
		boolean updateFullMode = false;
		if (!_users.containsKey(sParticipantId)) {
			LogSdk.e(TAG, "remove Participant failed! Participant "
					+ sParticipantId + " not found");
			return updateFullMode;
		}
		Integer view_id = _users.get(sParticipantId).getViewId();
		if (view_id == -1) {
			LogSdk.d(TAG, "Participant " + sParticipantId
					+ " is not connected to any GLView");
		} else {
			RenderViewData d = _renders.get(view_id);
			updateFullMode = d.isFullMode;
			d._render.disconnect();
			d._video_on = false;
			d._user = null;
		}
		_users.remove(sParticipantId);

		if (updateFullMode) {
			_on_full_mode = true;
			for (int i = 0; i < _renders.size(); i++) {
				RenderViewData d = _renders.valueAt(i);
				if (d._view_id != view_id) {
					String user = getParticipantByViewId(d._view_id);
					LogSdk.d(TAG, "pausing GLview " + d._view_id + " for user "
							+ user);
					if (d._video_on) {
						ConferenceCore.instance().receiveParticipantVideoOff(user);
						LogSdk.d(TAG, "send turn video Off for user " + user);
					}
				} else {
					d.isFullMode = false;
				}
			}
		}
		LogSdk.d(TAG, "remove Participant " + sParticipantId + " GLView "
				+ view_id + " is free.");
		return updateFullMode;
	}

	public boolean turnVideoOn(String sParticipantId, VideoChannelPtr in) {
		if (!_users.containsKey(sParticipantId)) {
			LogSdk.e(TAG, "turnVideoOn: Participant " + sParticipantId
					+ " not found");
			return false;
		}

		RenderViewData d = null;
		Integer view_id = _users.get(sParticipantId).getViewId();
		if (view_id == -1) {
			LogSdk.d(TAG, "Participant " + sParticipantId
					+ " is not connected to any GLView");
			d = getFreeRender();
			if (d != null)
				_users.get(sParticipantId).setViewId(d._view_id);

		} else {
			d = _renders.get(view_id);
		}

		if (d != null) {
			d._render.connect(in, sParticipantId);
			d._video_on = true;
			d._user = sParticipantId;
			LogSdk.d(TAG, "video is ON for " + sParticipantId + " GLView "
					+ d._view_id);
			return true;
		} else {
			LogSdk.w(TAG, "No available GVView found for Participant "
					+ sParticipantId);
		}
		return false;
	}

	public void turnVideoOff(String sParticipantId) {
		if (!_users.containsKey(sParticipantId)) {
			LogSdk.e(TAG, "Participant " + sParticipantId + " not found");
			return;
		}

		Integer view_id = _users.get(sParticipantId).getViewId();
		if (view_id == -1) {
			LogSdk.w(TAG, "No GLview found for participant " + sParticipantId);
			return;
		}

		RenderViewData d = _renders.get(view_id);
		if (d != null) {
			if( d._render != null)
				d._render.disconnect();
			if (!_on_pause && !_on_full_mode) {
				d._video_on = false;
				d._user = null;
			}
			LogSdk.d(TAG, "video is OFF for " + sParticipantId);
		} else
			LogSdk.w(TAG, "turnVideoOff: No GVView found for Participant "
					+ sParticipantId);
	}

	public void Resume() {
		LogSdk.d(TAG, "Resume: on_pause=" + _on_pause);
		_on_pause = false;
		
		for (int i = 0; i < _renders.size(); i++) {
			RenderViewData d = _renders.valueAt(i);
			if ((d._video_on && d._user != null) && (!_on_full_mode || (_on_full_mode && d.isFullMode))) {
				ConferenceCore.instance().receiveParticipantVideoOn(d._user);
				LogSdk.d(TAG, "resume GLview " + d._view_id + " for user " + d._user);
			}
		}
	}

	public int getViewIdByParticipant(String sParticipantId) {
		if (!_users.containsKey(sParticipantId)) {
			return -1;
		}

		Integer view_id = _users.get(sParticipantId).getViewId();
		return view_id;

	}

	public void clear() {
		this._renders.clear();
		this._users.clear();
		_on_full_mode = false;
		_on_pause = false;
	}

	public VideoParticipant getParticipant(String sParticiapntId) {
		return _users.get(sParticiapntId);
	}
	
	public boolean isFullMode(){
		return _on_full_mode;
	}
	
	

}
