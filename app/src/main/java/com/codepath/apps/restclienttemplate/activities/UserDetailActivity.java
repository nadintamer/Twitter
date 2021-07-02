package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityUserDetailBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.models.Utils;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class UserDetailActivity extends AppCompatActivity {

    ActivityUserDetailBinding binding;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    TwitterClient client;
    User user;
    EndlessRecyclerViewScrollListener scrollListener;
    MenuItem miActionProgressItem;

    private static final String TAG = "UserDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // set up toolbar and custom back button
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // set up required data and recyclerView + scrollListener
        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        client = TwitterApp.getTwitterClient(this);
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.rvTweets.setLayoutManager(linearLayoutManager);
        binding.rvTweets.setAdapter(adapter);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi();
            }
        };
        binding.rvTweets.addOnScrollListener(scrollListener);

        // bind all relevant user data into views
        binding.tvName.setText(user.name);
        binding.tvScreenName.setText(String.format("@%s", user.screenName));
        binding.tvBio.setText(user.bio);

        Glide.with(this)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(binding.ivProfilePicture);

        Glide.with(this)
                .load(user.bannerUrl)
                .centerCrop()
                .into(binding.ivBanner);

        SpannableString following = Utils.formatPartiallyBoldText(Utils.formatNumber(user.numFollowing), " Following");
        binding.tvFollowing.setText(following);

        SpannableString followers = Utils.formatPartiallyBoldText(Utils.formatNumber(user.numFollowers), " Followers");
        binding.tvFollowers.setText(followers);

        // set up onClickListeners to lead to followers/following list activity
        binding.tvFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserDetailActivity.this, FollowersActivity.class);
                i.putExtra("user", Parcels.wrap(user));
                i.putExtra("fetching", "Followers");
                startActivity(i);
            }
        });

        binding.tvFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserDetailActivity.this, FollowersActivity.class);
                i.putExtra("user", Parcels.wrap(user));
                i.putExtra("fetching", "Following");
                startActivity(i);
            }
        });
    }

    // load next batch of user tweets from Twitter API (for endless scrolling)
    private void loadNextDataFromApi() {
        showProgressBar();
        client.getUserTimeline(tweets.get(tweets.size() - 1).id, user.screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.remove(tweets.size() - 1);
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    hideProgressBar();
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

    // fetch given user's timeline from the Twitter API
    private void populateUserTimeline() {
        showProgressBar();
        client.getUserTimeline(null, user.screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    hideProgressBar();
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

    // helper methods for menu bar & progress indicator
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // don't populate home timeline until progress bar has been set - otherwise no progress indicator will be visible
        populateUserTimeline();
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