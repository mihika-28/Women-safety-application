package com.example.secura;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText phoneInput, newPasswordInput, confirmPasswordInput;
    Button resetPasswordBtn;
    ImageView toggleNewPassword, toggleConfirmPassword;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        prefs = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);

        // 🔹 Initialize UI elements
        phoneInput = findViewById(R.id.phoneInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        toggleNewPassword = findViewById(R.id.toggleNewPassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);

        // 🔹 Toggle visibility for new password
        toggleNewPassword.setOnClickListener(v ->
                togglePasswordVisibility(newPasswordInput, toggleNewPassword));

        // 🔹 Toggle visibility for confirm password
        toggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(confirmPasswordInput, toggleConfirmPassword));

        // 🔹 Reset password logic
        resetPasswordBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String enteredPhone = phoneInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        String savedPhone = prefs.getString("phone", "NOT_FOUND");

        if (enteredPhone.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!enteredPhone.equals(savedPhone)) {
            Toast.makeText(this, " Phone number not registered", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, " Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit().putString("password", newPassword).apply();

        Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
        finish();
    }

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        if (passwordField.getInputType() ==
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        }
        passwordField.setSelection(passwordField.getText().length());
    }
}
