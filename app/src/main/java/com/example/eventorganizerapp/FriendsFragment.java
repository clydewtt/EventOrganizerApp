package com.example.eventorganizerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

@SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
public class FriendsFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    private ArrayList<String> friendIDs = new ArrayList<>();
    private final ArrayList<User> friends = new ArrayList<>();
    private final ArrayList<User> allUsers = new ArrayList<>();
    private final ArrayList<User> queryUserResults = new ArrayList<>();

    private MaterialCardView localUserCard;
    private User user;

    private LinearLayout mainContentLayout;
    private TextView currentUserName, textMessageFriends, searchResultsHeader;
    private ListView friendsListView, searchResultsListView;

    private SearchView searchView;
    private FriendsListAdapter friendsListAdapter;
    private QueriedUsersListAdapter queryListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        // Initialize views from the XML file.
        initializeViews(root);

        // When the user clicks their own card, send them to the ProfileActivity
        localUserCard.setOnClickListener(view ->
                startActivity(new Intent(getContext(), ProfileActivity.class)
                        .putExtra("user", user)));


        getUserDataFromDB();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Hides the main content and shows the search content
                mainContentLayout.setVisibility(View.GONE);
                searchResultsListView.setVisibility(View.VISIBLE);
                searchResultsHeader.setVisibility(View.VISIBLE);

                queryUserResults.clear(); // clears unwanted data

                // Searches for a matching username
                for (User user : allUsers) {
                    String username = user.getName();

                    if (s.length() <= username.length()) {
                        if (s.toUpperCase().equals(username.substring(0, s.length()).toUpperCase())) {
                            queryUserResults.add(user);
                        }
                    }
                }

                // If the query is empty, empty the results list as well.
                if (s.isEmpty()) {
                    queryUserResults.clear();
                    searchResultsHeader.setVisibility(View.GONE);
                    searchResultsListView.setVisibility(View.GONE);
                    mainContentLayout.setVisibility(View.VISIBLE);
                }

                // Links the searchResultsListView with the adapter so that the data can be displayed.
                queryListAdapter = new QueriedUsersListAdapter();
                searchResultsListView.setAdapter(queryListAdapter);

                return false;
            }
        });

        // Gets the close button of the search view.
        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) searchView.findViewById(searchCloseButtonId);

        // When the user closes the search view, hide the search content and show the main content.
        closeButton.setOnClickListener(view -> {
            searchView.clearFocus();
            searchView.setQuery("", false);
            searchResultsListView.setVisibility(View.GONE);
            searchResultsHeader.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.VISIBLE);
        });

        return root;
    }

    private void getUserDataFromDB() {
        db.collection("Users").document(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = task.getResult().toObject(User.class);
                currentUserName.setText(user.getName());

                friendIDs = user.getFriendUserIDs();
            }

            getAllUsers();
        });
    }

    private void getAllUsers() {
        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    User user = documentSnapshot.toObject(User.class);

                    // Add every user, except the current user
                    if (!user.getUserID().equals(firebaseUser.getUid())) {
                        allUsers.add(user);
                    }

                    // friendsIDs would be null if the user had no friends
                    if (friendIDs != null) {
                        for (String friendID : friendIDs) {
                            // Picks out the friends from the list of all users.
                            if (friendID.equals(user.getUserID())) {
                                friends.add(user);
                            }
                        }

                        // Links the friendsListView with the adapter
                        friendsListAdapter = new FriendsListAdapter();
                        friendsListView.setAdapter(friendsListAdapter);

                        // Hides the message that would say loading, and show the friends
                        textMessageFriends.setVisibility(View.GONE);
                        friendsListView.setVisibility(View.VISIBLE);

                    } else {
                        // friendsIDs is null and the user has no friends
                        textMessageFriends.setText("You have no friends.");
                    }
                }

            } else {
                Log.e("Error getting all users", task.getException().toString());
            }

        });

    }

    private void initializeViews(View root) {
        localUserCard = root.findViewById(R.id.current_user_card_view);
        currentUserName = root.findViewById(R.id.current_user_name_text);
        textMessageFriends = root.findViewById(R.id.text_message_friends);
        friendsListView = root.findViewById(R.id.friends_list_view);

        searchView = root.findViewById(R.id.search_friends_view);
        searchResultsHeader = root.findViewById(R.id.search_results_header);
        searchResultsListView = root.findViewById(R.id.search_results_list_view);
        mainContentLayout = root.findViewById(R.id.main_content_layout);
    }

    private class FriendsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return friendIDs.size();
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
            @SuppressLint("InflateParams") View friendsCardLayout = getLayoutInflater().inflate(R.layout.list_item_user, null);

            // Views from the custom card layout that displays users.
            TextView username = friendsCardLayout.findViewById(R.id.user_username_list_item);

            username.setText(friends.get(i).getName());

            return friendsCardLayout;
        }
    }

    private class QueriedUsersListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return queryUserResults.size();
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
            View userCardLayout = getLayoutInflater().inflate(R.layout.list_item_user, null);

            // Views from the custom card layout that displays users.
            MaterialButton addFriendBtn = userCardLayout.findViewById(R.id.add_button);
            TextView username = userCardLayout.findViewById(R.id.user_username_list_item);

            // This button set to View.GONE on default in order to make the layout reusable.
            addFriendBtn.setVisibility(View.VISIBLE);

            username.setText(queryUserResults.get(i).getName());

            // If the user does have friends...
            if (friendIDs != null) {
                for (String friendUid : friendIDs) {
                    // Tell the users which friends they have by saying that they are already added
                    // and disabling the addFriendBtn.
                    if (queryUserResults.get(i).getUserID().equals(friendUid)) {
                        addFriendBtn.setText("Added");
                        addFriendBtn.setEnabled(false);
                    }
                }
            }
            addFriendBtn.setOnClickListener(view1 -> {
                // If user originally had no friends, initialize the friendIDs list
                if (friendIDs == null) {
                    friendIDs = new ArrayList<>();
                }

                if (friendsListAdapter == null) {
                    friendsListAdapter = new FriendsListAdapter();
                    friendsListView.setAdapter(friendsListAdapter);
                }

                friendsListAdapter.notifyDataSetChanged();

                textMessageFriends.setVisibility(View.GONE);
                friendsListView.setVisibility(View.VISIBLE);

                // Add them to the user's friend list.
                friendIDs.add(queryUserResults.get(i).getUserID());
                friends.add(queryUserResults.get(i));

                // Save the new data in the Firebase database
                db.collection("Users").document(firebaseUser.getUid()).update(
                        "friendUserIDs", FieldValue.arrayUnion(queryUserResults.get(i).getUserID()));

                // Adds the current user to the friend list of the added user.
                db.collection("Users").document(queryUserResults.get(i).getUserID())
                        .update("friendUserIDs", FieldValue.arrayUnion(firebaseUser.getUid()));
            });

            return userCardLayout;
        }
    }
}