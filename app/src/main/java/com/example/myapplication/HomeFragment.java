package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView trainersRecyclerView;
    private TrainersAdapter trainersAdapter;
    private List<Trainer> trainersList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trainersRecyclerView = view.findViewById(R.id.trainersRecyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 1);

        trainersAdapter = new TrainersAdapter(requireContext(), trainersList);
        trainersRecyclerView.setLayoutManager(layoutManager);
        trainersRecyclerView.setAdapter(trainersAdapter);


        fetchTrainersFromFirestore();
    }

    private void fetchTrainersFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("isTrainer", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    trainersList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        // Convert to a Trainer object
                        Trainer trainer = doc.toObject(Trainer.class);
                        if (trainer != null) {
                            trainer.setUserId(doc.getId());
                            trainersList.add(trainer);
                        }
                    }
                    trainersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // handle error
                });
    }
}
