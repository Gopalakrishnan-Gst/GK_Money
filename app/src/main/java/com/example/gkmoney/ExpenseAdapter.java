package com.example.gkmoney;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String noteDateStr = sdf.format(expense.date);
        holder.txtDate.setText(noteDateStr );
        holder.txtAmount.setText(String.format("₹%.2f", expense.amount));
        holder.imgType.setImageResource(expense.iconResId);

        if ("Credit".equalsIgnoreCase(expense.NoteType)){
            holder.txtAmount.setTextColor(Color.parseColor("#388E3C"));
            holder.imgType.setColorFilter(Color.parseColor("#388E3C"));
        }else if("Debit".equalsIgnoreCase(expense.NoteType)){
            holder.txtAmount.setTextColor(Color.parseColor("#D32F2F"));
            holder.imgType.setColorFilter(Color.parseColor("#D32F2F"));
        } else if("Investment".equalsIgnoreCase(expense.NoteType)){
            holder.txtAmount.setTextColor(Color.parseColor("#F9A825"));
            holder.imgType.setColorFilter(Color.parseColor("#F9A825"));
        }

    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }
}

