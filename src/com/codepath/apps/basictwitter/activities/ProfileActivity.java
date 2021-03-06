package com.codepath.apps.basictwitter.activities;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.fragments.TweetListFragment;
import com.codepath.apps.basictwitter.fragments.UserTimelineFragment;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProfileActivity 
		extends FragmentActivity 
		implements TweetListFragment.OnProfileImageClickListener {
	
	private TwitterClient twitterClient;
	private String screenName = null;
	
	private ImageView ivProfileImage;
	private TextView tvUserName;
	private TextView tvUserDescription;
	private TextView tvFollowers;
	private TextView tvFollowing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		twitterClient = TwitterApplication.getRestClient();
		
		setupViews();
		
		getAndPopulateUserAttributes();
		getActionBar().setTitle("@" + screenName);
		
		// Create the timeline list fragment.
		// This is different from TweeetListFragment as it doesn't support infinite
		// scroll or pull-to-refresh.
		createUserTimelineFragment();
	}

	// When the profile image in the user timeline is clicked, nothing happens.
	@Override 
	public void onProfileImageClick(User user) {
		
	}

	/* Private methods */
	
	private void setupViews() {
		
		ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
		ivProfileImage.setImageResource(android.R.color.transparent);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvUserDescription = (TextView) findViewById(R.id.tvUserDescription);
		tvFollowers = (TextView) findViewById(R.id.tvFollowers);
		tvFollowing = (TextView) findViewById(R.id.tvFollowing);
	}
	
	// Determine if the current user's profile is being shown, or another's.
	// Populate the attributes accordingly.
	private void getAndPopulateUserAttributes() {
		
		final Bundle bundle = getIntent().getExtras();
		if (bundle == null || bundle.isEmpty()) {
			// No data was passed in intent. Show the current user's profile.
			getMyProfile(); // This gets the screen name, among other things
		} else {
			// A user was passed in. Populate their profile attributes.
			final User user = (User) getIntent().getSerializableExtra("user");
			populateProfileInfo(user);
			screenName = user.getScreenName();
		}
	}
	
	private void getMyProfile() {
		
		twitterClient.getMyProfile(new JsonHttpResponseHandler() {

			final Intent profileFetchedIntent = new Intent();
			
			@Override
			public void onFailure(Throwable arg0, JSONObject arg1) {
				
				Log.d("debug", "My profile couldn't be fetched: " + arg0.getMessage());
				super.onFailure(arg0, arg1);
				
				// If the tweet couldn't be posted, don't pass it to TimelineActivity
				setResult(RESULT_CANCELED, profileFetchedIntent);
				
				// Close this activity
				finish();
			}

			@Override
			public void onSuccess(JSONObject arg1) {
				
				Log.d("debug", "My profile fetched successfully.");
				super.onSuccess(arg1);
				
				final User me = User.fromJson(arg1);
				
				populateProfileInfo(me);
				
				screenName = me.getScreenName();
			}
		});
	}
	
	private void populateProfileInfo(User user) {
		
		final ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(user.getProfileImageUrl(), ivProfileImage);
		tvUserName.setText(user.getName());
		tvUserDescription.setText(user.getDescription());
		tvFollowers.setText(user.getFollowersCount() + " Followers");
		tvFollowing.setText(user.getFollowingCount() + " Following");
	}
	
	// Replace the placeholder fragment with the user timeline one
	// and pass the screen name to call the appropriate Twitter API.
	private void createUserTimelineFragment() {
		
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		final UserTimelineFragment userTimelineFragment = 
				UserTimelineFragment.newInstance(screenName);
		ft.replace(R.id.flContainer, userTimelineFragment);
		ft.commit();
	}
}
