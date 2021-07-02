package com.codepath.apps.restclienttemplate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.databinding.ActivityImageDetailBinding;
import com.codepath.apps.restclienttemplate.databinding.ActivityUserDetailBinding;

import org.parceler.Parcels;

// activity to display an image from a Tweet in full screen without cropping
public class ImageDetailActivity extends AppCompatActivity {

    ActivityImageDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageDetailBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        Glide.with(this)
                .load(imageUrl)
                .into(binding.ivEmbeddedImage);

        binding.ibExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}