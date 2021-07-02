package com.codepath.apps.restclienttemplate;

import android.content.Context;

import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;

public class TwitterClient extends OAuthBaseClient {
	public static final BaseApi REST_API_INSTANCE = TwitterApi.instance();
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;
	public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET;

	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

	public TwitterClient(Context context) {
		super(context, REST_API_INSTANCE,
				REST_URL,
				REST_CONSUMER_KEY,
				REST_CONSUMER_SECRET,
				null,  // OAuth2 scope, null for OAuth1
				String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
						context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
	}

	public void getHomeTimeline(Long maxId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		if (maxId != null) params.put("max_id", maxId);
		client.get(apiUrl, params, handler);
	}

	public void getUserTimeline(Long maxId, String screenName, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", 25);
		params.put("screen_name", screenName);
		if (maxId != null) params.put("max_id", maxId);
		client.get(apiUrl, params, handler);
	}

	public void publishTweet(String tweetContent, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", tweetContent);
		client.post(apiUrl, params, "", handler);
	}

	public void replyToTweet(String tweetContent, Long replyingTweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", tweetContent);
		params.put("in_reply_to_status_id", replyingTweetId);
		client.post(apiUrl, params, "", handler);
	}

	public void favoriteTweet(Long tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/create.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	public void unfavoriteTweet(Long tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/destroy.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	public void retweetTweet(Long tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl(String.format("statuses/retweet/%d.json", tweetId));
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	public void unretweetTweet(Long tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl(String.format("statuses/unretweet/%d.json", tweetId));
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	public void getSingleTweet(Long tweetId, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/show.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.get(apiUrl, params, handler);
	}

	public void getUserInformation(JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("account/verify_credentials.json");
		client.get(apiUrl, null, handler);
	}

	public void getFollowers(String screenName, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("followers/list.json");
		RequestParams params = new RequestParams();
		params.put("screen_name", screenName);
		client.get(apiUrl, params, handler);
	}

	public void getFollowing(String screenName, JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("friends/list.json");
		RequestParams params = new RequestParams();
		params.put("screen_name", screenName);
		client.get(apiUrl, params, handler);
	}
}
