package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Parcel
public class User {
    public String name;
    public String screenName;
    public String profilePictureUrl;
    public String bannerUrl;
    public String bio;
    public String location;
    public String joined;
    public int numFollowers;
    public int numFollowing;

    public User() {}

    public static User fromJson(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profilePictureUrl = jsonObject.getString("profile_image_url_https");
        if (jsonObject.has("profile_banner_url")) {
            user.bannerUrl = jsonObject.getString("profile_banner_url");
        } else {
            user.bannerUrl = "https://www.schemecolor.com/wallpaper?i=4334&og"; // default twitter blue banner
        }
        user.location = jsonObject.getString("location");
        user.bio = jsonObject.getString("description");
        user.joined = jsonObject.getString("created_at");
        user.numFollowers = jsonObject.getInt("followers_count");
        user.numFollowing = jsonObject.getInt("friends_count");
        return user;
    }

    public static List<User> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<User> followers = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            followers.add(fromJson(object));
        }
        return followers;
    }
}
