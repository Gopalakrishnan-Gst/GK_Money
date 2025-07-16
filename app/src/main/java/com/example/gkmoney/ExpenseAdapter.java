package com.example.gkmoney;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;

    public ExpenseAdapter(List<Expense> expenses) {
        this.expenseList = expenses;
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoteType, txtNoteDescription, txtDate, txtAmount;
        ImageView imgType;

        public ExpenseViewHolder(View itemView) {
            super(itemView);

            txtNoteDescription = itemView.findViewById(R.id.noteDescription);
            txtDate = itemView.findViewById(R.id.noteDate);
            txtAmount = itemView.findViewById(R.id.noteAmount);
            imgType = itemView.findViewById(R.id.noteType);
        }
    }



    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.txtNoteDescription.setText(expense.NoteDescription);
        holder.txtDate.setText(expense.date);
        holder.txtAmount.setText(String.format("₹%.2f", expense.amount));
        holder.imgType.setImageResource(expense.iconResId);

        // Optional: Red for debits, green for credits
        if (expense.amount < 0) {
            holder.txtAmount.setTextColor(Color.RED);
        } else {
            holder.txtAmount.setTextColor(Color.parseColor("#388E3C")); // Green
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }
}

