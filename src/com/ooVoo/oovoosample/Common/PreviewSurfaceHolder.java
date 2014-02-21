//
// PreviewSurfaceHolder.java
// 
// Created by ooVoo on July 22, 2013
//
// 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Common;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.winraguini.lyngoapp.R;
import com.ooVoo.oovoosample.VideoCall.DynamicAbsoluteLayout;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;

public class PreviewSurfaceHolder extends FrameLayout {

	public SurfaceView mPreviewView = null;
	public ImageView mNoVideoImageView = null;
	public TextView mUserInfo = null;
	public boolean isFullMode = false;
	
	public PreviewSurfaceHolder(Context context) {
		super(context);
	}

	public PreviewSurfaceHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PreviewSurfaceHolder(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mNoVideoImageView = (ImageView)findViewById(R.id.myAvatar);
		mPreviewView = (SurfaceView)findViewById(R.id.myVideoSurface);
		mUserInfo = (TextView)findViewById(R.id.preview_info);
	}

	public void updateUserStatusInfo(ConferenceCoreError eErrorCode){
		
	}

	public void updatePrivewParams(int left, int top, int previewWidth, int previewHeight) {
		DynamicAbsoluteLayout.LayoutParams view_params = (DynamicAbsoluteLayout.LayoutParams) getLayoutParams();
		view_params.x = (int) left;
		view_params.y = (int) top;
		view_params.width = (int) previewWidth;
		view_params.height = (int) previewHeight;
		setLayoutParams(view_params);
	}
}
