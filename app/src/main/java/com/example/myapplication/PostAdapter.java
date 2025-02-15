package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<TrainerProfileFragment.PostModel> postList;
    private final String trainerId;

    public PostAdapter(List<TrainerProfileFragment.PostModel> postList, String tId) {
        this.postList = postList;
        this.trainerId = tId;
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

        holder.deletePostButton.setOnClickListener(v -> {
            // get Firestore ref
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // trainerId might need to be passed in the adapter's constructor
            db.collection("users")
                    .document(trainerId)
                    .collection("posts")
                    .document(post.getDocId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(holder.itemView.getContext(), "Post deleted!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
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
        Button deletePostButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textPostContent = itemView.findViewById(R.id.textPostContent);
            deletePostButton = itemView.findViewById(R.id.deletePostButton);
        }

        public void bind(TrainerProfileFragment.PostModel post) {
            textPostContent.setText(post.getContent());
        }
    }
}

