package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codepath.apps.basictwitter.helpers.DateTimeUtils;

/**
 * Represents a tweet in Twitter land.
 * 
 * @author shine
 *
 */
public class Tweet implements Serializable {

	private static final long serialVersionUID = 2335164581715874653L;
	
	private long tid; //tweet id
	private String text; //tweet text
	private String createdAt; //tweet creation timestamp
	private User user; // the embedded user in the tweet
	
	public Tweet() { }
	
	public Tweet(String text, String createdAt, User user) {
		
		this.text = text;
		this.createdAt = createdAt;
		this.user = user;
	}
	
	public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray) {
		
		final ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());
		
		JSONObject tweetJson = null;
		for (int i = 0; i < jsonArray.length(); i++) {
			
			try {
				tweetJson = jsonArray.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			
			final Tweet tweet = Tweet.fromJson(tweetJson);
			if (tweet != null) {
				tweets.add(tweet);
			}
		}
		
		return tweets;
	}
	
	private static Tweet fromJson(JSONObject jsonObject) {
		
		Tweet tweet = null;
		try {
			tweet = new Tweet();
			tweet.tid = jsonObject.getLong("id");
			tweet.text = jsonObject.getString("text");
			tweet.createdAt = 
					DateTimeUtils.getRelativeTimeofTweet(jsonObject.getString("created_at"));
			tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
//			Log.d("debug", "Text: " + (tweet.text.length() > 50 ? tweet.text.substring(0, 50) : tweet.text) + ", user: " + tweet.user.getName());
//			Log.d("debug", ", createdAt: " + tweet.createdAt + ", tweet id: " + tweet.tid);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return tweet;
	}

	public long getTid() {
		return tid;
	}

	public String getText() {
		return text;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return getText() + " " + getUser().getScreenName();
	}
}
