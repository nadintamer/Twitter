package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.restclienttemplate.models.Retweet;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    ActivityTweetDetailBinding binding;
    Tweet tweet;
    TwitterClient client;

    public static final String TAG = "TweetDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTweetDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        client = TwitterApp.getTwitterClient(this);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        if (tweet instanceof Retweet) {
            tweet = ((Retweet) tweet).getOriginal();
        }
        binding.tvBody.setText(tweet.body);
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
        Glide.with(this)
                .load(tweet.user.profilePictureUrl)
                .circleCrop()
                .into(binding.ivProfilePicture);

        int radius = 40;
        if (!tweet.imageUrls.isEmpty()) {
            Glide.with(this)
                    .load(tweet.imageUrls.get(0))
                    .transform(new CenterCrop(), new RoundedCorners(radius))
                    .into(binding.ivEmbeddedImage);
            binding.ivEmbeddedImage.setVisibility(View.VISIBLE);
        } else {
            binding.ivEmbeddedImage.setVisibility(View.GONE);
        }

        binding.ivProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = tweet.user;
                Intent i = new Intent(TweetDetailActivity.this, UserDetailActivity.class);
                i.putExtra("user", Parcels.wrap(user));
                startActivity(i);
            }
        });

        binding.etReply.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    binding.etReply.setText(String.format("@%s", tweet.user.screenName));
                    binding.etReply.setSelection(tweet.user.screenName.length() + 1);
                }

            }
        });

        binding.btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.replyToTweet(binding.etReply.getText().toString(), tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to reply to Tweet");
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
                        Log.e(TAG, "onFailure to reply to Tweet", throwable);
                    }
                });
            }
        });
    }
}