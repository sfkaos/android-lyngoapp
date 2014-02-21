//
// AlertsActivity.java
// 
// Created by ooVoo on July 22, 2013
//
// 2013 ooVoo, LLC.  Used under license. 
//
package com.ooVoo.oovoosample.Alerts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.oovoo.core.Utils.LogSdk;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.winraguini.lyngoapp.R;
import com.ooVoo.oovoosample.Common.AlertsManager;
import com.ooVoo.oovoosample.Common.IAlertsListener;

public class AlertsActivity extends Activity implements IAlertsListener {

	private static final String TAG = AlertsActivity.class.getSimpleName();
	private AlertsAdapter mAlertsAdapter = null;
	private ListView mAlertsListView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);		
		initView();
		LogSdk.d(TAG, "onCreate");	
	}
	
	private void initView(){
		setContentView(R.layout.alerts);
		mAlertsListView = (ListView) findViewById(R.id.alertsList);
		mAlertsAdapter = new AlertsAdapter();
		mAlertsListView.setDivider(null);
		mAlertsListView.setDividerHeight(0);
		mAlertsListView.setItemsCanFocus(false);
		mAlertsListView.setAdapter(mAlertsAdapter);
		mAlertsListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mAlertsListView.setStackFromBottom(true);
		 
		ActionBar ab = getActionBar();
		if(ab != null){
			ab.setHomeButtonEnabled(true);
			ab.setTitle(R.string.alerts);
			ab.setHomeButtonEnabled(true);
			ab.setDisplayShowTitleEnabled(true);
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayUseLogoEnabled(false);
			ab.setIcon(R.drawable.menu_ic_alerts);
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
	

	@Override
	protected void onResume() {
		super.onResume();
		LogSdk.d(TAG, "onResume");	
		mAlertsAdapter.updateAlerts(AlertsManager.getInstance().GetAlerts());		
		
		LogSdk.d(TAG, "onResume - registering to AlertsManager events");
		AlertsManager.getInstance().addListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogSdk.d(TAG, "onPause - de-registering to AlertsManager events");
		AlertsManager.getInstance().removeListener(this);
	}

	@Override
	public void OnAlert(final String alert) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LogSdk.d(TAG, "OnAlert - recieved alert:"+alert);
				addAlert(alert);
			}
		});		
	}

	synchronized void addAlert(final String alert) {	
		if(mAlertsAdapter != null){
			synchronized (mAlertsAdapter) {
				mAlertsAdapter.addAlert(alert);
				mAlertsAdapter.notifyDataSetChanged();
			}
		}
	}
	
	private class AlertsAdapter extends BaseAdapter{

		private List<String> mAlerts = new ArrayList<String>();
		
		public AlertsAdapter() {
			super();
		}
		
		public synchronized void updateAlerts(List<String> alerts){			
			if(alerts == null)
				mAlerts.clear();
			else 
				mAlerts = alerts;
		}
		
		public synchronized void addAlert(String alert){	
			if(mAlerts == null)
				mAlerts = new ArrayList<String>();
			mAlerts.add(alert);
		}

		@Override
		public synchronized int getCount() {
			return (mAlerts == null)? 0 : mAlerts.size(); // total number of elements in the list
		}

		@Override
		public synchronized String getItem(int i) {
//			LogSdk.d(TAG, "getItem: " + i + "/" + (mAlerts == null ? 0 : mAlerts.size()));
			return (mAlerts == null)? null : mAlerts.get(i); // single item in the list
		}
		
		@Override
		public synchronized long getItemId(int i) {
			return i; // index number
		}

		@Override
		public synchronized View getView(int index, View view, final ViewGroup parent) {
			AlertViewHolder viewHolder = null;
			try {
				if (view == null) {
					LayoutInflater inflater = LayoutInflater.from(parent.getContext());
					view = inflater.inflate(R.layout.alert_row_view, parent, false);
					viewHolder = new AlertViewHolder();
					viewHolder.textView = (TextView) view.findViewById(R.id.alert_row);
					view.setTag(viewHolder);			
				}
				else{
					viewHolder = (AlertViewHolder)view.getTag();
				}
				String alert = getItem(index);			
				viewHolder.textView.setText(alert);
			} catch( IllegalStateException ex) {
				ex.printStackTrace();
			}
			return view;
		}
		
		private class AlertViewHolder{
			TextView textView;
		}
	}
}
