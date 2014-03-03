package com.winraguini.lyngoapp;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ActionBarActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_action_bar);
		setupTabs();
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
								LobbyFragment.class));

		actionBar.addTab(tabFirst);
		actionBar.selectTab(tabFirst);

		Tab tabSecond = actionBar
			.newTab()
			.setText("Chats")
			.setTabListener(
				new SherlockTabListener<ChatsFragment>(R.id.flContainer, this, "Second",
							ChatsFragment.class));

		actionBar.addTab(tabSecond);
	}

}
