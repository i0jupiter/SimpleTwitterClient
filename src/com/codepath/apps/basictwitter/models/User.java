package com.codepath.apps.basictwitter.models;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

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
		return "User [uid=" + uid + ", name=" + name + ", screenName="
				+ screenName + ", profileImageUrl=" + profileImageUrl + "]";
	}
}
