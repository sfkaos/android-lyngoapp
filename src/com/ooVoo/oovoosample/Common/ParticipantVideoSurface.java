//
// ParticipantVideoSurface.java
// 
// Created by ooVoo on July 22, 2013
//
// 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.winraguini.lyngoapp.R;
import com.ooVoo.oovoosample.VideoCall.DynamicAbsoluteLayout;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;

public class ParticipantVideoSurface extends FrameLayout {
	
	public ImageView avatar;
	public android.opengl.GLSurfaceView mVideoView;
	public TextView nameBox;
	public TextView mVideoInfo;
	
	public ParticipantVideoSurface(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public ParticipantVideoSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ParticipantVideoSurface(Context context) {
		super(context);
	}

	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mVideoInfo = (TextView)findViewById(R.id.video_info);
		
	}

	public void showAvatar() {
		avatar.setVisibility(ImageView.VISIBLE);
		mVideoView.setVisibility(GLSurfaceView.INVISIBLE);
	}
	
	public void showUserStatusInfo(){
		mVideoInfo.setVisibility(View.VISIBLE);
	}

	public void hideUserStatusInfo(){
		mVideoInfo.setVisibility(View.GONE);
	}
	
	public void showEmptyCell() {
		avatar.setVisibility(ImageView.INVISIBLE);
		mVideoView.setVisibility(GLSurfaceView.INVISIBLE);
		nameBox.setVisibility(GLSurfaceView.INVISIBLE);
		mVideoInfo.setVisibility(View.GONE);
	}

	public void showVideo() {
		avatar.setVisibility(ImageView.INVISIBLE);
		mVideoView.setVisibility(GLSurfaceView.VISIBLE);
		mVideoInfo.setVisibility(View.GONE);
	}

	public void setName(String sParticipantId) {
		final String nameToShow = sParticipantId;
		nameBox.setVisibility(nameToShow.equals("") ? TextView.INVISIBLE : TextView.VISIBLE);
		nameBox.setText(nameToShow);
	}

	public void moveTo(float toXDelta, float toYDelta,
			float toWidth, float toHeight) {
		DynamicAbsoluteLayout.LayoutParams view_params = (DynamicAbsoluteLayout.LayoutParams) getLayoutParams();
		view_params.x = (int) toXDelta;
		view_params.y = (int) toYDelta;
		view_params.width = (int) toWidth;
		view_params.height = (int) toHeight;
		setLayoutParams(view_params);
	}

}
