package com.codepath.apps.basictwitter.fragments;

import java.util.ArrayList;

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
import com.codepath.apps.basictwitter.models.Tweet;

import eu.erikw.PullToRefreshListView;

/** 
 * The fragment to display the tweets on the current user's timeline.
 * 
 * @author shine
 *
 */
public abstract class TweetListFragment extends Fragment {
	
	protected ArrayList<Tweet> tweets;
	protected ArrayAdapter<Tweet> aTweets;
	protected PullToRefreshListView ptrlvTweets;
	
	protected TwitterClient twitterClient;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		twitterClient = TwitterApplication.getRestClient();
		
		tweets = new ArrayList<Tweet>();
		// XXX fix this! Use getActivity() sparingly.
		aTweets = new TweetArrayAdapter(getActivity(), tweets);
		
		Log.d("debug", "Fragment created.");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
		ptrlvTweets = (PullToRefreshListView) view.findViewById(R.id.ptrlvTweets);
		ptrlvTweets.setAdapter(aTweets);
		
		Log.d("debug", "Fragment view created.");
		
		return view;
	}
	
	/**
	 * Refresh the timeline on pull-to-refresh and compose tweet
	 */
	public abstract void refreshTimeline();
	
	/**
	 * Populate more tweets in timeline on infinite scroll
	 */
	public abstract void scrollTimeline();
	
}
