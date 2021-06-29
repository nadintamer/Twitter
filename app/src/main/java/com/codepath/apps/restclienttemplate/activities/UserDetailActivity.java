package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.databinding.ActivityUserDetailBinding;
import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

public class UserDetailActivity extends AppCompatActivity {

    ActivityUserDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        User user = Parcels.unwrap(getIntent().getParcelableExtra("user"));

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

        // TODO: clean up this code
        String boldText = User.formatNumber(user.numFollowing);
        String normalText = " Following";
        SpannableString str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvFollowing.setText(str);

        boldText = User.formatNumber(user.numFollowers);
        normalText = " Followers";
        str = new SpannableString(boldText + normalText);
        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvFollowers.setText(str);
    }
}