package com.codepath.apps.restclienttemplate;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.facebook.stetho.Stetho;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     RestClient client = RestApplication.getRestClient(Context context);
 *     // use client to send requests to API
 *
 */
public class TwitterApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static TwitterClient getTwitterClient(Context context) {
        return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, context);
    }
}