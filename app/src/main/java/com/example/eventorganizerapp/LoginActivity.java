package com.example.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private TextView signUpTextLayout;
    private EditText emailAddressEditText, passwordEditText;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views from the XML file.
        initializeViews();

        // When the user presses the sign up text...
        signUpTextLayout.setOnClickListener(view -> {
            // Send user to the SignupActivity
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        // When the user presses the nextButton, log them in
        nextButton.setOnClickListener(view -> logUserIn());
    }

    private void initializeViews() {
        signUpTextLayout = findViewById(R.id.sign_up_text_layout);
        emailAddressEditText = findViewById(R.id.email_address_login_edit_text);
        passwordEditText = findViewById(R.id.password_login_edit_text);
        nextButton = findViewById(R.id.next_button_login);
    }

    private void logUserIn() {
        // Getting user input from edit text
        String emailAddressInput = emailAddressEditText.getText().toString();
        String passwordInput = passwordEditText.getText().toString();

        // These are checks before the user can be logged in
        if (emailAddressInput.isEmpty()) {
            emailAddressEditText.setError("Email cannot be empty");
            emailAddressEditText.requestFocus();
        }
        else if (passwordInput.isEmpty()) {
            passwordEditText.setError("Password cannot be empty.");
            passwordEditText.requestFocus();
        }
        else {
            mAuth.signInWithEmailAndPassword(emailAddressInput, passwordInput)
                    .addOnCompleteListener(task -> {
                        // The user has been successfully logged in.
                        if (task.isSuccessful()) {
                            // Send the user to the MainActivity.
                            Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }

                        // The user has been unsuccessfully logged in.
                        else {
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}