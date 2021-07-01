package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
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
    int position;
    Long tweetId;

    public static final String TAG = "TweetDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTweetDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        setSupportActionBar(binding.toolbar);

        client = TwitterApp.getTwitterClient(this);

        getSupportActionBar().setTitle("");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        position = getIntent().getExtras().getInt("position");

        User retweeter;
        if (tweet instanceof Retweet) {
            retweeter = ((Retweet) tweet).getRetweetedBy();
            tweetId = tweet.id;
            tweet = ((Retweet) tweet).getOriginal();

            binding.tvRetweetedBy.setText(String.format("%s Retweeted", retweeter.name));
            binding.tvRetweetedBy.setVisibility(View.VISIBLE);
            binding.ivRetweet.setVisibility(View.VISIBLE);
        } else {
            tweetId = tweet.id;
            binding.tvRetweetedBy.setVisibility(View.GONE);
            binding.ivRetweet.setVisibility(View.GONE);

            // since tvRetweetedBy doesn't exist anymore, we need to update the rest of the content
            // to be below the toolbar instead
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.ivProfilePicture.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.toolbar);
        }

        binding.tvBody.setText(tweet.body);
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
        binding.tvDate.setText(tweet.exactTimestamp);
        Glide.with(this)
                .load(tweet.user.profilePictureUrl)
                .circleCrop()
                .into(binding.ivProfilePicture);

        // TODO: where to put format number?
        String boldText = User.formatNumber(tweet.retweetCount);
        String normalText = tweet.retweetCount == 1 ? " Retweet" : " Retweets";
        SpannableString str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvRetweetCount.setText(str);

        boldText = User.formatNumber(tweet.favoriteCount);
        normalText = tweet.favoriteCount == 1 ? " Like" : " Likes";
        str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvFavoriteCount.setText(str);

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

        if (tweet.isRetweeted) {
            binding.ibRetweet.setImageResource(R.drawable.ic_vector_retweet);
            binding.ibRetweet.setTag("filled");
        } else {
            binding.ibRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
            binding.ibRetweet.setTag("empty");
        }

        if (tweet.isFavorited) {
            binding.ibFavorite.setImageResource(R.drawable.ic_vector_heart);
            binding.ibFavorite.setTag("filled");
        } else {
            binding.ibFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
            binding.ibFavorite.setTag("empty");
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
                            i.putExtra("reply", Parcels.wrap(tweet));
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

        binding.ibFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to (un)favorite Tweet");
                        try {
                            Intent i = new Intent();

                            if (json.jsonObject.getBoolean("favorited")) {
                                binding.ibFavorite.setImageResource(R.drawable.ic_vector_heart);
                                binding.ibFavorite.setTag("filled");
                            } else {
                                binding.ibFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
                                binding.ibFavorite.setTag("empty");
                            }

                            i.putExtra("position", position);
                            if (json.jsonObject.has("retweeted_status")) {
                                Retweet tweet = Retweet.fromJson(json.jsonObject);
                                i.putExtra("tweet", Parcels.wrap(tweet));
                            } else {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                i.putExtra("tweet", Parcels.wrap(tweet));
                            }

                            setResult(RESULT_OK, i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to (un)favorite Tweet " + response, throwable);
                    }
                };

                if (binding.ibFavorite.getTag() == "empty") {
                    Log.i(TAG, "empty");
                    client.favoriteTweet(tweetId, handler);
                } else if (binding.ibFavorite.getTag() == "filled") {
                    Log.i(TAG, "filled");
                    client.unfavoriteTweet(tweetId, handler);
                }
            }
        });
    }
}