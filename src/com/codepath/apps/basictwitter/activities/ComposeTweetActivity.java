package com.codepath.apps.basictwitter.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import com.codepath.apps.basictwitter.models.Tweet;
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
		
		twitterClient = TwitterApplication.getRestClient();
		setupViews();
		
		setupUserAttributes();
		
		setupUpdateStatusListener();
	}

	public void onUpdateStatus(View v) {
		
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
		
		postStatusUpdateAndReturnToTimeline(newTweet);
	}
	
	/* Private methods */
	
	private void setupViews() {
		
		btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
		etUpdateStatus = (EditText) findViewById(R.id.etUpdateStatus);
		ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
		ivProfileImage.setImageResource(android.R.color.transparent);
		tvTweetCharCount = (TextView) findViewById(R.id.tvTweetCharCount);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvUserScreenName = (TextView) findViewById(R.id.tvUserScreenName);
	}

	private void setupUserAttributes() {
		
		currentUser = (User) getIntent().getExtras().get("currentUser");
		tvUserName.setText(currentUser.getName());
		tvUserScreenName.setText(currentUser.getScreenName());
		final ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(currentUser.getProfileImageUrl(), ivProfileImage);
	}
	
	// Show char count as the user is updating the status
	private void setupUpdateStatusListener() {
		
		etUpdateStatus.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				btnUpdateStatus.setEnabled(true);
				final int remainingChars = MAX_CHAR_LIMIT - s.length(); 
				tvTweetCharCount.setText(Integer.toString(remainingChars));
				if (remainingChars < 11) {
					tvTweetCharCount.setTextColor(Color.RED);
				}
				if (remainingChars < 1) {
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
	private void postStatusUpdateAndReturnToTimeline(final String newTweet) {
		
		twitterClient.updateStatus(new JsonHttpResponseHandler() {

			final Intent statusUpdatedIntent = new Intent();
			
			@Override
			public void onSuccess(JSONObject jsonObject) {
				
				// If the post was successful, pass it to TimelineActivity for
				// immediate display (without lag).
				final Tweet newlyComposedTweet = new Tweet(newTweet, "0s", currentUser);
				statusUpdatedIntent.putExtra("newlyComposedTweet", newlyComposedTweet);
				// Set result and close this activity
				setResult(RESULT_OK, statusUpdatedIntent);
				
				Log.d("debug", "Tweet posted successfully: " + newTweet);
				Toast.makeText(getApplicationContext(), 
						"Tweet sent successfully!", Toast.LENGTH_SHORT).show();
				super.onSuccess(jsonObject);
				
				// Close this activity
				finish();
			}
			
			@Override
			public void onFailure(Throwable t, String s) {
				
				// If the tweet couldn't be posted, don't pass it to TimelineActivity
				setResult(RESULT_CANCELED, statusUpdatedIntent);
				
				Log.d("debug", "Tweet post failed: " + newTweet);
				Toast.makeText(getApplicationContext(), 
						"Tweet could not be sent!", Toast.LENGTH_SHORT).show();
				super.onFailure(t, s);
				
				// Close this activity
				finish();
			}
		
		}, TwitterClient.getRequestParameters("status", newTweet));
	}

}
