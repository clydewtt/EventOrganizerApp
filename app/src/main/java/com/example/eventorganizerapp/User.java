package com.example.eventorganizerapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class User implements Serializable {
    private String name;
    private String email;
    private String userID;
    private boolean isUserCOVIDPositive;
    private ArrayList<String> friendUserIDs; // friend's userIDs
    private ArrayList<String> eventsAttended; // eventIDs
    private HashMap<Date, Boolean> covidStatusHistory; // Key: Day, Value: Positive or Negative status

    public User() {
        this.name = "";
        this.email = "";
        this.userID = "";
        this.isUserCOVIDPositive = false;
        this.friendUserIDs = null;
        this.eventsAttended = null;
        this.covidStatusHistory = null;
    }

    public User(String name, String email, String userID, boolean isUserCOVIDPositive) {
        this.name = name;
        this.email = email;
        this.userID = userID;
        this.isUserCOVIDPositive = isUserCOVIDPositive;
    }

    public User(String name, String email, String userID, boolean isUserCOVIDPositive, ArrayList<String> friendUserIDs, ArrayList<String> eventsAttended, HashMap<Date, Boolean> covidStatusHistory) {
        this.name = name;
        this.email = email;
        this.userID = userID;
        this.isUserCOVIDPositive = isUserCOVIDPositive;
        this.friendUserIDs = friendUserIDs;
        this.eventsAttended = eventsAttended;
        this.covidStatusHistory = covidStatusHistory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isUserCOVIDPositive() {
        return isUserCOVIDPositive;
    }

    public void setUserCOVIDPositive(boolean userCOVIDPositive) {
        isUserCOVIDPositive = userCOVIDPositive;
    }

    public ArrayList<String> getFriendUserIDs() {
        return friendUserIDs;
    }

    public void setFriendUserIDs(ArrayList<String> friendUserIDs) {
        this.friendUserIDs = friendUserIDs;
    }

    public ArrayList<String> getEventsAttended() {
        return eventsAttended;
    }

    public void setEventsAttended(ArrayList<String> eventsAttended) {
        this.eventsAttended = eventsAttended;
    }

    public HashMap<Date, Boolean> getCovidStatusHistory() {
        return covidStatusHistory;
    }

    public void setCovidStatusHistory(HashMap<Date, Boolean> covidStatusHistory) {
        this.covidStatusHistory = covidStatusHistory;
    }
}
