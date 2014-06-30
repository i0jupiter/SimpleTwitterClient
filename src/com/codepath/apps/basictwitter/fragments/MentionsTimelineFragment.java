package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.codepath.apps.basictwitter.helpers.NetworkUtils;
import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class MentionsTimelineFragment extends TweetListFragment {
	
	private long MAX_TWEET_ID = Long.MAX_VALUE;
	private long SINCE_TWEET_ID = 1L;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		
		// Check for Internet availability
//		if (!NetworkUtils.isNetworkAvailable(parentActivity)) {
//			Toast.makeText(parentActivity.getApplicationContext(), 
//					"No Internet connection found. Loading previously cached tweets.", 
//					Toast.LENGTH_LONG).show();
//			populateTimelineOffline();
//			return;
//		}
		
		// Clear the adapter before loading any tweets
		aTweets.clear();
		populateTimeline(false /* This is the first load of the dataset. Add as received. */,
				"since_id", "1");
	}
	
	// Get the current user's 'mentions' timeline
	@Override
	public void populateTimeline(final boolean addToTop, String... args) {

	    Log.d("debug", "Populating mentions timeline with params: " + args 
	    		+ " and adding tweets at the " + (addToTop == true ? "top." : "bottom."));

		twitterClient.getMentionsTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				final ArrayList<Tweet> tweetsLoadedInThisBatch = 
						Tweet.fromJsonArray(jsonArray);
				Log.d("debug", "Got #mentions: " + tweetsLoadedInThisBatch.size());

				if (!addToTop) {
					aTweets.addAll(tweetsLoadedInThisBatch);
					// As more past tweets are fetched, max_id will change
					MAX_TWEET_ID = determineMaxTweetId(MAX_TWEET_ID, tweetsLoadedInThisBatch);
				} else {
					addLatestTweetsToTimeline(tweetsLoadedInThisBatch);
					// As latest tweets are fetched, since_id will change
					SINCE_TWEET_ID = determineSinceTweetId(SINCE_TWEET_ID, tweetsLoadedInThisBatch);
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
	
	@Override
	public void refreshTimeline() {
		
		Log.d("debug", "Populating mentions upon pull to refresh: " + SINCE_TWEET_ID);
		populateTimeline(true /* add new tweets to top */,
				"since_id", Long.toString(SINCE_TWEET_ID));
	}
	
	@Override
	public void scrollTimeline() {
		
		Log.d("debug", "Populating more mentions upon infinite scroll: " + MAX_TWEET_ID);
		populateTimeline(false /* more tweets will be added at the bottom */,
				"max_id", Long.toString(MAX_TWEET_ID));
	}
}
