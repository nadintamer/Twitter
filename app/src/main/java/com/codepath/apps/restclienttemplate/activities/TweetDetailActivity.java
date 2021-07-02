package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.codepath.apps.restclienttemplate.models.Utils;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
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

        // set up toolbar
        getSupportActionBar().setTitle("");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        client = TwitterApp.getTwitterClient(this);
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
        position = getIntent().getExtras().getInt("position");

        // set up UI to show retweeted text if applicable
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

        // bind relevant data into views
        binding.tvBody.setText(tweet.body);
        binding.tvName.setText(tweet.user.name);
        binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
        binding.tvDate.setText(tweet.exactTimestamp);
        Glide.with(this)
                .load(tweet.user.profilePictureUrl)
                .circleCrop()
                .into(binding.ivProfilePicture);

        String retweetText = tweet.retweetCount == 1 ? " Retweet" : " Retweets";
        SpannableString retweet = Utils.formatPartiallyBoldText(Utils.formatNumber(tweet.retweetCount), retweetText);
        binding.tvRetweetCount.setText(retweet);

        String favoriteText = tweet.favoriteCount == 1 ? " Like" : " Likes";
        SpannableString favorite = Utils.formatPartiallyBoldText(Utils.formatNumber(tweet.favoriteCount), favoriteText);
        binding.tvFavoriteCount.setText(favorite);

        // display image if there is one
        int radius = 40;
        if (!tweet.imageUrls.isEmpty()) {
            final String imageUrl = tweet.imageUrls.get(0);
            Glide.with(this)
                    .load(imageUrl)
                    .transform(new CenterCrop(), new RoundedCorners(radius))
                    .into(binding.ivEmbeddedImage);
            binding.ivEmbeddedImage.setVisibility(View.VISIBLE);
            binding.ivEmbeddedImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(TweetDetailActivity.this, ImageDetailActivity.class);
                    i.putExtra("imageUrl", imageUrl);
                    startActivity(i);
                }
            });
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

        // set up reply interaction at the bottom of the screen
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
                            Log.e(TAG, "JSON exception", e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to reply to Tweet", throwable);
                    }
                });
            }
        });

        // set up onClickListeners for favorite, retweet, reply buttons
        binding.ibFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleFavoriteClick();
            }
        });

        binding.ibRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRetweetClick();
            }
        });

        binding.ibReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.etReply.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(binding.etReply, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void handleFavoriteClick() {
        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess to (un)favorite Tweet");
                try {
                    if (json.jsonObject.getBoolean("favorited")) {
                        setFavoriteButton("filled");
                    } else {
                        setFavoriteButton("empty");
                    }

                    Intent i = new Intent();
                    updateIntentWithJson(i, json, position);
                    setResult(RESULT_OK, i);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
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

    private void handleRetweetClick() {
        final JsonHttpResponseHandler singleTweetHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                try {
                    if (json.jsonObject.getBoolean("retweeted")) {
                        setRetweetButton("filled");
                    } else {
                        setRetweetButton("empty");
                    }

                    Intent i = new Intent();
                    updateIntentWithJson(i, json, position);
                    setResult(RESULT_OK, i);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to get single Tweet " + response, throwable);
            }
        };

        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess to (un)retweet Tweet");
                // make another API call to Twitter to fetch necessary information about original tweet (in case it was a retweet)
                client.getSingleTweet(tweetId, singleTweetHandler);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to (un)retweet Tweet " + response, throwable);
            }
        };

        if (binding.ibRetweet.getTag() == "empty") {
            Log.i(TAG, "empty");
            client.retweetTweet(tweetId, handler);
        } else if (binding.ibRetweet.getTag() == "filled") {
            Log.i(TAG, "filled");
            client.unretweetTweet(tweetId, handler);
        }
    }

    // helper methods to handle status of retweet and favorite buttons
    private void setRetweetButton(String status) {
        if (status.equals("filled")) {
            binding.ibRetweet.setImageResource(R.drawable.ic_vector_retweet);
        } else {
            binding.ibRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
        }
        binding.ibRetweet.setTag(status);
    }

    private void setFavoriteButton(String status) {
        if (status.equals("filled")) {
            binding.ibFavorite.setImageResource(R.drawable.ic_vector_heart);
        } else {
            binding.ibFavorite.setImageResource(R.drawable.ic_vector_heart_stroke);
        }
        binding.ibFavorite.setTag(status);
    }

    // helper method to update intent with new information from json
    private void updateIntentWithJson(Intent i, JsonHttpResponseHandler.JSON json, int position) throws JSONException {
        i.putExtra("position", position);
        JSONObject object = json.jsonObject;

        if (object.has("retweeted_status")) {
            Retweet tweet = Retweet.fromJson(json.jsonObject);
            i.putExtra("tweet", Parcels.wrap(tweet));
        } else {
            Tweet tweet = Tweet.fromJson(json.jsonObject);
            i.putExtra("tweet", Parcels.wrap(tweet));
        }
    }
}