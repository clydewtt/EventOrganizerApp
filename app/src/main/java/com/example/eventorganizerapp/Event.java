package com.example.eventorganizerapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Event implements Serializable {
    private String eventTitle;
    private String eventLocation;
    private String eventDescription;
    private Date eventDate;
    private ArrayList<String> attendeeUserIDs;

    public Event() {
        this.eventTitle = "";
        this.eventLocation = "";
        this.eventDescription = "";
        this.eventDate = null;
        this.attendeeUserIDs = null;
    }

    public Event(String eventTitle, String eventLocation, String eventDescription, Date eventDate, ArrayList<String> attendeeUserIDs) {
        this.eventTitle = eventTitle;
        this.eventLocation = eventLocation;
        this.eventDescription = eventDescription;
        this.eventDate = eventDate;
        this.attendeeUserIDs = attendeeUserIDs;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public ArrayList<String> getAttendeeUserIDs() {
        return attendeeUserIDs;
    }

    public void setAttendeeUserIDs(ArrayList<String> attendeeUserIDs) {
        this.attendeeUserIDs = attendeeUserIDs;
    }
}
