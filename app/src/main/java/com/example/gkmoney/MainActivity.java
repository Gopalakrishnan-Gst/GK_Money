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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton addNote;
    FirebaseAuth firebaseAuth;

    Button CurrentmonthNotes, AllNotes, AllNoteDtls, CreditNoteDtls, DebitNoteDtls, InvestmentNoteDtls;
    TextView CreditAmount, DebitAmount, InvestmentAmount, AvailableBalance;

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

        Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
        btnSelection(AllNoteDtls,btnArray_secondFilter);



        CurrentmonthNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button[] btnArray = {AllNotes};
                btnSelection(CurrentmonthNotes,btnArray);
                //second level filter should reset
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(AllNoteDtls,btnArray_secondFilter);

            }
        });

        AllNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button[] btnArray = {CurrentmonthNotes};
                btnSelection(AllNotes,btnArray);
                //second level filter should reset
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(AllNoteDtls,btnArray_secondFilter);

            }
        });

        AllNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(AllNoteDtls,btnArray_secondFilter);
            }
        });

        CreditNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button[] btnArray_secondFilter = {AllNoteDtls,DebitNoteDtls,InvestmentNoteDtls};
                btnSelection(CreditNoteDtls,btnArray_secondFilter);
            }
        });

        DebitNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button[] btnArray_secondFilter = {CreditNoteDtls,AllNoteDtls,InvestmentNoteDtls};
                btnSelection(DebitNoteDtls,btnArray_secondFilter);
            }
        });

        InvestmentNoteDtls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button[] btnArray_secondFilter = {CreditNoteDtls,DebitNoteDtls,AllNoteDtls};
                btnSelection(InvestmentNoteDtls,btnArray_secondFilter);
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

        RecyclerView recyclerView = findViewById(R.id.recyclerExpenses);
        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense("Credit", "This is credit Note", "2025-07-15",1000, R.drawable.credit_icon));
        expenses.add(new Expense("Debit", "This is Debit Note", "2025-07-01",300, R.drawable.debit_icon));
        expenses.add(new Expense("Investment", "This is Investment Note", "2025-07-10",700, R.drawable.investment_icon));

        ExpenseAdapter adapter = new ExpenseAdapter(expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


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
        selectedBtn.setBackgroundColor(Color.DKGRAY);
        selectedBtn.setTextColor(Color.BLACK);
        for(Button button : deselectedBtn) {
            button.setSelected(false);
            button.setBackgroundColor(Color.LTGRAY);
            button.setTextColor(Color.WHITE);
        }
    }
}