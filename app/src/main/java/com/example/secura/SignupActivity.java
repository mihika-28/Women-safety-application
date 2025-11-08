package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    EditText nameInput, phoneInput, passwordInput;
    Button signupBtn;
    TextView loginText;

    public static final String PREFS_NAME = "user"; // same file used in login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean("logged_in", false)) {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_signup);

        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupBtn = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);

        signupBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.matches("\\d{10}")) {
                Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.matches("(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}")) {
                Toast.makeText(this, "Password must contain letters & numbers (min 6 chars)", Toast.LENGTH_LONG).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name", name);
            editor.putString("phone", phone);
            editor.putString("password", password);
            editor.putBoolean("logged_in", true);
            editor.apply();

            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();

            // Redirect to TrustedContactsActivity after signup
            Intent intent = new Intent(SignupActivity.this, TrustedContactsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
        });
    }
}
