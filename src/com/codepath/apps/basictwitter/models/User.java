package com.codepath.apps.basictwitter.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a single user in Twitter land.
 * 
 * @author shine
 *
 */
public class User implements Serializable {
	
	private static final long serialVersionUID = -613876000295656062L;
	
	private long uid;
	private String name;
	private String screenName;
	private String profileImageUrl;
	
	public User() { }
	
	public User(String name, String screenName, String profileImageUrl) {
		
		this.name = name;
		this.screenName = screenName;
		this.profileImageUrl = profileImageUrl;
	}
	
	public static User fromJson(JSONObject jsonObject) {
		
		User user = null;
		try {
			user = new User();
			user.uid = jsonObject.getLong("id");
			user.name = jsonObject.getString("name");
			// Append the '@' here so it doesn't have to be for every activity screen
			// It's easier to strip it when it's not needed, for those few and far-in-between cases.
			user.screenName = "@" + jsonObject.getString("screen_name");
			user.profileImageUrl = jsonObject.getString("profile_image_url");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}

	public long getUid() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public String getScreenName() {
		return screenName;
	}
	
	public String getProfileImageUrl() {
		return profileImageUrl;
	}
	
	@Override
	public String toString() {
		return name + " " + screenName;
	}
}
