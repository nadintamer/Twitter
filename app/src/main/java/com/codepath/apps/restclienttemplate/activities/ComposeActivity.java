package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    TwitterClient client;
    ActivityComposeBinding binding;

    private static final int MAX_TWEET_LENGTH = 140;
    private static final String TAG = "ComposeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComposeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        client = TwitterApp.getTwitterClient(this);

        binding.btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = binding.etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    // TODO: use snackbar instead of toast?
                    Toast.makeText(ComposeActivity.this, "Sorry, your Tweet cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your Tweet cannot be more than 140 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish Tweet");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Intent i = new Intent();
                            i.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, i);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish Tweet", throwable);
                    }
                });
            }
        });
    }
}