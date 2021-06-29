package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.databinding.ActivityComposeBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityTweetDetailBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcels;

public class TweetDetailActivity extends AppCompatActivity {

    ActivityTweetDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTweetDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));
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
    }
}