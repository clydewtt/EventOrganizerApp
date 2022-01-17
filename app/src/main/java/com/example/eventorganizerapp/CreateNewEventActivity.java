package com.example.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams"})
public class CreateNewEventActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private EditText eventTitleEditText, dateEditText, startTimeEditText, eventDescriptionEditText, eventLocationEditText;
    private MaterialButton doneButton;
    private ListView participantListView;

    private final Calendar calendar = Calendar.getInstance(); // Gets current date information
    private String eventStateLocation = "";

    private final HashMap<String, String> stateRestrictions = new HashMap<>();
    private final ArrayList<String> attendeeUserIDs = new ArrayList<>();
    private final ArrayList<User> friends = new ArrayList<>();
    private ArrayList<String> friendIDs = new ArrayList<>();
    private User localUser;


    private final String[] usStates = {"Alabama", "Alaska", "Arizona", "Arkansas", "California", "Colorado", "Connecticut", "Delaware", "District of Columbia","Florida", "Georgia", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia", "Wisconsin", "Wyoming"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        // Initializes the views from the XML.
        initializeViews();

        // Gets the information to show to the user depending on the state
        getCDCGatheringRestrictions();

        // When the user presses the dateEditText, show the date picker
        dateEditText.setOnClickListener(view -> showDatePicker());

        // When the user presses the startTimeEditText, show the time picker
        startTimeEditText.setOnClickListener(view -> showTimePicker());

        // When the user presses the eventLocationEditText, show the location picker
        eventLocationEditText.setOnClickListener(view -> showLocationPickerDialog());

        // When the user presses the done button, create the event.
        doneButton.setOnClickListener(view -> createEvent());

        getUserData();
    }

    private void initializeViews() {
        eventTitleEditText = findViewById(R.id.event_title_edit_text);
        dateEditText = findViewById(R.id.date_edit_text);
        startTimeEditText = findViewById(R.id.time_edit_text);
        eventDescriptionEditText = findViewById(R.id.event_description_edit_text);
        eventLocationEditText = findViewById(R.id.event_location_edit_text);
        doneButton = findViewById(R.id.done_button_create_new_event);

        participantListView = findViewById(R.id.participants_list_view);
    }

    private void showDatePicker() {
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // When the user chooses a date, update the dateEditText
        DatePickerDialog datePicker = new DatePickerDialog(this, (datePicker1, year, month, day) -> {
            calendar.set(year, month, day);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            dateEditText.setText(dateFormat.format(calendar.getTime()));
        }, currentYear, currentMonth, currentDay);

        datePicker.show();
    }

    private void showTimePicker(){
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // When the user chooses a time, update the timeEditText
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (timePicker, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            startTimeEditText.setText(timeFormat.format(calendar.getTime()));
        }, currentHour, currentMinute, false);

        timePickerDialog.show();
    }

    private void showLocationPickerDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CreateNewEventActivity.this);
        dialogBuilder.setTitle("Choose the event's state location.");

        // When the user clicks a state on the listview, update the edit text and show the CDC guidelines
        // for the selected state.
        dialogBuilder.setItems(usStates, (dialogInterface, i) -> {
            eventStateLocation = usStates[i];
            eventLocationEditText.setText(eventStateLocation);

            showCDCGuidelinesDialog(eventStateLocation);
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }
    private void getUserData() {
        db.collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                localUser = task.getResult().toObject(User.class);
                friendIDs = localUser.getFriendUserIDs();

                if (friendIDs != null) {
                    getFriends();
                }
            }
        });
    }

    private void getFriends() {
        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    String userID = documentSnapshot.getId();
                    User user = documentSnapshot.toObject(User.class);

                    // Gets the friends in order to add them to the list of addable participants.
                    for (String friendID : friendIDs) {
                        if (userID.equals(friendID)) {
                            friends.add(user);
                        }
                    }
                }

                // Sets the listadapter and links it with the listview
                ParticipantsListAdapter listAdapter = new ParticipantsListAdapter();
                participantListView.setAdapter(listAdapter);

                // This is needed in order to show all items without them being cut off.
                setListViewHeightBasedOnItems();

            }
        });
    }

    private void getCDCGatheringRestrictions() {
        db.collection("GatheringRestrictions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    // Gets the state restrictions and saves it in a HashMap
                    String stateName = documentSnapshot.getId();
                    String stateRestriction = (String) documentSnapshot.get("restriction");

                    stateRestrictions.put(stateName, stateRestriction);
                }
            } else {
                Toast.makeText(CreateNewEventActivity.this, "Error retrieving CDC information.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCDCGuidelinesDialog(String state) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CreateNewEventActivity.this);
        dialogBuilder.setTitle(state + " Restrictions");
        dialogBuilder.setMessage(stateRestrictions.get(state));
        dialogBuilder.setCancelable(true);
        dialogBuilder.setPositiveButton("Ok", null);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void createEvent() {
        // Gets the user input from the edit text fields
        String eventTitle = eventTitleEditText.getText().toString();
        String eventDay = dateEditText.getText().toString();
        String eventTime = startTimeEditText.getText().toString();
        String eventDescription = eventDescriptionEditText.getText().toString();

        // Checks all of the requirements before creating the event.
        if (eventTitle.isEmpty()) {
            eventTitleEditText.setError("Title cannot be empty.");
            eventTitleEditText.requestFocus();
        }
        else if (eventDay.isEmpty()) {
            dateEditText.setError("Please choose a date.");
            dateEditText.requestFocus();
        }
        else if (eventTime.isEmpty()) {
            startTimeEditText.setError("Please choose a time.");
            startTimeEditText.requestFocus();
        }
        else if (eventDescription.isEmpty()) {
            eventDescriptionEditText.setError("Description cannot be empty.");
            eventDescriptionEditText.requestFocus();
        }
        else {
            Date eventDate = calendar.getTime();
            attendeeUserIDs.add(firebaseUser.getUid());

            // Makes a documentID for the event document for Firebase
            String eventDocumentID = db.collection("Events").document().getId();

            // Create event object and save it to Firebase.
            Event event = new Event(eventTitle, eventStateLocation, eventDescription, eventDate, attendeeUserIDs);
            db.collection("Events").document(eventDocumentID).set(event)
                    .addOnFailureListener(e -> Toast.makeText(CreateNewEventActivity.this,
                            "Error creating event: " + e.toString(), Toast.LENGTH_SHORT).show());

            // Update the events that the user has attended in Firebase
            db.collection("Users").document(firebaseUser.getUid()).update("eventsAttended", FieldValue.arrayUnion(eventDocumentID));

            // Updates the event field for the attendees as well.
            for (String attendeeID : attendeeUserIDs) {
                db.collection("Users").document(attendeeID).update("eventsAttended", FieldValue.arrayUnion(eventDocumentID));

            }

            startActivity(new Intent(CreateNewEventActivity.this, MainActivity.class));
            finish();
        }

    }

    private class ParticipantsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friends.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View userLayout = getLayoutInflater().inflate(R.layout.list_item_user, null);

            TextView usernameView = userLayout.findViewById(R.id.user_username_list_item);
            MaterialButton addButton = userLayout.findViewById(R.id.add_button);

            usernameView.setText(friends.get(i).getName());
            addButton.setVisibility(View.VISIBLE);

            // Adds user to list of participants for the event
            addButton.setOnClickListener(view1 -> {
                attendeeUserIDs.add(friends.get(i).getUserID());
                addButton.setEnabled(false);
                addButton.setText("Added");
            });

            return  userLayout;
        }
    }

    private void setListViewHeightBasedOnItems() {

        ParticipantsListAdapter listAdapter = (ParticipantsListAdapter) participantListView.getAdapter();
        if (listAdapter != null) {

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < listAdapter.getCount(); itemPos++) {
                View item = listAdapter.getView(itemPos, null, participantListView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = participantListView.getDividerHeight() *
                    (listAdapter.getCount() - 1);

            // Set list height.
            ViewGroup.LayoutParams params = participantListView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            participantListView.setLayoutParams(params);
        }

    }
}
