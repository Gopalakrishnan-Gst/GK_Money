package com.example.gkmoney;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.WriteResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddNote extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] noteTypes = {
            "Credit","Debit","Investment"
    };

    AutoCompleteTextView noteType, noteCategory;
    EditText noteDate, noteAmount, noteDescription;
    String noteTypeValue,noteCategoryValue, noteDescriptionValue,noteAmountValue;
    Button cancel, addNote;
    Calendar noteDateValue;

    Map<String, List<String>> categoryMap;

    FirebaseFirestore db;


    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private Notes notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        noteAmount = findViewById(R.id.noteAmount);
        noteDescription = findViewById(R.id.noteDescription);
        cancel = findViewById(R.id.cancelButton);
        addNote = findViewById(R.id.addButton);

        // Get instance of Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Get reference for the database
        databaseReference = firebaseDatabase.getReference("Notes");


        notes = new Notes();

        //Note Types
        noteType = findViewById(R.id.noteType);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, noteTypes);
        noteType.setAdapter(typeAdapter);
        noteType.setOnClickListener(view -> noteType.showDropDown());


        // Step 2: Category List Mapping
        noteCategory = findViewById(R.id.noteCategory);
        categoryMap = new HashMap<>();
        categoryMap.put("Credit", Arrays.asList("Salary", "Dividend","Bond Interest","Bank Interest", "Other Income"));
        categoryMap.put("Debit", Arrays.asList("Food", "Shopping", "Bills", "Travel", "Others"));
        categoryMap.put("Investment", Arrays.asList("Stocks", "Mutual Funds", "Real Estate","Gold", "Gold Schemes", "Others"));

        noteType.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = noteTypes[position];
            // Reset category field when type changes
            noteCategory.setText("", false);

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, categoryMap.get(selectedType));
            noteCategory.setAdapter(categoryAdapter);
        });

        // Show category dropdown only if available
        noteCategory.setOnClickListener(v -> {
            if (noteCategory.getAdapter() != null) {
                noteCategory.showDropDown();
            } else {
                Toast.makeText(this, "Please select Note Type first", Toast.LENGTH_SHORT).show();
            }
        });

        noteDate = findViewById(R.id.noteDate);
        noteDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                noteDateValue = Calendar.getInstance();
                noteDateValue.set(year, month, dayOfMonth, 0, 0, 0);
                noteDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        //cancel button action
        cancel.setOnClickListener(view -> finish());


        //Add button action
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                noteTypeValue = noteType.getText().toString();
                noteCategoryValue = noteCategory.getText().toString();
                noteAmountValue = noteAmount.getText().toString();
                noteDescriptionValue = noteDescription.getText().toString();


                if (noteTypeValue == null || noteTypeValue.trim().isEmpty() || noteCategoryValue == null || noteCategoryValue.trim().isEmpty()
                        || noteDate.getText().toString().isEmpty() || noteAmountValue == null ||
                        noteAmountValue.trim().isEmpty() || noteDescriptionValue == null || noteDescriptionValue.trim().isEmpty()) {
                    Toast.makeText(AddNote.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(noteAmountValue);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddNote.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                String CreatedOn = sdf.format(new Date());

                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(AddNote.this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                String CreatedBy = firebaseAuth.getCurrentUser().getEmail().toString();


                Map<String, Object> note = new HashMap<>();
                note.put("NOTE_TYPE", noteTypeValue);
                note.put("NOTE_CATEGORY", noteCategoryValue);
                note.put("NOTE_DATE", noteDateValue.getTime());
                note.put("NOTE_AMOUNT", amount);
                note.put("NOTE_DESCRIPTION", noteDescriptionValue);
                note.put("CREATED_BY", CreatedBy);
                note.put("CREATED_ON", CreatedOn);

                db.collection("Notes")
                        .add(note)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(AddNote.this, "Note added successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(AddNote.this, "Failed to add note!", Toast.LENGTH_SHORT).show();

                            }
                        });


            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        noteTypeValue = noteTypes[position];

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }




}