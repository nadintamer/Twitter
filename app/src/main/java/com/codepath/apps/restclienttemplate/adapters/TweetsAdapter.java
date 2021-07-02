package com.codepath.apps.restclienttemplate.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.ComposeDialogFragment;
import com.codepath.apps.restclienttemplate.activities.ImageDetailActivity;
import com.codepath.apps.restclienttemplate.activities.TimelineActivity;
import com.codepath.apps.restclienttemplate.activities.TweetDetailActivity;
import com.codepath.apps.restclienttemplate.activities.UserDetailActivity;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Retweet;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.models.Utils;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.List;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;
    TwitterClient client;

    public static final String TAG = "TweetsAdapter";
    public static final int DETAIL_REQUEST_CODE = 40;

    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        client = TwitterApp.getTwitterClient(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTweetBinding binding = ItemTweetBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // utility methods for convenience in notifying adapter
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemTweetBinding binding;

        public ViewHolder(@NonNull ItemTweetBinding itemTweetBinding) {
            super(itemTweetBinding.getRoot());
            this.binding = itemTweetBinding;
            itemTweetBinding.getRoot().setOnClickListener(this);
        }

        public void bind(Tweet tweet) {
            // if tweet is a retweet, display "@user Retweeted" text, otherwise hide it
            if (tweet instanceof Retweet) {
                User retweeter = ((Retweet) tweet).getRetweetedBy();
                tweet = ((Retweet) tweet).getOriginal();
                binding.tvRetweetedBy.setText(String.format("%s Retweeted", retweeter.name));
                binding.tvRetweetedBy.setVisibility(View.VISIBLE);
                binding.ivRetweet.setVisibility(View.VISIBLE);
            } else {
                binding.tvRetweetedBy.setVisibility(View.GONE);
                binding.ivRetweet.setVisibility(View.GONE);
            }

            // bind relevant data to text views
            binding.tvBody.setText(tweet.body);
            binding.tvUsername.setText(tweet.user.name);
            binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
            binding.tvTimestamp.setText(tweet.relativeTimestamp);

            Glide.with(context)
                    .load(tweet.user.profilePictureUrl)
                    .circleCrop()
                    .into(binding.ivProfilePicture);

            // if there is an image, load it and set up onClickListener to zoom in, otherwise hide it
            int radius = 40;
            if (!tweet.imageUrls.isEmpty()) {
                final String imageUrl = tweet.imageUrls.get(0);
                Glide.with(context)
                        .load(imageUrl)
                        .transform(new CenterCrop(), new RoundedCorners(radius))
                        .into(binding.ivEmbeddedImage);
                binding.ivEmbeddedImage.setVisibility(View.VISIBLE);
                binding.ivEmbeddedImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(context, ImageDetailActivity.class);
                        i.putExtra("imageUrl", imageUrl);
                        context.startActivity(i);
                    }
                });
            } else {
                binding.ivEmbeddedImage.setVisibility(View.GONE);
            }

            // set up retweet and like buttons
            binding.tvRetweetCount.setText(Utils.formatNumber(tweet.retweetCount));
            binding.tvFavoriteCount.setText(Utils.formatNumber(tweet.favoriteCount));

            String retweetStatus = tweet.isRetweeted ? "filled" : "empty";
            setRetweetButton(retweetStatus);

            String favoriteStatus = tweet.isFavorited ? "filled" : "empty";
            setFavoriteButton(favoriteStatus);

            // set up onClickListener on profile picture to show user detail view
            binding.ivProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // check if an item was deleted, but the user clicked it before the UI removed it
                        Tweet tweet = tweets.get(position);
                        if (tweet instanceof Retweet) {
                            tweet = ((Retweet) tweet).getOriginal();
                        }
                        User user = tweet.user;
                        Intent i = new Intent(context, UserDetailActivity.class);
                        i.putExtra("user", Parcels.wrap(user));
                        context.startActivity(i);
                    }
                }
            });

            // set up onClickListeners for reply, favorite & retweet buttons
            final Tweet finalTweet = tweet;
            binding.ibReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TimelineActivity activity = (TimelineActivity) view.getContext();
                    FragmentManager fm = activity.getSupportFragmentManager();
                    ComposeDialogFragment composeDialogFragment = ComposeDialogFragment.newInstance(activity.getCurrentUser(), finalTweet);
                    composeDialogFragment.show(fm, "fragment_compose");
                }
            });

            binding.ibFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleFavoriteClick();
                }
            });

            binding.ibRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleRetweetClick();
                }
            });
        }

        // set up onClickListener for view as a whole (to show tweet detail activity)
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, TweetDetailActivity.class);
                i.putExtra("tweet", Parcels.wrap(tweet));
                i.putExtra("position", position);
                ((Activity) context).startActivityForResult(i, DETAIL_REQUEST_CODE);
            }
        }

        private void handleFavoriteClick() {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to (un)favorite Tweet");
                        try {
                            updateTweetsWithJson(json, position);
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
                    client.favoriteTweet(tweet.id, handler);
                } else if (binding.ibFavorite.getTag() == "filled") {
                    client.unfavoriteTweet(tweet.id, handler);
                }
            }
        }

        private void handleRetweetClick() {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Tweet tweet = tweets.get(position);
                final JsonHttpResponseHandler singleTweetHandler = new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            updateTweetsWithJson(json, position);
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
                        Tweet tweet = tweets.get(position);
                        // make another API call to Twitter to fetch necessary information about original tweet (in case it was a retweet)
                        client.getSingleTweet(tweet.id, singleTweetHandler);
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to (un)retweet Tweet " + response, throwable);
                    }
                };

                if (binding.ibRetweet.getTag() == "empty") {
                    Log.i(TAG, "empty");
                    client.retweetTweet(tweet.id, handler);
                } else if (binding.ibRetweet.getTag() == "filled") {
                    Log.i(TAG, "filled");
                    client.unretweetTweet(tweet.id, handler);
                }
            }
        }

        // helper method to update tweets list with new information from json
        private void updateTweetsWithJson(JsonHttpResponseHandler.JSON json, int position) throws JSONException {
            JSONObject object = json.jsonObject;
            if (json.jsonObject.has("retweeted_status")) {
                Retweet originalRetweet = Retweet.fromJson(object);
                tweets.set(position, originalRetweet); // displayed retweeted status
            } else {
                Tweet originalTweet = Tweet.fromJson(object);
                tweets.set(position, originalTweet);
            }
            notifyItemChanged(position);
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
    }
}
