package com.winraguini.lyngoapp;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ActionBarActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_action_bar);
		setupTabs();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.action_bar, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		  switch (item.getItemId()) {
		    case R.id.profile_settings:
		    	showProfileSettings();
		      return true;
		    default:
		      return super.onOptionsItemSelected(item);
		  }
	}
	
	private void showProfileSettings() {
		Intent i = new Intent(this, UserDetailsActivity.class);
		startActivity(i);
	}

	private void setupTabs() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);

		Tab tabFirst = actionBar
			.newTab()
			.setText("People")
			.setTabListener(
				(TabListener) new SherlockTabListener<LobbyFragment>(R.id.flContainer, this, "First",
								LobbyFragment.class) {
					
				});

		actionBar.addTab(tabFirst);
		actionBar.selectTab(tabFirst);

		Tab tabSecond = actionBar
			.newTab()
			.setText("Chats")
			.setTabListener(
				new SherlockTabListener<ChatsFragment>(R.id.flContainer, this, "Second",
							ChatsFragment.class) {
					
				});

		actionBar.addTab(tabSecond);
	}

}
