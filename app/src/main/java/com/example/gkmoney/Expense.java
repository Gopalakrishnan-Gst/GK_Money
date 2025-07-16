package com.example.gkmoney;

public class Expense {
    public String NoteType;
    public String NoteDescription;
    public String date;
    public double amount;
    public int iconResId;

    public Expense(String NoteType, String NoteDescription, String date, double amount, int iconResId) {
        this.NoteType = NoteType;
        this.NoteDescription = NoteDescription;
        this.date = date;
        this.amount = amount;
        this.iconResId = iconResId;
    }
}

