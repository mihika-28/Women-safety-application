package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CallConnectedActivity extends AppCompatActivity {

    TextView connectedName;
    Button endCallBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_connected);

        connectedName = findViewById(R.id.connectedName);
        endCallBtn = findViewById(R.id.endCallBtn);

        // Get caller name safely
        String name = getIntent().getStringExtra("callerName");
        if (name == null || name.trim().isEmpty()) {
            name = "Unknown Caller";
        }

        connectedName.setText(name);

        //  No ringtone or audio in this screen (real call effect)
        // (Silent connected call — looks realistic, no duplicate sound)

        endCallBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Call Ended", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
