package com.codepath.apps.basictwitter.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.format.DateUtils;
import android.util.Log;

/**
 * Utility methods for handling and date and time stamps. 
 * 
 * @author shine
 *
 */
public class DateTimeUtils {

	private static final String TWITTER_TIMESTAMP_FORMAT = 
			"EEE MMM dd HH:mm:ss ZZZZZ yyyy";
	
	// Return an absolute timestamp from twitter as "49 seconds ago"
	public static String getRelativeTimeofTweet(String tweetCreatedAt) {
		
		final SimpleDateFormat sdf = 
				new SimpleDateFormat(TWITTER_TIMESTAMP_FORMAT, Locale.ENGLISH);
		String relativeTime = "";
		
		try {
			long dateInMillis = sdf.parse(tweetCreatedAt).getTime();
			
			relativeTime = DateUtils.getRelativeTimeSpanString(dateInMillis, 
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
			
		} catch (ParseException e) {
			Log.d("info", "Couldn't parse relative timestamp for: " + tweetCreatedAt);
		}
		
		return formatRelativeTime(relativeTime);
	}
	
	// Return "49 seconds ago" as "49s"
	private static String formatRelativeTime(String fullRelativeDate) {
		
		final String relativeDateFormat = "(\\d+)\\s(\\w+)\\s\\w+";
		final Pattern relativeDatePattern = Pattern.compile(relativeDateFormat);
		
		final Matcher matcher = relativeDatePattern.matcher(fullRelativeDate);
		final StringBuilder sb = new StringBuilder();
		
		if (matcher.matches()) {
			return sb.append(matcher.group(1)).append(matcher.group(2).charAt(0)).toString();
		}
		
		// If the regex couldn't be parsed, make the relative timestamp manually
		// Should never happen though
		final String[] info = fullRelativeDate.split("\\s");
		sb.append(info[0].toString()).append(info[1].toString().substring(0, 1));
		return sb.toString();
	}
}
