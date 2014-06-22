package com.codepath.apps.basictwitter.models;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a user's timeline in Twitter land.
 * 
 * @author shine
 *
 */
public class UserTimeline implements Serializable {
	
	private static final long serialVersionUID = 7275832277645874088L;
	
	private User user; // the embedded user in the timeline
	
	public UserTimeline() { }
	
	public UserTimeline(User user) {
		this.user = user;
	}
	
	public static ArrayList<UserTimeline> fromJsonArray(JSONArray jsonArray) {
		
		final ArrayList<UserTimeline> timelines = 
				new ArrayList<UserTimeline>(jsonArray.length());
		
		JSONObject timelineJson = null;
		for (int i = 0; i < jsonArray.length(); i++) {
			
			try {
				timelineJson = jsonArray.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			
			final UserTimeline timeline = UserTimeline.fromJson(timelineJson);
			if (timeline != null) {
				timelines.add(timeline);
			}
		}
		
		return timelines;
	}
	
	private static UserTimeline fromJson(JSONObject jsonObject) {
		
		UserTimeline userTimeline = null;
		try {
			userTimeline = new UserTimeline();
			userTimeline.user = User.fromJson(jsonObject.getJSONObject("user"));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return userTimeline;
	}
	
	public User getUser() {
		return user;
	}

	@Override
	public String toString() {
		return getUser().toString();
	}

}
