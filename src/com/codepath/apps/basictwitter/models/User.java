package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

/**
 * Represents a single user in Twitter land.
 * 
 * @author shine
 *
 */

@Table(name = "Users")
public class User extends Model implements Serializable {
	
	private static final long serialVersionUID = -613876000295656062L;
	
	@Column(unique = true, index = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	private long uid;
	@Column
	private String name;
	@Column(index = true)
	private String screenName;
	@Column
	private String profileImageUrl;
	@Column
	private long followersCount;
	@Column
	private long followingCount;
	@Column(name = "tagLine")
	private String description;
	
	public User() { 
		
		super();
	}
	
	public User(String name, String screenName, String profileImageUrl) {
		
		super();
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
			user.screenName = jsonObject.getString("screen_name");
			user.profileImageUrl = jsonObject.getString("profile_image_url");
			user.followersCount = jsonObject.getLong("followers_count");
			user.followingCount = jsonObject.getLong("friends_count");
			user.description = jsonObject.getString("description");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return user;
	}
	
	public static User getPersistedUser(User user) {
		
		final List<User> fetchedUsers = new Select().from(User.class)
				.where("uid = ?", user.getUid()).execute();
		if (fetchedUsers.size() == 1) {
			return fetchedUsers.get(0);
		}
		return null;
	}
	
	public static User getPersistedUserByScreenName(String screenName) {
		
		Log.d("debug", "Fetching user by screen name: " + screenName);
		final List<User> fetchedUsers = new Select().from(User.class)
				.where("screenName = ?", screenName).execute();
		if (fetchedUsers.size() == 1) {
			Log.d("debug", "Fetched user: " + fetchedUsers.get(0));
			return fetchedUsers.get(0);
		}
		return null;
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

	public long getFollowersCount() {
		return followersCount;
	}

	public long getFollowingCount() {
		return followingCount;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "User [uid=" + uid + ", name=" + name + ", screenName="
				+ screenName + ", profileImageUrl=" + profileImageUrl
				+ ", followersCount=" + followersCount + ", following="
				+ followingCount + ", description=" + description + "]";
	}
}
