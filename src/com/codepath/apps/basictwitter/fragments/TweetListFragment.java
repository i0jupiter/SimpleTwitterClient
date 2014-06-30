package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter;
import com.codepath.apps.basictwitter.adapters.TweetArrayAdapter.TweetAdapterInterface;
import com.codepath.apps.basictwitter.helpers.NetworkUtils;
import com.codepath.apps.basictwitter.listeners.EndlessScrollListener;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.codepath.apps.basictwitter.models.UserTimeline;
import com.loopj.android.http.JsonHttpResponseHandler;

import eu.erikw.PullToRefreshListView;
import eu.erikw.PullToRefreshListView.OnRefreshListener;

/** 
 * The fragment to display the tweets on the current user's timeline.
 * 
 * @author shine
 *
 */
public abstract class TweetListFragment 
		extends Fragment 
		implements TweetAdapterInterface {
	
	protected ArrayList<Tweet> tweets;
	protected ArrayAdapter<Tweet> aTweets;
	protected PullToRefreshListView ptrlvTweets;
	
	protected TwitterClient twitterClient;
	
	// Hold the activity of this fragment in this variable to avoid calling
	// getActivity().
	protected Activity parentActivity;
	
	// Interface to communicate profile image clicks to the activity of this fragment
	public interface OnProfileImageClickListener {
		
		public void onProfileImageClick(User user);
	}
	private OnProfileImageClickListener onProfileImageClickListener;
	
	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		parentActivity = activity;
		
		try {
			onProfileImageClickListener = (OnProfileImageClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement "
					+ "OnProfileImageClickListener");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		twitterClient = TwitterApplication.getRestClient();
		
		tweets = new ArrayList<Tweet>();
		aTweets = new TweetArrayAdapter(parentActivity, tweets, this /* pass this fragment to the
			adapter, so that it can listen to profile image clicks */);
		
		if (NetworkUtils.isNetworkAvailable(parentActivity)) {
			
			// Internet is available. Prepare to get timeline from Twitter
			// Get the current user's timeline first to fetch their username/image profile URL, etc 
			// to be shown on the different screens in this app
			getCurrentUserTimeline("screen_name", TwitterClient.USER_SCREEN_NAME, "count", "1");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
		ptrlvTweets = (PullToRefreshListView) view.findViewById(R.id.ptrlvTweets);
		ptrlvTweets.setAdapter(aTweets);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		
		// Pull down to get latest tweets
		handlePullToRefresh();
		// Scroll down infinitely to load more tweets on the timeline
		handleTimelineScroll();
	}
	
	/**
	 * Implement the functionality when a profile image is clicked in TweetArrayAdapter. 
	 * Basically pass off the user's screen name to the parent activity of this fragment
	 * so that another fragment can be launched from there. 
	 * Two fragments should only communicate via an activity and not be coupled together.
	 * 
	 * @param userScreenName -- the screen name of the user whose profile image was clicked
	 */
	@Override
	public void profileImageClicked(User user) {
		
		onProfileImageClickListener.onProfileImageClick(user);
	}
 	
	/**
	 * Populate timeline initially
	 */
	public abstract void populateTimeline(boolean top, String... args);
	
	/**
	 * Refresh the timeline on pull-to-refresh and compose tweet
	 */
	public abstract void refreshTimeline();
	
	/**
	 * Populate more tweets in timeline on infinite scroll
	 */
	public abstract void scrollTimeline();
	
	/**
	 * Pull down to get the latest tweets
	 * Uses since_id
	 */
	protected void handlePullToRefresh() {
		
		Log.d("debug", "Handling pull to refresh in timeline.");
		
		ptrlvTweets.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				
				// The subclasses should provide their own implementation to
				// fetch the latest data on pull-to-refresh in timeline
				refreshTimeline();
				ptrlvTweets.onRefreshComplete();
			}
		});
	}
			
	/**
	 * Infinite scroll to load more tweets from the past
	 * Uses max_id
	 */
	protected void handleTimelineScroll() {
	
		Log.d("debug", "Handling infinite scroll in timeline.");
		ptrlvTweets.setOnScrollListener(new EndlessScrollListener() {
			
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				
				Log.d("debug", "Populating tweets upon infinite scroll.");
				if (aTweets.isEmpty()) {
					Log.d("debug", "Timeline hasn't been loaded initially. Skipping.");
					return;
				}
				
				// The subclasses should provide their own implementation to
				// populate the timeline on infinite scroll
				scrollTimeline();
			}
		});
	}
	
	/** 
	 * Determine the max_id so that the future infinite-scroll queries can be optimized.
	 * 
	 * @param tweetsInThisBatch
	 * @return new max_id
	 */
	protected long determineMaxTweetId(long previousValue,
			ArrayList<Tweet> tweetsInThisBatch) {
		
		long minTweetId = previousValue; // start from the previous value
		
		// If no new tweets were loaded, we'll use the previously set value of max_id
		if (tweetsInThisBatch == null || tweetsInThisBatch.size() == 0) {
			return minTweetId;
		}
		
		for (Tweet tweet : tweetsInThisBatch) {
			if (tweet.getTid() < minTweetId) {
				minTweetId = tweet.getTid();
			}
		}
		
		--minTweetId;
		Log.d("Debug", "New max_id: " + minTweetId);
		return minTweetId;
	}
	
	/** 
	 * Determine the since_id so that the future pull-to-refresh queries can be optimized.
	 * 
	 * @param previousValue -- previous value of since_id
	 * @param tweetsInThisBatch
	 * @return new since_id
	 */
	protected long determineSinceTweetId(long previousValue, 
			ArrayList<Tweet> tweetsInThisBatch) {
		
		long maxTweetId = previousValue; // start from the previous value
		
		// If no new tweets were loaded, we'll use the previously set value of max_id
		if (tweetsInThisBatch == null || tweetsInThisBatch.size() == 0) {
			return previousValue;
		}
		
		for (Tweet tweet : tweetsInThisBatch) {
			if (tweet.getTid() >= maxTweetId) {
				maxTweetId = tweet.getTid();
			}
		}
		
		Log.d("Debug", "New since_id: " + maxTweetId);
		return maxTweetId;
	}
	
	/**
	 * Add a given list of tweets on top of the existing list
	 * 
	 * @param latestTweets
	 */
	protected void addLatestTweetsToTimeline(ArrayList<Tweet> latestTweets) {
		
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
	
	// Get the a given user's timeline
	protected void getCurrentUserTimeline(String... args) {
		
		Log.d("debug", "Getting user's timeline with params: " + args);
		
		twitterClient.getUserTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				final UserTimeline currentUserTimeline = 
						UserTimeline.fromJsonArray(jsonArray).get(0);
				Log.d("debug", "Got user's timeline successfully: " 
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
	
}
