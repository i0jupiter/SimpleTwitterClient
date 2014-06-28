package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.helpers.NetworkUtils;
import com.codepath.apps.basictwitter.helpers.android.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.UserTimeline;
import com.loopj.android.http.JsonHttpResponseHandler;

import eu.erikw.PullToRefreshListView.OnRefreshListener;

public class HomeTimelineFragment extends TweetListFragment {

	private long MAX_TWEET_ID = Long.MAX_VALUE;
	private long SINCE_TWEET_ID = 1L;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		Log.d("debug", "Sub-fragment created.");
		
		// Check for Internet availability
		if (!NetworkUtils.isNetworkAvailable(getActivity())) {
			Toast.makeText(getActivity().getApplicationContext(), 
					"No Internet connection found. Loading previously cached tweets.", 
					Toast.LENGTH_LONG).show();
			populateTimelineOffline();
			return;
		}
		
		// Internet is available. Prepare to get timeline from Twitter
		// Get the current user's timeline first to fetch their username/image profile URL, etc 
		// to be shown on the different screens in this app
		getCurrentUserTimeline("screen_name", TwitterClient.USER_SCREEN_NAME, "count", "1");
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		
		// Pull down to get latest tweets
		handlePullToRefresh();
		// Scroll down infinitely to load more tweets on the timeline
		handleTimelineScroll();
		
		// Clear the adapter before loading any tweets
		aTweets.clear();
		populateTimeline(false /* This is the first load of the dataset. Add as received. */,
				"since_id", "1");
	}
	
	@Override
	public void refreshTimeline() {
		
		Log.d("debug", "Populating tweets upon pull to refresh: " + SINCE_TWEET_ID);
		populateTimeline(true /* add new tweets to top */,
				"since_id", Long.toString(SINCE_TWEET_ID));
	}
	
	@Override
	public void scrollTimeline() {
		
		Log.d("debug", "Populating more tweets upon infinite scroll: " + SINCE_TWEET_ID);
		populateTimeline(false /* more tweets will be added at the bottom */,
				"max_id", Long.toString(MAX_TWEET_ID));
	}
	
	/* Private methods */
	
	// Get persisted tweets in DB
	private void populateTimelineOffline() {
		
		Log.d("debug", "Fetching tweets offline");
		
		aTweets.clear();
		aTweets.addAll(Tweet.fetchPersistedTweets());
	}

	// Get the current user's timeline
	private void getCurrentUserTimeline(String... args) {
		
		Log.d("debug", "Getting current user's timeline with params: " + args);
		
		twitterClient.getCurrentUserTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				final UserTimeline currentUserTimeline = 
						UserTimeline.fromJsonArray(jsonArray).get(0);
				Log.d("debug", "Got current user's timeline successfully: " 
						+ currentUserTimeline.getUser().toString());
			}

			@Override
			public void onFailure(Throwable t, String s) {
				
				super.onFailure(t, s);
				Log.d("debug", t.toString());
				Log.d("debug", s);
			}
		}, args);
	}
	
	// Pull down to get the latest tweets
	// Uses since_id
	private void handlePullToRefresh() {
		
		Log.d("debug", "Handling pull to refresh in timeline.");
		
		ptrlvTweets.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				
				refreshTimeline();
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
				
				scrollTimeline();
//				populateTimeline(false /* more tweets will be added at the bottom */,
//						"max_id", Long.toString(MAX_TWEET_ID));
				
//				populateTimeline(TwitterClient.getRequestParameters("max_id", 
//						Long.toString(MAX_TWEET_ID)), false /* more tweets will be added at the bottom */);
			}
		});
	}
	
	// Get the current user's timeline
	private void populateTimeline(final boolean addToTop, String... args) {

	    Log.d("debug", "Populating timeline with params: " + args 
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
		}, args);
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
	
}
