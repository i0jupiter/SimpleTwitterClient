package com.codepath.apps.basictwitter.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.fragments.HomeTimelineFragment;
import com.codepath.apps.basictwitter.fragments.MentionsTimelineFragment;
import com.codepath.apps.basictwitter.fragments.TweetListFragment;
import com.codepath.apps.basictwitter.listeners.FragmentTabListener;

/**
 * Main activity showing the Twitter timeline of the user.
 * 
 * @author shine
 *
 */
public class TimelineActivity extends FragmentActivity {

	private final int COMPOSE_TWEET_REQUEST_CODE = 100;
	
	private ActionBar actionBar;
	private Tab homeTab;
	private Tab mentionsTab;
	private TweetListFragment tweetListFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		setupTabs();
		
		tweetListFragment = 
				(TweetListFragment) getSupportFragmentManager().findFragmentByTag("Home");
	}

	// Create the action bar for this activity
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.timeline_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	// Show ComposeTweetActivity on click of Compose in ActionBar
	public void onComposeTweet(MenuItem mi) {
		
		final Intent composeTweetIntent = 
				new Intent(TimelineActivity.this, ComposeTweetActivity.class);
		
		startActivityForResult(composeTweetIntent, COMPOSE_TWEET_REQUEST_CODE);
	}
	
	// If a new tweet was successfully composed, update the timeline.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.d("debug", "in onActivityResult: resultCode: " + requestCode + " resultCode: " + resultCode);
		
		if (resultCode == RESULT_OK && requestCode == COMPOSE_TWEET_REQUEST_CODE) {
			
			Log.d("debug", "Trying to refresh timeline after composing tweet.");
			tweetListFragment.refreshTimeline();
		}
	}
	
	
	/* Private methods */
	
	private void setupTabs() {
		
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		homeTab = actionBar
		    .newTab()
		    .setText("Home")
		    .setTabListener(new FragmentTabListener<HomeTimelineFragment>(R.id.flContainer, this,
                        "Home", HomeTimelineFragment.class));

		actionBar.addTab(homeTab);
		actionBar.selectTab(homeTab);

		mentionsTab = actionBar
		    .newTab()
		    .setText("Mentions")
		    .setTabListener(new FragmentTabListener<MentionsTimelineFragment>(R.id.flContainer, this,
                        "Mentions", MentionsTimelineFragment.class));
		actionBar.addTab(mentionsTab);
	}
}
