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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
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
    TextView CreditAmount, DebitAmount, InvestmentAmount, AvailableBalance, EmptyView;

    String TopFilter, SecondaryFilter, userMailId;


    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenses = new ArrayList<>();


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

        userMailId = firebaseAuth.getCurrentUser().getEmail().toString();



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CurrentmonthNotes = findViewById(R.id.currentMonthNotes);
        AllNotes = findViewById(R.id.allNotes);
        AllNoteDtls = findViewById(R.id.AllNoteDtls);
        CreditNoteDtls = findViewById(R.id.CreditNoteDtls);
        DebitNoteDtls = findViewById(R.id.DebitNoteDtls);
        InvestmentNoteDtls = findViewById(R.id.InvestmentNoteDtls);
        EmptyView = findViewById(R.id.emptyView);

        CreditAmount = findViewById(R.id.creditAmount);
        DebitAmount = findViewById(R.id.debitAmount);
        InvestmentAmount = findViewById(R.id.investmentAmount);
        AvailableBalance = findViewById(R.id.AvailableBalanceAmount);

        TopFilter="CURRENT_MONTH_NOTES";
        Button[] btnArray = {AllNotes};
        btnSelection(CurrentmonthNotes,btnArray);


        SecondaryFilter="ALL_NOTE_DTLS";
        Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
        btnSelection(AllNoteDtls,btnArray_secondFilter);

        //Recycler View changes
        recyclerView = findViewById(R.id.recyclerExpenses);
        adapter = new ExpenseAdapter(expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(adapter);




        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense deletedExpense = expenses.get(position);

                // Show confirmation dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove visually first
                            expenses.remove(position);
                            adapter.notifyItemRemoved(position);
                            calculateBalance(TopFilter);

                            Snackbar.make(recyclerView, "Note deleted", Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", v -> {
                                        // Restore item if UNDO pressed
                                        expenses.add(position, deletedExpense);
                                        adapter.notifyItemInserted(position);
                                        recyclerView.scrollToPosition(position);
                                        calculateBalance(TopFilter);
                                    })
                                    .addCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                                                // Only delete from Firestore if UNDO NOT pressed
                                                db.collection("Notes")
                                                        .document(deletedExpense.getDocId())
                                                        .delete()
                                                        .addOnSuccessListener(aVoid ->{
                                                            displayEmptyView();
                                                            calculateBalance(TopFilter);
                                                            Toast.makeText(MainActivity.this, "Note deleted permanently", Toast.LENGTH_SHORT).show();
                                                        })
                                                            .addOnFailureListener(e -> {
                                                            // Restore item if Firestore delete fails
                                                            expenses.add(position, deletedExpense);
                                                            adapter.notifyItemInserted(position);
                                                                calculateBalance(TopFilter);
                                                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }
                                    })
                                    .show();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Restore item if user cancels
                            adapter.notifyItemChanged(position);
                            calculateBalance(TopFilter);
                            dialog.dismiss();
                        })
                        .setCancelable(false)
                        .show();
            }

        });
        itemTouchHelper.attachToRecyclerView(recyclerView);




        //Call function load data when openening the app
        fetchNoteDtls(TopFilter, SecondaryFilter);
        //Call dashboard display function to display data when opening the app
        calculateBalance(TopFilter);



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
                calculateBalance(TopFilter);


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
                calculateBalance(TopFilter);

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
    protected void onResume() {
        super.onResume();

        // Refresh your RecyclerView + dashboard totals every time user comes back
        fetchNoteDtls(TopFilter, SecondaryFilter);

        calculateBalance(TopFilter);
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {

        getMenuInflater().inflate(R.menu.main, menu);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            MenuItem accountItem = menu.findItem(R.id.accountDetails);
             accountItem.setTitle(user.getEmail()); // show logged-in email
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.accountDetails) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Account Info")
                        .setMessage("Logged in as:\n" + userMailId)
                        .setPositiveButton("Logout", (dialog, which) -> {
                            firebaseAuth.signOut();
                            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), Login.class));
                            finish();
                        })
                        .setNegativeButton("Close", null)
                        .show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        String noteCategory = document.getString("NOTE_CATEGORY");
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
        Expense expense = new Expense(noteType, noteCategory, noteDescription, noteDate, noteAmount, iconResId);
        expense.setDocId(document.getId()); // 🔑 save Firestore docId
        expenses.add(expense);
    }

    public void displayEmptyView(){
        if (expenses == null || expenses.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            EmptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            EmptyView.setVisibility(View.GONE);
        }
    }

    public void fetchNoteDtls(String TopFilter, String SecondaryFilter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String CurrentUserMailId = user.getEmail();
        Query query = null;

        // Example: ALL_NOTES + ALL_NOTE_DTLS
        if ("ALL_NOTES".equals(TopFilter) && "ALL_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("ALL_NOTES".equals(TopFilter) && "CREDIT_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE", "Credit")
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("ALL_NOTES".equals(TopFilter) && "DEBIT_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE", "Debit")
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("ALL_NOTES".equals(TopFilter) && "INVESTMENT_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE", "Investment")
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("CURRENT_MONTH_NOTES".equals(TopFilter) && "ALL_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("CURRENT_MONTH_NOTES".equals(TopFilter) && "CREDIT_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE", "Credit")
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("CURRENT_MONTH_NOTES".equals(TopFilter) && "DEBIT_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE", "Debit")
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }
        else if ("CURRENT_MONTH_NOTES".equals(TopFilter) && "INVESTMENT_NOTE_DTLS".equals(SecondaryFilter)) {
            query = db.collection("Notes")
                    .whereEqualTo("CREATED_BY", CurrentUserMailId)
                    .whereEqualTo("NOTE_TYPE", "Investment")
                    .whereGreaterThanOrEqualTo("NOTE_DATE",getFirstDayOfMonth())
                    .orderBy("NOTE_DATE", Query.Direction.DESCENDING);
        }

        if (query != null) {
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    expenses.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        constructNotesData(document, expenses);
                    }
                    adapter.notifyDataSetChanged(); // 🔑 update existing adapter

                    displayEmptyView();


                } else {
                    Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void calculateBalance(String TopFilter) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null || user.getEmail().isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String CurrentUserMailId = user.getEmail();
        Query query = db.collection("Notes").whereEqualTo("CREATED_BY", CurrentUserMailId);

        // ✅ If Current Month, add date filter
        if ("CURRENT_MONTH_NOTES".equals(TopFilter)) {
            query = query.whereGreaterThanOrEqualTo("NOTE_DATE", getFirstDayOfMonth());
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalCredit = 0;
                    double totalDebit = 0;
                    double totalInvestment = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String type = doc.getString("NOTE_TYPE");
                        Double amount = doc.getDouble("NOTE_AMOUNT");

                        if (amount == null) amount = 0.0;

                        switch (type != null ? type : "") {
                            case "Credit":
                                totalCredit += amount;
                                break;
                            case "Debit":
                                totalDebit += amount;
                                break;
                            case "Investment":
                                totalInvestment += amount;
                                break;
                        }
                    }

                    double availableBalance = totalCredit - (totalDebit + totalInvestment);

                    // ✅ Update UI
                    CreditAmount.setText(String.format("₹%.2f", totalCredit));
                    DebitAmount.setText(String.format("₹%.2f", totalDebit));
                    InvestmentAmount.setText(String.format("₹%.2f", totalInvestment));
                    AvailableBalance.setText(String.format("₹%.2f", availableBalance));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }







}
