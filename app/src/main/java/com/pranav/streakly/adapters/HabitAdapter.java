package com.pranav.streakly.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pranav.streakly.R;
import com.pranav.streakly.models.Habit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder>{
    private List<Habit> habitList;
    private Context context;

    public HabitAdapter(List<Habit> habitList,Context context) {
        this.habitList = habitList;
        this.context = context;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_card, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        Habit habit = habitList.get(position);
        holder.tvHabitName.setText(habit.getName());
        holder.tvProgress.setText("0% complete"); // You can compute progress if needed

        holder.btnEdit.setOnClickListener(v -> {
            int truePosition = holder.getAdapterPosition();
            if (truePosition != RecyclerView.NO_POSITION){
                showEditDialog(habitList.get(truePosition), truePosition);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int truePosition = holder.getAdapterPosition();
            if (truePosition != RecyclerView.NO_POSITION) {
                deleteHabitFromFirestore(habitList.get(truePosition).getId(), truePosition);
            }
        });

    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    private void deleteHabitFromFirestore(String habitId, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position < 0 || position >= habitList.size()) {
            Toast.makeText(context, "Invalid item position", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .document(habitId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Habit deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditDialog(Habit habit, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Habit");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_habit, null);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etGoal = view.findViewById(R.id.etEditGoal);

        etName.setText(habit.getName());
        etGoal.setText(habit.getGoal());

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString();
            String newGoal = etGoal.getText().toString();

            updateHabitInFirestore(habit.getId(), newName, newGoal, position);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateHabitInFirestore(String id, String newName, String newGoal, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("goal", newGoal);

        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .document(id)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Habit updatedHabit = habitList.get(position);
                    updatedHabit.setName(newName);
                    updatedHabit.setGoal(newGoal);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Habit updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update habit", Toast.LENGTH_SHORT).show();
                });
    }


    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName, tvProgress;
        ImageView btnEdit,btnDelete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
