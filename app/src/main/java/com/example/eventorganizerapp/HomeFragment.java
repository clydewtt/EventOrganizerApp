package com.example.eventorganizerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.collect.Ordering;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@SuppressLint("ViewHolder, InflateParams")
public class HomeFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private final ArrayList<Event> events = new ArrayList<>();
    private ArrayList<String> usersEventIDs = new ArrayList<>();
    private User user;

    private TextView noEventsTextView;
    private ListView eventsListView;
    private CircularProgressIndicator loadingCircle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_home, container, false);

        // Initializes the views from the XML files
        initializeViews(root);

        getUserDataFromDB();

        return root;
    }

    private void initializeViews(View root) {
        eventsListView = root.findViewById(R.id.events_list_view);
        noEventsTextView = root.findViewById(R.id.no_events_text);
        loadingCircle = root.findViewById(R.id.progress_circle_home);
    }

    private void getUserDataFromDB() {
        db.collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    user = task.getResult().toObject(User.class);

                    if (user != null) {
                        usersEventIDs = user.getEventsAttended();
                    }

                    // Now that the user is loaded, we can get the events that they're signed up for.
                    getEventsDataFromDB();
                }
            }
            else {
                Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getEventsDataFromDB() {
        db.collection("Events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    Event event = documentSnapshot.toObject(Event.class);
                    String eventID = documentSnapshot.getId();

                    // usersEventIDs would be null if they were not signed up for any events.
                    if (usersEventIDs != null) {
                        for (String userEventID : usersEventIDs) {
                            // Pulls only the events that the user has on their list.
                            if (userEventID.equals(eventID)) {
                                events.add(event);
                            }
                        }
                    }
                }

                // NOTE: This code is run only after the event data is loaded from the DB.

                // Hide the loading circle, because the data is loaded.
                loadingCircle.setVisibility(View.GONE);

                // In the event that the user is signed up for no events hide the list view
                // and tell them that they have no events.
                if (events.isEmpty()) {
                    eventsListView.setVisibility(View.GONE);
                    noEventsTextView.setVisibility(View.VISIBLE);
                } else {
                    noEventsTextView.setVisibility(View.GONE);
                    eventsListView.setVisibility(View.VISIBLE);
                }

                // Sorts the events by date, most recent to oldest.
                events.sort(new Ordering<Event>() {
                    @Override
                    public int compare(@NullableDecl Event left, @NullableDecl Event right) {
                        return right.getEventDate().compareTo(left.getEventDate());
                    }
                });

                // Sets the list adapter for the events to be displayed.
                EventsListAdapter listAdapter = new EventsListAdapter();
                eventsListView.setAdapter(listAdapter);

            } else {
                Toast.makeText(getContext(), "Error loading events" + task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class EventsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return events.size();
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
            View eventListItemLayout = getLayoutInflater().inflate(R.layout.list_item_event, null);

            // The views from the custom layout that I made
            MaterialCardView eventCard = eventListItemLayout.findViewById(R.id.card_view_event);
            TextView eventDateView = eventListItemLayout.findViewById(R.id.date_and_time_event_list_item);
            TextView eventTitleView = eventListItemLayout.findViewById(R.id.event_title_list_item);
            TextView eventLocationView = eventListItemLayout.findViewById(R.id.location_list_item);

            Event event = events.get(i);
            Date eventDate = event.getEventDate();

            // If the event passed, gray it out.
            if (eventDate.before(new Date())) {
                eventCard.setAlpha(0.5f);
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, hh:mm a", Locale.US);
            eventDateView.setText(dateFormatter.format(eventDate));
            eventTitleView.setText(event.getEventTitle());
            eventLocationView.setText(event.getEventLocation());

            // When the user presses an event card, send them to the page with more details
            eventCard.setOnClickListener(view1 -> {
                Intent goToEventDetails = new Intent(getContext(), EventDetailsActivity.class);
                goToEventDetails.putExtra("event", events.get(i));
                startActivity(goToEventDetails);
            });

            return eventListItemLayout;
        }
    }
}