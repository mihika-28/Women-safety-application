package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class TrustedContactsActivity extends AppCompatActivity {

    EditText contact1Name, contact1Number, contact2Name, contact2Number;
    Button saveBtn;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contacts);

        contact1Name = findViewById(R.id.contact1Name);
        contact1Number = findViewById(R.id.contact1Number);
        contact2Name = findViewById(R.id.contact2Name);
        contact2Number = findViewById(R.id.contact2Number);
        saveBtn = findViewById(R.id.saveContactsBtn);

        dbHelper = new DBHelper(this);

        //  Load any saved contacts
        loadContacts();

        saveBtn.setOnClickListener(v -> {
            String n1 = contact1Name.getText().toString().trim();
            String p1 = contact1Number.getText().toString().trim();
            String n2 = contact2Name.getText().toString().trim();
            String p2 = contact2Number.getText().toString().trim();

            if (p1.isEmpty() && p2.isEmpty()) {
                Toast.makeText(this, "Please add at least one phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            //  Normalize phone numbers (add +91 if missing)
            p1 = normalizePhoneNumber(p1);
            p2 = normalizePhoneNumber(p2);

            dbHelper.clearTrustedContacts();

            if (!p1.isEmpty()) dbHelper.saveTrustedContact(n1, p1);
            if (!p2.isEmpty()) dbHelper.saveTrustedContact(n2, p2);

            Toast.makeText(this, "Trusted contacts saved successfully ", Toast.LENGTH_SHORT).show();

            // Redirect to MainActivity (Home page)
            Intent intent = new Intent(TrustedContactsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //  No finish(); we want to keep app active
        });
    }

    //  Load contacts from DB
    private void loadContacts() {
        ArrayList<HashMap<String, String>> contacts = dbHelper.getAllTrustedNumbers();
        if (contacts.size() >= 1) {
            contact1Name.setText(contacts.get(0).get("name"));
            contact1Number.setText(contacts.get(0).get("phone"));
        }
        if (contacts.size() >= 2) {
            contact2Name.setText(contacts.get(1).get("name"));
            contact2Number.setText(contacts.get(1).get("phone"));
        }
    }

    //  Utility: ensure phone number format is valid (adds +91 if needed)
    private String normalizePhoneNumber(String number) {
        if (number.isEmpty()) return "";
        number = number.replaceAll("\\s+", ""); // remove spaces
        number = number.replaceAll("-", ""); // remove dashes

        // If not starting with + and has 10 digits, assume Indian number
        if (!number.startsWith("+") && number.length() == 10) {
            number = "+91" + number;
        }

        return number;
    }
}
