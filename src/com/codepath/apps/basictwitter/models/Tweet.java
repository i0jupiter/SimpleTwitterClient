package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.basictwitter.helpers.DateTimeUtils;

/**
 * Represents a tweet in Twitter land.
 * 
 * @author shine
 *
 */

@Table(name = "Tweets")
public class Tweet extends Model implements Serializable {

	private static final long serialVersionUID = 2335164581715874653L;
	
	@Column(unique = true, index = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long tid; //tweet id
	@Column
	private String text; //tweet text
	@Column
	private String createdAt; //tweet creation timestamp
	@Column
	private User user; // the embedded user in the tweet
	
	public Tweet() { 
		
		super();
	}
	
	public Tweet(String text, String createdAt, User user) {

		super();
		this.text = text;
		this.createdAt = createdAt;
		this.user = user;
	}
	
	/**
	 * Parse the JSON array of responses and persist them to the DB for offline access.
	 * 
	 * @param jsonArray -- JSON array of tweets
	 * @return -- ArrayList of {@link Tweet}
	 */
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
				tweet.user.save();
				tweet.save();
				tweets.add(tweet);
			}
		} //for
		
		return tweets;
		// Persist the newly fetched tweets and return them
		//return persistAndReturnTweets(tweets);
	}
	
	private static ArrayList<Tweet> persistAndReturnTweets(
			ArrayList<Tweet> tweetsToBePersisted) {
		
		final ArrayList<Tweet> persistedTweets = 
				new ArrayList<Tweet>(tweetsToBePersisted.size());
		
		if (tweetsToBePersisted == null || tweetsToBePersisted.size() == 0) {
			return persistedTweets;
		}
		
		ActiveAndroid.beginTransaction();
		try {
			for (Tweet tweet : tweetsToBePersisted) {
				// If the tweet exists, save it to the DB returning it in a list
				User tempUser = tweet.getUser();
				//Log.d("debug", "Inserting user: " + tempUser);
				tempUser.save();
				//Log.d("debug", "Inserting tweet: " + tweet);
				tweet.save();
//				final List<Tweet> fetchedTweets = 
//						new Select().from(Tweet.class).where("tid = ?", tweet.getTid()).execute();
//				Log.d("debug", "Fetched tweet: " + fetchedTweets.size() + " " + fetchedTweets.get(0).toString());
				// Also set the timestamp to relative before returning
				tweet.createdAt = DateTimeUtils.getRelativeTimeofTweet(tweet.getCreatedAt());
				persistedTweets.add(tweet);
			}
			ActiveAndroid.setTransactionSuccessful();
		} finally {
			ActiveAndroid.endTransaction();
		}
		
		checkTweetsPersisted(tweetsToBePersisted);
		tweetsToBePersisted.clear();
		return persistedTweets;
	}
	
	private static Tweet fromJson(JSONObject jsonObject) {
		
		Tweet tweet = null;
		try {
			tweet = new Tweet();
			tweet.tid = jsonObject.getLong("id");
			tweet.text = jsonObject.getString("text");
			tweet.createdAt = jsonObject.getString("created_at");
			tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
			
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
		return "Tweet [tid=" + tid + ", text=" + text + ", createdAt="
				+ createdAt + ", user=" + user + "]";
	}
	
	private static void checkTweetsPersisted(ArrayList<Tweet> tweetsExpectedToBePersisted) {
		
		for (Tweet tweet : tweetsExpectedToBePersisted) {
			
			final List<Tweet> fetchedTweets = 
					new Select().from(Tweet.class).where("tid = ?", tweet.getTid()).execute();
			Log.d("debug", "Fetched tweet: " + fetchedTweets.size() + " " + fetchedTweets.get(0).toString());
			
//			final List<User> fetchedUsers = 
//					new Select().from(User.class).where("uid = ?", tweet.getUser().getUid()).execute();
//			Log.d("debug", "Fetched user: " + fetchedUsers.size() + " " + fetchedUsers.get(0).toString());
		}
	}
	
}
