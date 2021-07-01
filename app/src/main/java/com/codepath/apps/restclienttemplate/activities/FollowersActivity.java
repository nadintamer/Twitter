package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.FollowersAdapter;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityFollowersBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class FollowersActivity extends AppCompatActivity {

    ActivityFollowersBinding binding;
    EndlessRecyclerViewScrollListener scrollListener;
    List<User> followers;
    FollowersAdapter adapter;
    TwitterClient client;
    User user;
    String fetching;

    private static final String TAG = "FollowersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFollowersBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbar);
        fetching = getIntent().getStringExtra("fetching");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (fetching.equals("followers")) {
            getSupportActionBar().setTitle("Followers");
        } else {
            getSupportActionBar().setTitle("Following");
        }

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        client = TwitterApp.getTwitterClient(this);
        followers = new ArrayList<>();
        adapter = new FollowersAdapter(this, followers);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvFollowers.setLayoutManager(linearLayoutManager);
        binding.rvFollowers.setAdapter(adapter);

        if (fetching.equals("followers")) {
            populateFollowersList();
        } else {
            populateFollowingList();
        }
    }

    private void populateFollowersList() {
        client.getFollowers(user.screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess! " + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    adapter.clear();
                    adapter.addAll(User.fromJsonArray(jsonObject.getJSONArray("users")));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }

    private void populateFollowingList() {
        client.getFollowing(user.screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess! " + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    adapter.clear();
                    adapter.addAll(User.fromJsonArray(jsonObject.getJSONArray("users")));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }
}