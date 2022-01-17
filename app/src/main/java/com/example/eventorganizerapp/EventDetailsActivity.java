package com.example.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

@SuppressLint("ViewHolder, InflateParams")
public class EventDetailsActivity extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView eventTitleView, eventDateView, eventLocationView, eventDescriptionView;
    private Event event;
    private ListView participantListView;

    private ArrayList<String> attendeeUserIDs = new ArrayList<>();
    private final ArrayList<User> attendees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        // Gets event from previous screen
        event = (Event) getIntent().getSerializableExtra("event");

        // Initialize the views from the XML file
        initializeViews();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy @ hh:mm a", Locale.US);
        eventTitleView.setText(event.getEventTitle());
        eventDateView.setText(dateFormatter.format(event.getEventDate()));
        eventLocationView.setText(event.getEventLocation());
        eventDescriptionView.setText(event.getEventDescription());

        attendeeUserIDs = event.getAttendeeUserIDs();

        getParticipants();

    }

    private void getParticipants() {
        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    User user = documentSnapshot.toObject(User.class);
                    for (String attendeeUserID : attendeeUserIDs) {
                        if (attendeeUserID.equals(user.getUserID())) {
                            attendees.add(user);
                        }
                    }
                }

                ParticipantListAdapter listAdapter = new ParticipantListAdapter();
                participantListView.setAdapter(listAdapter);
                setListViewHeightBasedOnItems();
            }
        });
    }

    private void initializeViews() {
        eventTitleView = findViewById(R.id.event_details_title);
        eventDateView = findViewById(R.id.event_details_date_and_time);
        eventLocationView = findViewById(R.id.event_details_location);
        eventDescriptionView = findViewById(R.id.event_details_event_description);
        participantListView = findViewById(R.id.event_details_participants);
    }

    private class ParticipantListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return attendees.size();
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

            usernameView.setText(attendees.get(i).getName());

            return userLayout;
        }
    }

    private void setListViewHeightBasedOnItems() {

        ParticipantListAdapter listAdapter = (ParticipantListAdapter) participantListView.getAdapter();
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