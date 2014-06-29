package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

public class UserTimelineFragment extends TweetListFragment {

	private ListView lvTimelineTweets;
	private String screenName;
	
	// Create this fragment through a singleton and pass the screen name
	// of the user whose timeline needs to be shown in the fragment.
	public static UserTimelineFragment newInstance(String screenName) {
		
		final UserTimelineFragment userTimelineFragment = 
				new UserTimelineFragment();
		final Bundle args = new Bundle();
		args.putString("screenName", screenName);
		userTimelineFragment.setArguments(args);
		return userTimelineFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		screenName = getArguments().getString("screenName");
	}
	
	// This is different from the base class, TweetListActivity, as it is not a
	// pull-to-refresh view. So, inflate the simple list view.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_timeline_tweet_list, container, false);
		lvTimelineTweets = (ListView) view.findViewById(R.id.lvTimelineTweets);
		lvTimelineTweets.setAdapter(aTweets);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		
		// Check for Internet availability
//		if (!NetworkUtils.isNetworkAvailable(getActivity())) {
//			Toast.makeText(getActivity().getApplicationContext(), 
//					"No Internet connection found. Loading previously cached tweets.", 
//					Toast.LENGTH_LONG).show();
//			populateTimelineOffline();
//			return;
//		}
		
		// Clear the adapter before loading any tweets
		aTweets.clear();
		populateTimeline(false, "screen_name", screenName, "include_rts", "1", "count", "20");
	}
	
	@Override
	public void populateTimeline(boolean addToTop, String... args) {
		
		Log.d("debug", "Getting user's timeline with params: " + args);
		
		twitterClient.getUserTimeline(new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, JSONArray jsonArray) {

				super.onSuccess(jsonArray);
				final ArrayList<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
				Log.d("debug", "Got user's timeline successfully: " + tweets.size());
				aTweets.addAll(tweets);
			}

			@Override
			public void onFailure(Throwable t, String s) {
				
				super.onFailure(t, s);
				Log.d("debug", t.toString());
				Log.d("debug", s);
			}
		}, args);
	}
	
	// These methods are not supported here!
	@Override
	protected void handlePullToRefresh() {
		
	}
	
	protected void handleTimelineScroll() {
		
	}
	
	@Override
	public void refreshTimeline() {
		
	}

	@Override
	public void scrollTimeline() {
		
	}
}
