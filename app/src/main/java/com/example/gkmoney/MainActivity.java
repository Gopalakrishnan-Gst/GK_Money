package com.example.gkmoney;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addNote;
    FirebaseAuth firebaseAuth;

    Button CurrentmonthNotes, AllNotes, AllNoteDtls, CreditNoteDtls, DebitNoteDtls, InvestmentNoteDtls;
    TextView CreditAmount, DebitAmount, InvestmentAmount, AvailableBalance;

    String TopFilter, SecondaryFilter, CurrentUserMailId;



    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();
        CurrentUserMailId = firebaseAuth.getCurrentUser().getEmail().toString();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CurrentmonthNotes = findViewById(R.id.currentMonthNotes);
        AllNotes = findViewById(R.id.allNotes);
        AllNoteDtls = findViewById(R.id.AllNoteDtls);
        CreditNoteDtls = findViewById(R.id.CreditNoteDtls);
        DebitNoteDtls = findViewById(R.id.DebitNoteDtls);
        InvestmentNoteDtls = findViewById(R.id.InvestmentNoteDtls);

        CreditAmount = findViewById(R.id.creditAmount);
        DebitAmount = findViewById(R.id.debitAmount);
        InvestmentAmount = findViewById(R.id.investmentAmount);
        AvailableBalance = findViewById(R.id.AvailableBalanceAmount);

        Button[] btnArray = {AllNotes};
        btnSelection(CurrentmonthNotes,btnArray);
        TopFilter="CURRENT_MONTH_NOTES";

        Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
        btnSelection(AllNoteDtls,btnArray_secondFilter);
        SecondaryFilter="ALL_NOTE_DTLS";

        //Call function load data when openening the app
        fetchNoteDtls(TopFilter, SecondaryFilter);
        //Call dashboard display function to display data when opening the app
        calculateBalanceFromFirestoreForCurrentMonthNotes();



        CurrentmonthNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopFilter = "CURRENT_MONTH_NOTES";
                Button[] btnArray = {AllNotes};
                btnSelection(CurrentmonthNotes,btnArray);
                //second level filter should reset
                SecondaryFilter = "ALL_NOTE_DTLS";
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(AllNoteDtls,btnArray_secondFilter);

                fetchNoteDtls(TopFilter, SecondaryFilter);

                //Dashboard display function
                calculateBalanceFromFirestoreForCurrentMonthNotes();

            }
        });

        AllNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TopFilter = "ALL_NOTES";
                Button[] btnArray = {CurrentmonthNotes};
                btnSelection(AllNotes,btnArray);
                //second level filter should reset
                SecondaryFilter = "ALL_NOTE_DTLS";
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(AllNoteDtls,btnArray_secondFilter);

                fetchNoteDtls(TopFilter, SecondaryFilter);

                //Dashboard display function
                calculateBalanceFromFirestoreForAllNotes();

            }
        });

        AllNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecondaryFilter = "ALL_NOTE_DTLS";
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(AllNoteDtls,btnArray_secondFilter);

                fetchNoteDtls(TopFilter, SecondaryFilter);
            }
        });

        CreditNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecondaryFilter = "CREDIT_NOTE_DTLS";
                Button[] btnArray_secondFilter = {AllNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(CreditNoteDtls,btnArray_secondFilter);

                fetchNoteDtls(TopFilter, SecondaryFilter);
            }
        });

        DebitNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecondaryFilter = "DEBIT_NOTE_DTLS";
                Button[] btnArray_secondFilter = {CreditNoteDtls,AllNoteDtls,InvestmentNoteDtls};
                btnSelection(DebitNoteDtls,btnArray_secondFilter);

                fetchNoteDtls(TopFilter, SecondaryFilter);
            }
        });

        InvestmentNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecondaryFilter = "INVESTMENT_NOTE_DTLS";
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,AllNoteDtls};
                btnSelection(InvestmentNoteDtls,btnArray_secondFilter);

                fetchNoteDtls(TopFilter, SecondaryFilter);
            }
        });

        FloatingActionButton addNote = findViewById(R.id.addNote);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNote.class);
                startActivity(intent);
            }
        });






    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {

        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       int id= item.getItemId();
       if (id==R.id.logout){
           firebaseAuth.signOut();
           Toast.makeText(this, "Logged out Successfully", Toast.LENGTH_SHORT).show();
           Intent intent = new Intent(getApplicationContext(), Login.class);
           startActivity(intent);

       }
       return true;
    }

    public void btnSelection(Button selectedBtn, Button[] deselectedBtn){
        selectedBtn.setSelected(true);
        selectedBtn.setBackgroundColor(Color.parseColor("#1E88E5"));
        selectedBtn.setTextColor(Color.WHITE);
        for(Button button : deselectedBtn) {
            button.setSelected(false);
            button.setBackgroundColor(Color.parseColor("#E3F2FD"));
            button.setTextColor(Color.parseColor("#1E88E5"));
        }
    }

    private Date getFirstDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime(); // java.util.Date
    }

    public void constructNotesData(QueryDocumentSnapshot document, List<Expense> expenses) {
        String noteType = document.getString("NOTE_TYPE");
        Double noteAmount = document.getDouble("NOTE_AMOUNT");
        Date noteDate = document.getDate("NOTE_DATE");
        String noteDescription = document.getString("NOTE_DESCRIPTION");
        Integer iconResId;
        if(noteType.equals("Credit")){
            iconResId = R.drawable.credit_icon;
        } else if (noteType.equals("Debit")) {
            iconResId = R.drawable.debit_icon;
        } else {
            iconResId = R.drawable.investment_icon;
        }
        expenses.add(new Expense(noteType, noteDescription, noteDate,noteAmount, iconResId));

    }

    public void fetchNoteDtls(String TopFilter, String SecondaryFilter){
        List<Expense> expenses = new ArrayList<>();
        if (TopFilter=="ALL_NOTES" && SecondaryFilter=="ALL_NOTE_DTLS"){
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this,  task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        } else if (TopFilter=="ALL_NOTES" && SecondaryFilter=="CREDIT_NOTE_DTLS") {
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE","Credit")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        } else if (TopFilter=="ALL_NOTES" && SecondaryFilter=="DEBIT_NOTE_DTLS") {
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE","Debit")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        } else if (TopFilter=="ALL_NOTES" && SecondaryFilter=="INVESTMENT_NOTE_DTLS") {
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE","Investment")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        }

        if (TopFilter=="CURRENT_MONTH_NOTES" && SecondaryFilter=="ALL_NOTE_DTLS"){
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth()  )
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        } else if (TopFilter=="CURRENT_MONTH_NOTES" && SecondaryFilter=="CREDIT_NOTE_DTLS") {
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .whereEqualTo("NOTE_TYPE","Credit")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        } else if (TopFilter=="CURRENT_MONTH_NOTES" && SecondaryFilter=="DEBIT_NOTE_DTLS") {
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .whereEqualTo("NOTE_TYPE","Debit")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        } else if (TopFilter=="CURRENT_MONTH_NOTES" && SecondaryFilter=="INVESTMENT_NOTE_DTLS") {
            db.collection("Notes")
                    //.whereEqualTo("CREATED_BY",CurrentUserMailId)
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .whereEqualTo("NOTE_TYPE","Investment")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    constructNotesData(document,expenses);
                                }
                                //Recycler View changes
                                RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
                                ExpenseAdapter adapter = new ExpenseAdapter(expenses);
                                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();

                            }
                        }


                    });
        }


    }

    private void calculateBalanceFromFirestoreForAllNotes() {
         db.collection("Notes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalCredit = 0;
                    double totalDebit = 0;
                    double totalInvestment = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("NOTE_TYPE");
                        Number amountNum = doc.getDouble("NOTE_AMOUNT");

                        // Defensive check: Firestore may store numbers as Long or Double
                        double amount = amountNum != null ? amountNum.doubleValue() : 0;

                        if ("Credit".equalsIgnoreCase(type)) {
                            totalCredit += amount;
                        } else if ("Debit".equalsIgnoreCase(type)) {
                            totalDebit += amount;
                        } else if ("Investment".equalsIgnoreCase(type)) {
                            totalInvestment += amount;
                        }
                    }

                    double availableBalance = totalCredit - (totalDebit + totalInvestment);

                    CreditAmount.setText(String.format("₹%.2f",totalCredit));
                    DebitAmount.setText(String.format("₹%.2f",totalDebit));
                    InvestmentAmount.setText(String.format("₹%.2f",totalInvestment));
                    AvailableBalance.setText(String.format("₹%.2f",availableBalance));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void calculateBalanceFromFirestoreForCurrentMonthNotes() {
        db.collection("Notes")
                .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalCredit = 0;
                    double totalDebit = 0;
                    double totalInvestment = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("NOTE_TYPE");
                        Number amountNum = doc.getDouble("NOTE_AMOUNT");

                        // Defensive check: Firestore may store numbers as Long or Double
                        double amount = amountNum != null ? amountNum.doubleValue() : 0;

                        if ("Credit".equalsIgnoreCase(type)) {
                            totalCredit += amount;
                        } else if ("Debit".equalsIgnoreCase(type)) {
                            totalDebit += amount;
                        } else if ("Investment".equalsIgnoreCase(type)) {
                            totalInvestment += amount;
                        }
                    }

                    double availableBalance = totalCredit - (totalDebit + totalInvestment);

                    CreditAmount.setText(String.format("₹%.2f",totalCredit));
                    DebitAmount.setText(String.format("₹%.2f",totalDebit));
                    InvestmentAmount.setText(String.format("₹%.2f",totalInvestment));
                    AvailableBalance.setText(String.format("₹%.2f",availableBalance));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}
