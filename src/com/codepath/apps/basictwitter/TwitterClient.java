package com.codepath.apps.basictwitter;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	
    public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
    public static final String REST_URL = "https://api.twitter.com/1.1";
    public static final String REST_CONSUMER_KEY = "h0fUm64jJzpSXEdrA8pG1UeDR";
    public static final String REST_CONSUMER_SECRET = "meT3r8UWsFbkkcNeAK4qxPbA0xpuL6IG1c6dW4gm42L0B18rFY";
    public static final String REST_CALLBACK_URL = "oauth://cpbasictweets";
    public static final String USER_SCREEN_NAME = "RawFoodistsJour";
    
    public TwitterClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
    }
    
    public void getCurrentUserTimeline(AsyncHttpResponseHandler handler, 
    		String... args) {
    	
    	final String apiUrl = getApiUrl("statuses/user_timeline.json");
    	final RequestParams requestParams = getRequestParameters(args);
    	
    	Log.d("debug", "GET URL: " + apiUrl);
    	Log.d("debug", "params: " + requestParams.toString());
    	
    	client.get(apiUrl, requestParams, handler);
    }
    
    // Get the tweets on the user's timeline based on the different query parameters provided. 
    public void getHomeTimeline(AsyncHttpResponseHandler handler, 
    		String... args) {
    	
    	final String apiUrl = getApiUrl("statuses/home_timeline.json");
    	final RequestParams requestParams = getRequestParameters(args);
    	
    	Log.d("debug", "GET URL: " + apiUrl);
    	Log.d("debug", "params: " + requestParams.toString());
    	
    	client.get(apiUrl, requestParams, handler);
    }

    // Post a new tweet to Twitter
    public void updateStatus(AsyncHttpResponseHandler handler, 
    		String... args) {
    	
    	final String apiUrl = getApiUrl("statuses/update.json");
    	final RequestParams requestParams = getRequestParameters(args);
    	
    	if (requestParams == null) {
    		Log.d("warn", "No text received in status. Ignoring.");
    		return;
    	}
    	
    	Log.d("debug", "POST URL: " + apiUrl);
    	Log.d("debug", "params: " + requestParams.toString());

    	client.post(apiUrl, requestParams, handler);
    }
    
    // Get the current user's 'mentions' based on the different query parameters provided. 
    public void getMentionsTimeline(AsyncHttpResponseHandler handler, 
    		String... args) {
    	
    	final String apiUrl = getApiUrl("statuses/mentions_timeline.json");
    	final RequestParams requestParams = getRequestParameters(args);
    	
    	Log.d("debug", "GET URL: " + apiUrl);
    	Log.d("debug", "params: " + requestParams.toString());
    	
    	client.get(apiUrl, requestParams, handler);
    }
    
    private static RequestParams getRequestParameters(String... args) {
		
		// Parameters should always come in key-value pairs
		if ((args.length % 2) != 0) {
			return null;
		}
		
		final int numParams = args.length / 2 + 1;
		final RequestParams requestParams = new RequestParams();
		
		for (int i = 0; i < numParams; i += 2) {
			requestParams.put(args[i], args[i + 1]);
		}
		
		return requestParams;
	}
    
    // CHANGE THIS
    // DEFINE METHODS for different API endpoints here
//    public void getInterestingnessList(AsyncHttpResponseHandler handler) {
//        String apiUrl = getApiUrl("?nojsoncallback=1&method=flickr.interestingness.getList");
//        // Can specify query string params directly or through RequestParams.
//        RequestParams params = new RequestParams();
//        params.put("format", "json");
//        client.get(apiUrl, params, handler);
//    }
    
    /* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
     * 	  i.e getApiUrl("statuses/home_timeline.json");
     * 2. Define the parameters to pass to the request (query or body)
     *    i.e RequestParams params = new RequestParams("foo", "bar");
     * 3. Define the request method and make a call to the client
     *    i.e client.get(apiUrl, params, handler);
     *    i.e client.post(apiUrl, params, handler);
     */
}