package com.example.secura;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class CallHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DBHelper dbHelper;
    ArrayList<HashMap<String, String>> fakeCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_history);

        recyclerView = findViewById(R.id.callHistoryRecyclerView);
        dbHelper = new DBHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadHistory();
    }

    private void loadHistory() {
        fakeCalls = dbHelper.getAllFakeCalls();
        CallHistoryAdapter adapter = new CallHistoryAdapter(this, fakeCalls);
        recyclerView.setAdapter(adapter);
    }
}
