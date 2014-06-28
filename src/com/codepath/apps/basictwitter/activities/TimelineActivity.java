package com.codepath.apps.basictwitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.fragments.TweetListFragment;

/**
 * Main activity showing the Twitter timeline of the user.
 * 
 * @author shine
 *
 */
public class TimelineActivity extends FragmentActivity {

	private final int COMPOSE_TWEET_REQUEST_CODE = 100;
	
	private TweetListFragment tweetListFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		
		tweetListFragment = (TweetListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.fragment_timeline);
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
}
