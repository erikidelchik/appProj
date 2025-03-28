package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


// an adapter to bind trainer data to the RecyclerView
public class TrainersAdapter extends RecyclerView.Adapter<TrainersAdapter.TrainerViewHolder> {

    private Context context;
    private List<Trainer> trainers;
    SharedPreferences prefs;

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

        // Set username
        holder.name.setText(trainer.getUsername() != null ? trainer.getUsername() : "Unknown Trainer");

        // Load profile picture
        if (trainer.getProfilePicture() != null && !trainer.getProfilePicture().isEmpty()) {
            Glide.with(context)
                    .load(trainer.getProfilePicture())
                    .placeholder(R.drawable.blank_profile_picture) // Placeholder image
                    .into(holder.profilePic);
        }
        else {
            holder.profilePic.setImageResource(R.drawable.blank_profile_picture);
        }


        // Message button click listener
        holder.messageButton.setOnClickListener(v -> {
            // Retrieve the phone number from SharedPreferences
            prefs = context.getSharedPreferences("phoneNumbers", Context.MODE_PRIVATE);
            String phoneNum = prefs.getString(trainer.getUserId() + "phoneNum", "");

            if (!phoneNum.isEmpty()) {
                holder.phoneText.setVisibility(View.VISIBLE);
                holder.phoneText.setText("Phone: " + phoneNum);
            }
            else {
                holder.phoneText.setVisibility(View.VISIBLE);
                holder.phoneText.setText("No phone number found.");
            }
        });

        // navigate to trainer's profile
        holder.profilePic.setOnClickListener(v -> {
            String trainerDocId = trainer.getUserId();
            String trainerName = trainer.getUsername();
            String trainerPicUrl = trainer.getProfilePicture();

            // Pass everything to the fragment
            TrainerVisualProfileFragment fragment = TrainerVisualProfileFragment.newInstance(
                    trainerDocId,
                    trainerName,
                    trainerPicUrl
            );

            // Do the usual fragment transaction
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
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
        TextView phoneText;

        public TrainerViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.trainerProfilePic);
            name = itemView.findViewById(R.id.trainerName);
            messageButton = itemView.findViewById(R.id.messageButton);
            phoneText = itemView.findViewById(R.id.trainerPhone);
        }
    }
}

