package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


// an adapter to bind trainer data to the RecyclerView
public class TrainersAdapter extends RecyclerView.Adapter<TrainersAdapter.TrainerViewHolder> {

    private Context context;
    private List<Trainer> trainers;

    public TrainersAdapter(Context context, List<Trainer> trainers) {
        this.context = context;
        this.trainers = trainers;
    }

    @NonNull
    @Override
    public TrainerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);
        return new TrainerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainerViewHolder holder, int position) {
        Trainer trainer = trainers.get(position);

        holder.name.setText(trainer.getName());
        Glide.with(context).load(trainer.getProfilePictureUrl()).into(holder.profilePic);

        holder.messageButton.setOnClickListener(v -> {
//            Intent intent = new Intent(context, MessageActivity.class);
//            intent.putExtra("trainerId", trainer.getUserId());
//            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return trainers.size();
    }

    static class TrainerViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView name;
        Button messageButton;

        public TrainerViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.trainerProfilePic);
            name = itemView.findViewById(R.id.trainerName);
            messageButton = itemView.findViewById(R.id.messageButton);
        }
    }
}

