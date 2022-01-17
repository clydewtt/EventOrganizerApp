package com.example.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private final FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private ViewPager2 viewPager;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the views from the XML file
        initializeViews();

        bottomNavigationView.setBackground(null); // Makes the background of the bottom nav clear and white.

        // Attaching the fragments to the parent MainActivity.
        setFragments();

        // When the user presses an icon on the bottom navigation bar...
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Scrolls the view pager to the appropriate page depending on the bottom nav icon clicked.
            switch (item.getItemId()) {
                case R.id.ic_home:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.ic_friends:
                    viewPager.setCurrentItem(1);
                    break;
            }
            return false;
        });

        // When the fab is pressed, send the user to the CreateNewEventActivity
        floatingActionButton.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, CreateNewEventActivity.class)));

        // Subscribes to the notification topic, which will allow the user to receive close contact notifications.
        firebaseMessaging.subscribeToTopic("COVIDNotification").addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("Error subscribing to notification topic", task.getException().toString());
            }
        });
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        viewPager = findViewById(R.id.view_pager);
        floatingActionButton = findViewById(R.id.fab);
    }

    private void setFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentManager, getLifecycle());

        // Link the viewpager and the fragmentAdapter so that they can work together :)
        viewPager.setAdapter(fragmentAdapter);

        // The MainActivity starts with the HomeFragment which is position 0.
        viewPager.setCurrentItem(0);

        // When the user changes the page by swiping, the bottom navigation bar icon should also
        // be updated to highlight the correct icon.
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // position 0 is the HomeFragment, 1 is the FriendsFragment
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.ic_home).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.ic_friends).setChecked(true);
                        break;
                }
            }
        });
    }
}