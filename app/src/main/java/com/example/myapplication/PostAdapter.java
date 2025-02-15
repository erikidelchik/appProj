package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<TrainerProfileFragment.PostModel> postList;

    public PostAdapter(List<TrainerProfileFragment.PostModel> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        TrainerProfileFragment.PostModel post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setData(List<TrainerProfileFragment.PostModel> newData) {
        this.postList = newData;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView textPostContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textPostContent = itemView.findViewById(R.id.textPostContent);
        }

        public void bind(TrainerProfileFragment.PostModel post) {
            textPostContent.setText(post.getContent());
        }
    }
}

