package com.example.eventorganizerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreen extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    final private int DELAY_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Initializing the view from the XML file.
        View whiteOverlay = findViewById(R.id.white_overlay_splash_screen);

        // By animating the whiteOverlay's alpha level, there's a fade in effect.
        whiteOverlay.animate().alpha(0.0f).setDuration(2500).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // If the user is logged in, send them to the MainActivity, else they need
                    // to log in.
                    if (isUserLoggedIn()) {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class)
                                .putExtra("firebaseUser", mAuth.getCurrentUser()));

                    }

                    finish();
                }, DELAY_TIME); // The DELAY_TIME allows time for the user to look at the splash screen.
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

    }

    private boolean isUserLoggedIn() {
        // If getCurrentUser() returns null, the user is not signed in.
        return mAuth.getCurrentUser() != null;
    }
}