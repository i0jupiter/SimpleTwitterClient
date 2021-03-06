package com.codepath.apps.basictwitter.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.basictwitter.R;
import com.codepath.apps.basictwitter.models.Tweet;
import com.codepath.apps.basictwitter.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Custom array adapter that holds a {@link Tweet} for 
 * display on the user's timeline.
 * 
 * @author shine
 *
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
	
	private ImageView ivProfileImage;
	private TextView tvUserName;
	private TextView tvUserScreenName;
	private TextView tvTweetTimestamp;
	private TextView tvTweetText;
	
	// Interface to listen to clicks on profile image from the fragment
	// (or activity) using this adapter.
	public interface TweetAdapterInterface {
		
		public void profileImageClicked(User user);
	}
	private TweetAdapterInterface profileImageClickListener;
	
	public TweetArrayAdapter(Context context, List<Tweet> tweets, 
			TweetAdapterInterface profileImageClickListener) {
		
		super(context, 0, tweets);
		this.profileImageClickListener = profileImageClickListener;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final Tweet tweet = getItem(position);
		View v;
		if (convertView == null) {
			
			final LayoutInflater inflater = LayoutInflater.from(getContext());
			v = inflater.inflate(R.layout.tweet_item, parent, false);
		} else {
			v = convertView;
		}
		
		setupViews(v);
		setTweetData(tweet);
		setupClickHandlers();
		return v;
	}
	
	/* Private methods */

	private void setupViews(View v) {
		
		ivProfileImage = (ImageView) v.findViewById(R.id.ivProfileImage);
		ivProfileImage.setImageResource(android.R.color.transparent);
		tvUserName = (TextView) v.findViewById(R.id.tvUserName);
		tvUserScreenName = (TextView) v.findViewById(R.id.tvUserScreenName);
		tvTweetTimestamp = (TextView) v.findViewById(R.id.tvTweetTimestamp);
		tvTweetText = (TextView) v.findViewById(R.id.tvTweetText);
	}
	
	private void setupClickHandlers() {
		
		ivProfileImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				profileImageClickListener.profileImageClicked((User) v.getTag());
			}
		});
	}
	
	private void setTweetData(final Tweet tweet) {
		
		final ImageLoader imageLoader = ImageLoader.getInstance();
		imageLoader.displayImage(tweet.getUser().getProfileImageUrl(), ivProfileImage);
		ivProfileImage.setTag(tweet.getUser());
		
		tvUserName.setText(tweet.getUser().getName());
		tvUserScreenName.setText("@" + tweet.getUser().getScreenName());
		tvTweetTimestamp.setText(tweet.getCreatedAt());
		tvTweetText.setText(tweet.getText());
	}
}
