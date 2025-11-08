package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HelplineActivity extends AppCompatActivity {

    LinearLayout policeLayout, womenLayout, ambulanceLayout, nationalLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpline);

        // Bind layouts
        policeLayout = findViewById(R.id.policeLayout);
        womenLayout = findViewById(R.id.womenLayout);
        ambulanceLayout = findViewById(R.id.ambulanceLayout);
        nationalLayout = findViewById(R.id.nationalLayout);

        // Click Listeners
        policeLayout.setOnClickListener(v -> makeCall("100"));
        womenLayout.setOnClickListener(v -> makeCall("1091"));
        ambulanceLayout.setOnClickListener(v -> makeCall("102"));
        nationalLayout.setOnClickListener(v -> makeCall("112"));
    }

    private void makeCall(String number) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + number));
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to make call: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
