package com.codepath.apps.restclienttemplate.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.Date;

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

    public static String formatNumber(int num) {
        if (num < 1000) {
            return String.valueOf(num);
        } else if (num < 10000) {
            String thousands = String.valueOf(num / 1000);
            String rest = String.valueOf(num % 1000);
            return String.format("%s.%s", thousands, rest);
        } else if (num < 1000000) {
            String thousands = String.valueOf(num / 1000);
            String hundreds = String.valueOf((num % 1000) / 100);
            return String.format("%s,%sK", thousands, hundreds);
        } else {
            String millions = String.valueOf(num / 1000000);
            String thousands = String.valueOf((num % 1000000) / 100000);
            return String.format("%s,%sM", millions, thousands);
        }
    }
}
