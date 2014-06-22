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
			
			relativeTime = getRelativeTimeFromMilliSeconds(dateInMillis);
			
		} catch (ParseException e) {
			Log.d("info", "Couldn't parse relative timestamp for: " + tweetCreatedAt);
		}
		
		return relativeTime;
	}

	public static String getRelativeTimeFromMilliSeconds(long dateInMillis) {
		
		final String relativeTime = DateUtils.getRelativeTimeSpanString(dateInMillis, 
				System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
		return formatRelativeTime(relativeTime);
	}
	
	// Return "49 seconds ago" as "49s"
	// Or "in 4 seconds" as "4s" (for newly composed tweets)
	private static String formatRelativeTime(String fullRelativeDate) {
		
		//Log.d("Debug", "Compose tweet time: " + fullRelativeDate);
		
		final String relativeDateFormat1 = "(\\d+)\\s(\\w+)\\s\\w+"; // "49 seconds ago"
		final Pattern relativeDatePattern1 = Pattern.compile(relativeDateFormat1); 
		final String relativeDateFormat2 = "(\\w+)\\s(\\d+)\\s(\\w+)"; // "in 4 seconds"
		final Pattern relativeDatePattern2 = Pattern.compile(relativeDateFormat2);
		
		final Matcher matcher1 = relativeDatePattern1.matcher(fullRelativeDate);
		final Matcher matcher2 = relativeDatePattern2.matcher(fullRelativeDate);
		final StringBuilder sb = new StringBuilder();
		
		if (matcher1.matches()) {
			return sb.append(matcher1.group(1)).append(matcher1.group(2).charAt(0)).toString();
		} else if (matcher2.matches()) {
			return sb.append(matcher2.group(2)).append(matcher2.group(3).charAt(0)).toString();
		}
		
		// If the regex couldn't be parsed, make the relative timestamp manually
		// Should never happen though
		final String[] info = fullRelativeDate.split("\\s");
		sb.append(info[0].toString()).append(info[1].toString().substring(0, 1));
		return sb.toString();
	}
}
