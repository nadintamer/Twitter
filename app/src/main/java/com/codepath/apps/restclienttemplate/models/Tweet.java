package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {
    public String body;
    public String createdAt;
    public String relativeTimestamp;
    public String exactTimestamp;
    public List<String> imageUrls;
    public User user;
    public int retweetCount;
    public int favoriteCount;
    public Boolean isRetweeted;
    public Boolean isFavorited;
    public Long id;

    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.id = jsonObject.getLong("id");
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.relativeTimestamp = getRelativeTimeAgo(tweet.createdAt);
        tweet.exactTimestamp = formatExactTimestamp(tweet.createdAt);
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.imageUrls = extractImageUrls(jsonObject);
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.isFavorited = jsonObject.getBoolean("favorited");
        tweet.isRetweeted = jsonObject.getBoolean("retweeted");
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            if (object.has("retweeted_status")) {
                tweets.add(Retweet.fromJson(object));
            } else {
                tweets.add(fromJson(object));
            }
        }
        return tweets;
    }

    private static final String TAG = "Tweet";
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    // code to get relative timestamp is adapted from: https://gist.github.com/nesquena/f786232f5ef72f6e10a7
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (ParseException e) {
            Log.e(TAG, "getRelativeTimeAgo failed", e);
        }

        return "";
    }

    // format exact timestamp to show on tweet detail view
    public static String formatExactTimestamp(String rawJsonDate) {
        DateFormat twitterFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy");
        try {
            Date result = twitterFormat.parse(rawJsonDate);
            SimpleDateFormat written = new SimpleDateFormat("HH:mm ??? dd.MM.yyyy");
            return written.format(result);
        } catch (ParseException e) {
            Log.e(TAG, "formatExactTimestamp failed", e);
        }

        return rawJsonDate;
    }

    // extract a list of image URLs from the given jsonObject
    public static List<String> extractImageUrls(JSONObject jsonObject) throws JSONException {
        List<String> imageUrls = new ArrayList<>();
        if (!jsonObject.has("extended_entities")) return imageUrls;
        JSONObject entities = jsonObject.getJSONObject("extended_entities");

        if (!entities.has("media")) return imageUrls;
        JSONArray media = entities.getJSONArray("media");
        for (int i = 0; i < media.length(); i++) {
            JSONObject currentMedia = media.getJSONObject(i);
            imageUrls.add(currentMedia.getString("media_url_https"));
        }

        return imageUrls;
    }
}
