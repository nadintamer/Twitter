package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.databinding.ItemFollowerBinding;
import com.codepath.apps.restclienttemplate.databinding.ItemTweetBinding;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.ViewHolder> {

    Context context;
    List<User> followers;
    TwitterClient client;

    public FollowersAdapter(Context context, List<User> followers) {
        this.context = context;
        this.followers = followers;
        client = TwitterApp.getTwitterClient(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ItemFollowerBinding binding = ItemFollowerBinding.inflate(LayoutInflater.from(context), parent, false);
        return new FollowersAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        User user = followers.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return followers.size();
    }

    public void clear() {
        followers.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<User> list) {
        followers.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemFollowerBinding binding;

        public ViewHolder(@NonNull ItemFollowerBinding itemFollowerBinding) {
            super(itemFollowerBinding.getRoot());
            this.binding = itemFollowerBinding;
        }

        public void bind(User user) {
            binding.tvName.setText(user.name);
            binding.tvScreenName.setText(String.format("@%s", user.screenName));
            binding.tvBio.setText(user.bio);
            Glide.with(context)
                    .load(user.profilePictureUrl)
                    .circleCrop()
                    .into(binding.ivProfilePicture);
        }
    }
}
