package com.codepath.apps.restclienttemplate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.FragmentComposeBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeDialogFragment extends DialogFragment {

    private static final int MAX_TWEET_LENGTH = 280;
    private static final String TAG = "ComposeDialogFragment";

    private FragmentComposeBinding binding;
    private TwitterClient client;

    public interface ComposeDialogListener {
        void onFinishComposeDialog(Tweet tweet);
    }

    public ComposeDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ComposeDialogFragment newInstance(User user, Tweet replyingTo) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("user", Parcels.wrap(user));
        args.putParcelable("replyingTo", Parcels.wrap(replyingTo));
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentComposeBinding.inflate(getLayoutInflater(), container, false);
        client = TwitterApp.getTwitterClient(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        // Fetch arguments from bundle and set title
        final User currentUser = Parcels.unwrap(getArguments().getParcelable("user"));
        final Tweet replyingTo = Parcels.unwrap(getArguments().getParcelable("replyingTo"));

        if (replyingTo != null) {
            Log.i(TAG, "Replying to a tweet");
            binding.tvReplyingTo.setText(String.format("Replying to @%s", replyingTo.user.screenName));
            binding.ivReply.setVisibility(View.VISIBLE);
            binding.tvReplyingTo.setVisibility(View.VISIBLE);
            binding.btnTweet.setText("Reply");
            binding.etBody.setHint("Tweet your reply");
            binding.etBody.setText(String.format("@%s", replyingTo.user.screenName));
            binding.etBody.setSelection(replyingTo.user.screenName.length() + 1);
        } else {
            binding.ivReply.setVisibility(View.GONE);
            binding.tvReplyingTo.setVisibility(View.GONE);
            binding.btnTweet.setText("Tweet");
            binding.etBody.setHint("What's happening?");
        }

        // Show soft keyboard automatically and request focus to field
        binding.etBody.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        binding.btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = binding.etBody.getText().toString();
                if (tweetContent.isEmpty()) {
                    // TODO: use snackbar instead of toast?
                    Toast.makeText(getActivity(), "Sorry, your Tweet cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(getActivity(), "Sorry, your Tweet cannot be more than 140 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (replyingTo != null) {
                    client.replyToTweet(tweetContent, replyingTo.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to reply to Tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                                listener.onFinishComposeDialog(null);
                                dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to reply to Tweet", throwable);
                        }
                    });
                } else {
                    client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to publish Tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                ComposeDialogListener listener = (ComposeDialogListener) getActivity();
                                listener.onFinishComposeDialog(tweet);
                                dismiss();
                            } catch (JSONException e) {
                                // TODO everywhere: log all errors
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to publish Tweet", throwable);
                        }
                    });
                }
            }
        });

        Glide.with(this)
                .load(currentUser.profilePictureUrl)
                .circleCrop()
                .into(binding.ivProfilePicture);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }
}
