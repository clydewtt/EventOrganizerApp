package com.example.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private TextView loginTextLayout;
    private EditText fullNameEditText, emailAddressEditText, passwordEditText;
    private MaterialCheckBox positiveCheckBox;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views from the XML file.
        initializeViews();

        // When the user clicks login, send them to the login screen with an animation.
        loginTextLayout.setOnClickListener(view -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));

            finish();
        });

        // When the user clicks the nextButton, register them as a user. [Call createUser()]
        nextButton.setOnClickListener(view -> createUser());
    }

    private void initializeViews() {
        loginTextLayout = findViewById(R.id.log_in_text_layout);
        fullNameEditText = findViewById(R.id.full_name_signup_edit_text);
        emailAddressEditText = findViewById(R.id.email_address_signup_edit_text);
        passwordEditText = findViewById(R.id.password_signup_edit_text);
        positiveCheckBox = findViewById(R.id.positive_status_checkbox);
        nextButton = findViewById(R.id.next_button_signup);
    }

    private void createUser() {
        // Getting user input from edit text
        String fullNameInput = fullNameEditText.getText().toString();
        String emailAddressInput = emailAddressEditText.getText().toString();
        String passwordInput = passwordEditText.getText().toString();
        boolean isUserCOVIDPositive = positiveCheckBox.isChecked();

        // These are checks before the user can be registered
        if (fullNameInput.isEmpty()) {
            fullNameEditText.setError("Name cannot be empty");
        }
        else if (emailAddressInput.isEmpty()){
            emailAddressEditText.setError("Email cannot be empty.");
            emailAddressEditText.requestFocus();
        }
        else if (passwordInput.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            passwordEditText.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(emailAddressInput, passwordInput)
                    .addOnCompleteListener(task -> {
                        // The user has been successfully registered.
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account created successfully.", Toast.LENGTH_SHORT).show();

                            // Make a local user object and a user document in Firebase.
                            User user = new User(fullNameInput, emailAddressInput, mAuth.getCurrentUser().getUid(), isUserCOVIDPositive);
                            db.collection("Users").document(user.getUserID()).set(user);

                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        }

                        // The user has been unsuccessfully registered.
                        else {
                            Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}