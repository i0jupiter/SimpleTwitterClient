package com.codepath.apps.basictwitter.activities;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.helpers.NetworkUtils;
import com.codepath.apps.basictwitter.helpers.android.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.UserTimeline;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

/**
 * Main activity showing the Twitter timeline of the user.
 * 
 * @author shine
 *
 */
public class TimelineActivity extends Activity {

	private final int COMPOSE_TWEET_REQUEST_CODE = 100;
	
	private long MAX_TWEET_ID = Long.MAX_VALUE;
	private long SINCE_TWEET_ID = 1L;
	
	private TwitterClient twitterClient;

	private ArrayList<Tweet> tweets;
	private ArrayAdapter<Tweet> aTweets;
	private PullToRefreshListView ptrlvTweets;
	private UserTimeline currentUserTimeline;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		
		setupViews();
		
		// Check for Internet availability
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(this, "Please check your network connection.", Toast.LENGTH_LONG).show();
			return;
		}
		
		// Internet is available. Prepare to get timeline from Twitter
		// Get the current user's timeline first to fetch theie username/image profile URL, etc 
		// to be shown on the different screens in this app
		getCurrentUserTimeline(TwitterClient.getRequestParameters("screen_name", 
				TwitterClient.USER_SCREEN_NAME, "count", "1"));
		
		// Pull down to get latest tweets
		handlePullToRefresh();
		// Scroll down infinitely to load more tweets on the timeline
		handleTimelineScroll();
		
		// Clear the adapter before loading any tweets
		aTweets.clear();
		populateTimeline(TwitterClient.getRequestParameters("since_id", "1"), 
				false /* This is the first load of the dataset. Add as received. */);
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
		composeTweetIntent.putExtra("currentUser", currentUserTimeline.getUser());
		
		startActivityForResult(composeTweetIntent, COMPOSE_TWEET_REQUEST_CODE);
	}
	
	// If a new tweet was successfully composed, update the timeline.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.d("debug", "in onActivityResult: resultCode: " + requestCode + " resultCode: " + resultCode);
		
		if (resultCode == RESULT_OK && requestCode == COMPOSE_TWEET_REQUEST_CODE) {
			
			Log.d("debug", "Trying to refresh timeline after composing tweet: " + SINCE_TWEET_ID);
			populateTimeline(TwitterClient.getRequestParameters("since_id", 
					Long.toString(SINCE_TWEET_ID)), 
					true /* add new tweets to top */);
		}
	}
	
	
	/* Private methods */
	
	private void setupViews() {
		
		twitterClient = TwitterApplication.getRestClient();
		
		ptrlvTweets = (PullToRefreshListView) findViewById(R.id.ptrlvTweets);
		tweets = new ArrayList<Tweet>();
		aTweets = new TweetArrayAdapter(this, tweets);
		ptrlvTweets.setAdapter(aTweets);
	}
	
	// Get the current user's timeline
	private void getCurrentUserTimeline(RequestParams requestParams) {
		
		Log.d("debug", "Getting current user's timeline with params: " + requestParams);
		
		twitterClient.getCurrentUserTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				currentUserTimeline = UserTimeline.fromJsonArray(jsonArray).get(0);
				Log.d("debug", "Got current user's timeline successfully: " 
						+ currentUserTimeline.getUser().toString());
			}

			@Override
			public void onFailure(Throwable t, String s) {
				
				super.onFailure(t, s);
				Log.d("debug", t.toString());
				Log.d("debug", s);
			}
		}, requestParams);
	}
	
	// Get the current user's timeline
	private void populateTimeline(RequestParams requestParams, final boolean addToTop) {
		
	    Log.d("debug", "Populating timeline with params: " + requestParams 
	    		+ " and adding tweets at the " + (addToTop == true ? "top." : "bottom."));
		
		twitterClient.getHomeTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				final ArrayList<Tweet> tweetsLoadedInThisBatch = 
						Tweet.fromJsonArray(jsonArray);
				Log.d("debug", "Got #tweets: " + tweetsLoadedInThisBatch.size());
				
				if (!addToTop) {
					aTweets.addAll(tweetsLoadedInThisBatch);
					// As more past tweets are fetched, max_id will change
					determineMaxTweetId(tweetsLoadedInThisBatch);
				} else {
					addLatestTweetsToTimeline(tweetsLoadedInThisBatch);
					// As latest tweets are fetched, since_id will change
					determineSinceTweetId(tweetsLoadedInThisBatch);
				}
			}

			@Override
			public void onFailure(Throwable t, String s) {
				
				super.onFailure(t, s);
				Log.d("debug", t.toString());
				Log.d("debug", s);
			}
		}, requestParams);
	}
	
	// Pull down to get the latest tweets
	// Uses since_id
	private void handlePullToRefresh() {
		
		Log.d("debug", "Handling pull to refresh in timeline.");
		
		ptrlvTweets.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				
				Log.d("debug", "Populating tweets upon pull to refresh.");
				
				populateTimeline(TwitterClient.getRequestParameters("since_id", 
						Long.toString(SINCE_TWEET_ID)), 
						true /* add new tweets to top */);
				ptrlvTweets.onRefreshComplete();
			}
		});
	}
	
	// Infinite scroll to load more tweets from the past
	// Uses max_id
	private void handleTimelineScroll() {
	
		Log.d("debug", "Handling infinite scroll in timeline.");
		ptrlvTweets.setOnScrollListener(new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				
				Log.d("debug", "Populating tweets upon infinite scroll.");
				if (aTweets.isEmpty()) {
					Log.d("debug", "Timeline hasn't been loaded initially. Skip.");
					return;
				}
				
				populateTimeline(TwitterClient.getRequestParameters("max_id", 
						Long.toString(MAX_TWEET_ID)), false /* more tweets will be added at the bottom */);
			}
		});
	}
	
	// Determine the max_id so that the future infinite-scroll queries can be optimized.
	private void determineMaxTweetId(ArrayList<Tweet> tweetsInThisBatch) {
		
		// If no new tweets were loaded, we'll use the previously set value of max_id
		if (tweetsInThisBatch == null || tweetsInThisBatch.size() == 0) {
			return;
		}
		
		long minTweetId = Long.MAX_VALUE;
		for (Tweet tweet : tweetsInThisBatch) {
			if (tweet.getTid() < minTweetId) {
				minTweetId = tweet.getTid();
			}
		}
		
		MAX_TWEET_ID = --minTweetId;
		Log.d("Debug", "New max_id: " + MAX_TWEET_ID);
	}
	
	// Determine the since_id so that the future pull-to-refresh queries can be optimized.
	private void determineSinceTweetId(ArrayList<Tweet> tweetsInThisBatch) {
		
		// If no new tweets were loaded, we'll use the previously set value of max_id
		if (tweetsInThisBatch == null || tweetsInThisBatch.size() == 0) {
			return;
		}
		
		long maxTweetId = SINCE_TWEET_ID; // start from the previous value
		for (Tweet tweet : tweetsInThisBatch) {
			if (tweet.getTid() >= maxTweetId) {
				maxTweetId = tweet.getTid();
			}
		}
		
		SINCE_TWEET_ID = maxTweetId;
		Log.d("Debug", "New since_id: " + SINCE_TWEET_ID);
	}
	
	private void addLatestTweetsToTimeline(ArrayList<Tweet> latestTweets) {
		
		// Create a new (temp) ArrayList of tweets
		final ArrayList<Tweet> updatedTweetList = new ArrayList<Tweet>();
		// Add the latest tweets to it first
		updatedTweetList.addAll(latestTweets);
		// Then add the existing tweets
		updatedTweetList.addAll(tweets);
		
		// Clear the list associated with the adapter
		tweets.clear();
		// Add everything from the updated tweet list
		tweets.addAll(updatedTweetList);
		// Notify adapter of change in dataset
		aTweets.notifyDataSetChanged();
		
		// Clear the temp list to reduce data held in memory
		updatedTweetList.clear();
	}
}
