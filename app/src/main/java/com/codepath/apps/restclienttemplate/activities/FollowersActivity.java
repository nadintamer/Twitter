package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.util.StringUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import org.apache.commons.codec.binary.StringUtils;
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
    MenuItem miActionProgressItem;

    private static final String TAG = "FollowersActivity";
    // response handler for fetching followers/following lists
    private JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Headers headers, JSON json) {
            Log.d(TAG, "onSuccess! " + json.toString());
            JSONObject jsonObject = json.jsonObject;
            try {
                adapter.clear();
                adapter.addAll(User.fromJsonArray(jsonObject.getJSONArray("users")));
                hideProgressBar();
            } catch (JSONException e) {
                Log.e(TAG, "JSON exception", e);
            }
        }

        @Override
        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
            Log.e(TAG, "onFailure! " + response, throwable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFollowersBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        fetching = getIntent().getStringExtra("fetching");

        client = TwitterApp.getTwitterClient(this);
        followers = new ArrayList<>();
        adapter = new FollowersAdapter(this, followers);

        // set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(fetching);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // set up recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvFollowers.setLayoutManager(linearLayoutManager);
        binding.rvFollowers.setAdapter(adapter);
    }

    private void populateFollowersList() {
        showProgressBar();
        client.getFollowers(user.screenName, handler);
    }

    private void populateFollowingList() {
        showProgressBar();
        client.getFollowing(user.screenName, handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        if (fetching.equals("Followers")) {
            populateFollowersList();
        } else {
            populateFollowingList();
        }
        return true;
    }

    public void showProgressBar() {
        if (miActionProgressItem == null) return;
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        if (miActionProgressItem == null) return;
        miActionProgressItem.setVisible(false);
    }
}