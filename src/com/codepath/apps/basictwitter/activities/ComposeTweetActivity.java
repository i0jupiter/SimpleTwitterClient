package com.codepath.apps.basictwitter.activities;

import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.TwitterApplication;
import com.codepath.apps.basictwitter.TwitterClient;
import com.codepath.apps.basictwitter.helpers.NetworkUtils;
import com.codepath.apps.basictwitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Activity screen that composes a tweet and posts it to Twitter.
 * 
 * @author shine
 *
 */
public class ComposeTweetActivity extends Activity {
	
	private static final int MAX_CHAR_LIMIT = 140;
	
	private TwitterClient twitterClient;
	
	private Button btnUpdateStatus;
	private Drawable roundCornersActive;
	private Drawable roundCornersInctive;
	private EditText etUpdateStatus;
	private ImageView ivProfileImage;
	private TextView tvTweetCharCount;
	private TextView tvUserName;
	private TextView tvUserScreenName;
	private User currentUser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose_tweet);
		// The compose tweet screen of Twitter app doesn't have an ActionBar
		getActionBar().hide();
		
		twitterClient = TwitterApplication.getRestClient();
		setupViews();
		
		setupUserAttributes();
		
		setupUpdateStatusListener();
	}

	public void onUpdateStatus(View v) {
		
		final long timeOfStatusUpdateInMS = new Date().getTime();
		final Intent statusUpdatedIntent = new Intent();
		
		// Check for Internet availability
		if (!NetworkUtils.isNetworkAvailable(this)) {
			Toast.makeText(this, "Please check your network connection.", 
					Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED, statusUpdatedIntent);
			finish();
		}
		
		// Internet is available. Prepare to post a new status update to Twitter
		final String newTweet = etUpdateStatus.getText().toString();
		
		postStatusUpdateAndReturnToTimeline(newTweet, 
				timeOfStatusUpdateInMS);
	}
	
	/* Private methods */
	
	private void setupViews() {
		
		roundCornersActive = getResources().getDrawable(R.drawable.round_corners_active);
		roundCornersInctive = getResources().getDrawable(R.drawable.round_corners_deactivated);
		
		btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
		etUpdateStatus = (EditText) findViewById(R.id.etUpdateStatus);
		ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
		ivProfileImage.setImageResource(android.R.color.transparent);
		tvTweetCharCount = (TextView) findViewById(R.id.tvTweetCharCount);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvUserScreenName = (TextView) findViewById(R.id.tvUserScreenName);
	}

	private void setupUserAttributes() {
		
		currentUser = User.getPersistedUserByScreenName(TwitterClient.USER_SCREEN_NAME);
		tvUserName.setText(currentUser.getName());
		tvUserScreenName.setText("@" + currentUser.getScreenName());
		final ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(currentUser.getProfileImageUrl(), ivProfileImage);
	}
	
	// Show char count as the user is updating the status
	private void setupUpdateStatusListener() {
		
		etUpdateStatus.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				// XXX Using deprecated methods is bad but will live with it for now.
				btnUpdateStatus.setBackgroundDrawable(roundCornersActive);
				btnUpdateStatus.setEnabled(true);
				final int remainingChars = MAX_CHAR_LIMIT - s.length(); 
				tvTweetCharCount.setText(Integer.toString(remainingChars));
				if (remainingChars < 11) {
					tvTweetCharCount.setTextColor(Color.RED);
				}
				if (remainingChars < 1) {
					btnUpdateStatus.setBackgroundDrawable(roundCornersInctive);
					btnUpdateStatus.setEnabled(false);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
	}

	// Send the new tweet to Twitter and return to TimelineActivity
	private void postStatusUpdateAndReturnToTimeline(final String newTweet, 
			final long timeOfUpdate) {
		
		twitterClient.updateStatus(new JsonHttpResponseHandler() {

			final Intent statusUpdatedIntent = new Intent();
			
			@Override
			public void onSuccess(JSONObject jsonObject) {
				
				Log.d("debug", "Tweet posted successfully: " + newTweet);
				Toast.makeText(getApplicationContext(), 
						"Tweet sent successfully!", Toast.LENGTH_SHORT).show();
				super.onSuccess(jsonObject);
				
				// Set result and close this activity
				setResult(RESULT_OK, statusUpdatedIntent);
				
				// Close this activity
				finish();
			}
			
			@Override
			public void onFailure(Throwable t, String s) {
				
				Log.d("debug", "Tweet post failed: " + newTweet);
				Toast.makeText(getApplicationContext(), 
						"Tweet could not be sent!", Toast.LENGTH_SHORT).show();
				super.onFailure(t, s);
				
				// If the tweet couldn't be posted, don't pass it to TimelineActivity
				setResult(RESULT_CANCELED, statusUpdatedIntent);
				
				// Close this activity
				finish();
			}
		
		}, "status", newTweet);
	}

}
