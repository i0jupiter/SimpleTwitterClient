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
import android.widget.ListView;
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

/**
 * Main activity showing the Twitter timeline of the user.
 * 
 * @author shine
 *
 */
public class TimelineActivity extends Activity {

	private final int COMPOSE_TWEET_REQUEST_CODE = 100;
	private static long MAX_TWEET_ID = Long.MAX_VALUE;
	
	private TwitterClient twitterClient;

	private ArrayList<Tweet> tweets;
	private ArrayAdapter<Tweet> aTweets;
	private ListView lvTweets;
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
		
		// Clear the adapter before loading any tweets
		aTweets.clear();
		populateTimeline(TwitterClient.getRequestParameters("since_id", "1"));
		
		handleTimelineScroll();
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
		
			final Tweet newTweet = 
					(Tweet) data.getSerializableExtra("newlyComposedTweet");
			
			Log.d("debug", "Came back to main activity: " + newTweet.toString());
			final ArrayList<Tweet> updatedTweetList = new ArrayList<Tweet>();
			updatedTweetList.add(newTweet);
			updatedTweetList.addAll(tweets);
			tweets.clear();
			tweets.addAll(updatedTweetList);
			updatedTweetList.clear();
			aTweets.notifyDataSetChanged();
		}
	}
	
	
	/* Private methods */
	
	private void setupViews() {
		
		twitterClient = TwitterApplication.getRestClient();
		lvTweets = (ListView) findViewById(R.id.lvTweets);
		tweets = new ArrayList<Tweet>();
		aTweets = new TweetArrayAdapter(this, tweets);
		lvTweets.setAdapter(aTweets);
	}
	
	// Get the current user's timeline
	private void getCurrentUserTimeline(RequestParams requestParams) {
		
		twitterClient.getCurrentUserTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				currentUserTimeline = UserTimeline.fromJsonArray(jsonArray).get(0);
				Log.d("debug", "Got timeline successfully: " + currentUserTimeline.getUser().toString());
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
	private void populateTimeline(RequestParams requestParams) {
		
		twitterClient.getHomeTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				final ArrayList<Tweet> tweetsLoadedInThisBatch = 
						Tweet.fromJsonArray(jsonArray);
				aTweets.addAll(tweetsLoadedInThisBatch);
				determineMinTweetId(tweetsLoadedInThisBatch);			
			}

			@Override
			public void onFailure(Throwable t, String s) {
				
				super.onFailure(t, s);
				Log.d("debug", t.toString());
				Log.d("debug", s);
			}
		}, requestParams);
	}
	
	// home_timeline endpoint already returns tweets in reverse chronological order.
	// It may not be necessary to determine the max_id by traversing the list of tweets
	// returned in this load.
	// But using this logic to be foolproof, anyway.
	private void determineMinTweetId(ArrayList<Tweet> tweetsInThisBatch) {
		
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
		Log.d("Debug", "Max tweet id: " + MAX_TWEET_ID);
	}
	
	private void handleTimelineScroll() {
		
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				
				populateTimeline(TwitterClient.getRequestParameters("max_id", 
						Long.toString(MAX_TWEET_ID)));
			}
		});
	}
}
