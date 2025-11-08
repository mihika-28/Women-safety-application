package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText phoneInput, passwordInput;
    Button loginBtn;

    public static final String PREFS_NAME = "user"; //  SAME file name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);


        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loginBtn.setOnClickListener(v -> {
            String enteredPhone = phoneInput.getText().toString().trim();
            String enteredPassword = passwordInput.getText().toString().trim();

            String savedPhone = prefs.getString("phone", "NOT_FOUND");
            String savedPassword = prefs.getString("password", "NOT_FOUND");


            if (enteredPhone.equals(savedPhone) && enteredPassword.equals(savedPassword)) {
                prefs.edit().putBoolean("logged_in", true).apply();

                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid phone or password", Toast.LENGTH_SHORT).show();
            }
        });
        // Forgot Password navigation
        findViewById(R.id.forgotPassword).setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}
