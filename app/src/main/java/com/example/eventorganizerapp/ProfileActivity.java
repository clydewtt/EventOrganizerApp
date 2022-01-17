package com.example.eventorganizerapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("SetTextI18n")
public class ProfileActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView usernameView, covidStatusView;
    private MaterialButton updateInfoBtn, signOutBtn;

    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initializes views from XML.
        initializeViews();

        // Gets intent from previous screen
        user = (User) getIntent().getSerializableExtra("user");

        usernameView.setText(user.getName());
        covidStatusView.setText(user.isUserCOVIDPositive() ? "Positive" : "Negative");

        // When the user clicks the updateInfoBtn, show the settings dialog
        updateInfoBtn.setOnClickListener(view -> showSettingsDialog());

        signOutBtn.setOnClickListener(view -> signOut());


    }

    private void initializeViews() {
        usernameView = findViewById(R.id.profile_username);
        covidStatusView = findViewById(R.id.covid_status_text_profile);
        updateInfoBtn = findViewById(R.id.update_information_button);
        signOutBtn = findViewById(R.id.sign_out_button);
    }

    private void showSettingsDialog() {
        // Creating a checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText("I have tested positive for COVID-19");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        checkBox.setChecked(user.isUserCOVIDPositive());
        linearLayout.addView(checkBox);

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setView(linearLayout);
        builder.setTitle("Settings");
        builder.setMessage("Note: Any possible close contact users will be notified if you are COVID-19 Positive.");
        builder.setCancelable(true);

        // When the user clicks on the check box, update data locally and on Firebase
        checkBox.setOnClickListener(view -> {
            user.setUserCOVIDPositive(checkBox.isChecked());
            String covidStatus;
            if (user.isUserCOVIDPositive()) {
                covidStatus = "Positive";
                sendCloseContactNotification();
                Toast.makeText(this, "Users were notified.", Toast.LENGTH_SHORT).show();
            } else {
                covidStatus = "Negative";
            }

            covidStatusView.setText(covidStatus);

            // Update the information in firebase
            db.collection("Users").document(user.getUserID()).update("userCOVIDPositive", user.isUserCOVIDPositive());
        });

        AlertDialog settingsDialog = builder.create();
        settingsDialog.show();

    }

    private void signOut() {
        // Signs out user
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
        finishAffinity();
    }

    private void sendCloseContactNotification() {
        // Make a POST request to Firebase Cloud Messaging to let them handle the sending out of
        // the notification to other users.
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject json = new JSONObject();

        try {
            json.put("to", "/topics/" + "COVIDNotification");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "Close Contact Notification");
            notificationObj.put("body", "You may have been in close contact with an individual at x event. Please refer to the CDC guidelines.");

            // Sends the notificationData
            json.put("notification", notificationObj);

            String URL = "https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    response -> Log.d("Notification", "Successfully sent."),
                    error -> Log.d("Notification", "onError: " + error.networkResponse)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AAAASZbqTJg:APA91bGiBdN97jRinE5V0SsPP0qtQ_YgbkkvOPYp9jkC54n72iotD_exOZKqlgAlfb3Kjj7RH8z6S_-R6LyOU-mWQa3H5-1We4-YLVZ2pDoxIgwGoTV4z3DDUFA_nZMO-jDDXOVWISXC");
                    return header;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}