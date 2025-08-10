package com.example.gkmoney;

import java.util.Date;

public class Expense {
    public String NoteType;
    public String NoteCategory;
    public String NoteDescription;
    public Date date;
    public double amount;
    public int iconResId;

    public Expense(String NoteType,String NoteCategory,  String NoteDescription, Date date, double amount, int iconResId) {
        this.NoteType = NoteType;
        this.NoteCategory = NoteCategory;
        this.NoteDescription = NoteDescription;
        this.date = date;
        this.amount = amount;
        this.iconResId = iconResId;
    }
}

