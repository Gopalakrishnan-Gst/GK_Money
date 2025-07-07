package com.example.gkmoney;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddNote extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String[] noteTypes = {
            "Credit","Debit","Investment"
    };

    EditText noteDate, noteAmount, noteDescription;
    String noteTypeValue, noteDateValue, noteAmountValue, noteDescriptionValue;
    Button cancel, addNote;

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
        cancel = findViewById(R.id.cancel);
        addNote = findViewById(R.id.addNote);

        // Get instance of Firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Get reference for the database
        databaseReference = firebaseDatabase.getReference("Notes");

        db = FirebaseFirestore.getInstance();

        notes = new Notes();

        Spinner noteType = findViewById(R.id.noteType);
        noteType.setOnItemSelectedListener(this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                noteTypes
        );
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        noteType.setAdapter(ad);

        noteDate = findViewById(R.id.noteDate);
        noteDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                         AddNote.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // on below line we are setting date to our edit text.
                                noteDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });

        //cancel button action
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteTypeValue=null;
                noteAmount.setText(null);
                noteDate.setText(null);
                noteDescription.setText(null);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });


        //Add button action
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteDateValue=noteDate.getText().toString().trim();
                noteAmountValue=noteAmount.getText().toString().trim();
                noteDescriptionValue=noteDescription.getText().toString().trim();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

                String CreatedOn = sdf.format(new Date());
                String CreatedBy = firebaseAuth.getCurrentUser().getEmail().toString();

                if (noteTypeValue.isEmpty() || noteAmountValue.isEmpty() || noteDateValue.isEmpty() || noteDescriptionValue.isEmpty()){
                    Toast.makeText(AddNote.this, "Please fill all details!", Toast.LENGTH_SHORT).show();
                }else{
                    Map<String, Object> note = new HashMap<>();
                    note.put("NOTE_TYPE", noteTypeValue);
                    note.put("NOTE_DATE", noteDateValue);
                    note.put("NOTE_AMOUNT", noteAmountValue);
                    note.put("NOTE_DESCRIPTION", noteDescriptionValue);
                    note.put("CREATED_BY", CreatedBy);
                    note.put("CREATED_ON", CreatedOn);

                    db.collection("Notes")
                            .add(note)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(AddNote.this, "Note added successfully", Toast.LENGTH_SHORT).show();
                                    noteTypeValue=null;
                                    noteAmount.setText(null);
                                    noteDate.setText(null);
                                    noteDescription.setText(null);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddNote.this, "Failed to add note!", Toast.LENGTH_SHORT).show();
                                }
                            });

                }




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