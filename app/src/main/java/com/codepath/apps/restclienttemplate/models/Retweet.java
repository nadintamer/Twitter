package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

@Parcel
public class Retweet extends Tweet {
    private Tweet original;
    private User retweetedBy;

    public Retweet() {}

    public Retweet(Tweet tweet) {
        this.id = tweet.id;
        this.body = tweet.body;
        this.createdAt = tweet.createdAt;
        this.relativeTimestamp = tweet.relativeTimestamp;
        this.user = tweet.user;
        this.imageUrls = tweet.imageUrls;
        this.retweetCount = tweet.retweetCount;
        this.favoriteCount = tweet.favoriteCount;
        this.isFavorited = tweet.isFavorited;
        this.isRetweeted = tweet.isRetweeted;
    }

    public static Retweet fromJson(JSONObject jsonObject) throws JSONException {
        Retweet retweet = new Retweet(Tweet.fromJson(jsonObject));
        retweet.setOriginal(Tweet.fromJson(jsonObject.getJSONObject("retweeted_status")));
        retweet.setRetweetedBy(User.fromJson(jsonObject.getJSONObject("user")));
        return retweet;
    }

    public Tweet getOriginal() {
        return original;
    }

    public User getRetweetedBy() {
        return retweetedBy;
    }

    public void setOriginal(Tweet original) {
        this.original = original;
    }

    public void setRetweetedBy(User retweetedBy) {
        this.retweetedBy = retweetedBy;
    }
}
