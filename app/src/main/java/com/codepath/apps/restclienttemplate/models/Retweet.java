package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Retweet extends Tweet {
    private Tweet original;
    private User retweetedBy;

    public static Retweet fromJson(JSONObject jsonObject) throws JSONException {
        Retweet retweet = new Retweet();
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
