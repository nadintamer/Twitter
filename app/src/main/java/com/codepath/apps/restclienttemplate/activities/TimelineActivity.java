package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.EndlessRecyclerViewScrollListener;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.databinding.ActivityTimelineBinding;
import com.codepath.apps.restclienttemplate.models.Retweet;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    ActivityTimelineBinding binding;
    EndlessRecyclerViewScrollListener scrollListener;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    TwitterClient client;
    User currentUser;
    MenuItem miActionProgressItem;

    private static final String TAG = "TimelineActivity";
    private static final int DETAIL_REQUEST_CODE = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimelineBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("");

        // set up required data and recyclerView + scrollListener + swipe layout
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

        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateHomeTimeline();
            }
        });
        binding.swipeContainer.setColorSchemeResources(R.color.twitter_blue,
                R.color.twitter_blue_30,
                R.color.twitter_blue_50,
                R.color.twitter_blue_fill_pressed);

        binding.fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showComposeDialog();
            }
        });

        populateCurrentUser();
    }

    // load next batch of user tweets from Twitter API (for endless scrolling)
    private void loadNextDataFromApi() {
        showProgressBar();
        client.getHomeTimeline(tweets.get(tweets.size() - 1).id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.remove(tweets.size() - 1);
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    hideProgressBar();
                } catch (JSONException e) {
                    // Log the error
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }

    // populate home timeline with tweets
    private void populateHomeTimeline() {
        showProgressBar();
        client.getHomeTimeline(null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    binding.swipeContainer.setRefreshing(false);
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

    // get current user information and save it
    private void populateCurrentUser() {
        client.getUserInformation(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess to fetch user information! " + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    currentUser = User.fromJson(jsonObject);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to fetch user information! " + response, throwable);
            }
        });
    }

    // handle results from replying or tweeting on a different activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == DETAIL_REQUEST_CODE) {
            if (data.hasExtra("reply")) {
                Tweet tweet = Parcels.unwrap(data.getParcelableExtra("reply"));
                tweets.add(0, tweet);
                adapter.notifyItemInserted(0);
                binding.rvTweets.scrollToPosition(0);
            } else if (data.hasExtra("tweet")) {
                int position = data.getExtras().getInt("position");
                Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
                if (tweet instanceof Retweet) {
                    Retweet retweet = (Retweet) tweet;
                    tweets.set(position, retweet);
                } else {
                    tweets.set(position, tweet);
                }
                adapter.notifyItemChanged(position);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // helper methods for compose tweet modal dialog
    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance(currentUser, null);
        composeDialogFragment.show(fm, "fragment_compose");
    }

    @Override
    public void onFinishComposeDialog(Tweet tweet) {
        if (tweet == null) { // replying to tweet, not composing
            Toast.makeText(this, "Reply sent successfully!", Toast.LENGTH_SHORT).show();
        } else {
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            binding.rvTweets.scrollToPosition(0);
        }
    }

    // helper methods for toolbar and progress indicator
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        populateHomeTimeline(); // don't populate home timeline until progress bar has been set
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            client.clearAccessToken(); // forget who's logged in
            finish(); // navigate backwards to Login screen
            return true;
        } else if (id == R.id.action_view_profile) {
            Intent i = new Intent(this, UserDetailActivity.class);
            i.putExtra("user", Parcels.wrap(getCurrentUser()));
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showProgressBar() {
        if (miActionProgressItem == null) return;
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        if (miActionProgressItem == null) return;
        miActionProgressItem.setVisible(false);
    }

    // return the user that is currently logged in
    public User getCurrentUser() {
        return currentUser;
    }
}