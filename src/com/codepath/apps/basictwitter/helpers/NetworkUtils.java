package com.codepath.apps.basictwitter.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility methods to for network management.
 * 
 * @author shine
 *
 */
public class NetworkUtils {

	// Check network availability
	public static boolean isNetworkAvailable(Activity activity) {

		final ConnectivityManager connectivityManager = 
				(ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		
		//return false;
		return (activeNetworkInfo != null && activeNetworkInfo
				.isConnectedOrConnecting());
	}
}
