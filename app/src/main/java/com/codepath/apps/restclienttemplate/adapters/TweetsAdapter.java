package com.codepath.apps.restclienttemplate.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.activities.TweetDetailActivity;
import com.codepath.apps.restclienttemplate.activities.UserDetailActivity;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Retweet;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
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
    public static final int REPLY_REQUEST_CODE = 40;

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

            binding.tvBody.setText(tweet.body);
            binding.tvUsername.setText(tweet.user.name);
            binding.tvScreenName.setText(String.format("@%s", tweet.user.screenName));
            binding.tvTimestamp.setText(tweet.relativeTimestamp);

            int radius = 40;
            if (!tweet.imageUrls.isEmpty()) {
                Glide.with(context)
                        .load(tweet.imageUrls.get(0))
                        .transform(new CenterCrop(), new RoundedCorners(radius))
                        .into(binding.ivEmbeddedImage);
                binding.ivEmbeddedImage.setVisibility(View.VISIBLE);
            } else {
                binding.ivEmbeddedImage.setVisibility(View.GONE);
            }

            binding.tvRetweetCount.setText(User.formatNumber(tweet.retweetCount)); // TODO: this seems like bad style lol
            binding.tvFavoriteCount.setText(User.formatNumber(tweet.favoriteCount));

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



            Glide.with(context)
                    .load(tweet.user.profilePictureUrl)
                    .circleCrop()
                    .into(binding.ivProfilePicture);

            binding.ivProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                        User user = tweets.get(position).user;
                        Intent i = new Intent(context, UserDetailActivity.class);
                        i.putExtra("user", Parcels.wrap(user));
                        context.startActivity(i);
                    }
                }
            });

            binding.ibFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                        Tweet tweet = tweets.get(position);
                        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess to (un)favorite Tweet");
                                try {
                                    if (json.jsonObject.has("retweeted_status")) {
                                        Retweet tweet = Retweet.fromJson(json.jsonObject);
                                        tweets.set(position, tweet);
                                    } else {
                                        Tweet tweet = Tweet.fromJson(json.jsonObject);
                                        tweets.set(position, tweet);
                                    }
                                    notifyItemChanged(position);
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
                            client.favoriteTweet(tweet.id, handler);
                        } else if (binding.ibFavorite.getTag() == "filled") {
                            Log.i(TAG, "filled");
                            client.unfavoriteTweet(tweet.id, handler);
                        }
                    }
                }
            });

            binding.ibRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                        Tweet tweet = tweets.get(position);

                        // TODO: is this bad? not sure how else to make it display correctly
                        final JsonHttpResponseHandler singleTweetHandler = new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                try {
                                    JSONObject object = json.jsonObject;
                                    Retweet originalRetweet = Retweet.fromJson(object);
                                    tweets.set(position, originalRetweet); // displayed retweeted status
                                    notifyItemChanged(position);
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
            });
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                Tweet tweet = tweets.get(position);
                Intent i = new Intent(context, TweetDetailActivity.class);
                i.putExtra("tweet", Parcels.wrap(tweet));
                ((Activity) context).startActivityForResult(i, REPLY_REQUEST_CODE);
            }
        }
    }
}
