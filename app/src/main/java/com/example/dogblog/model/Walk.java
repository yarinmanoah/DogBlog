package com.example.dogblog.model;

import java.util.Locale;

public class Walk {

    private UserProfile owner;
    private long dateTime;
    private int durationInMinutes;
    private String note;
    private WalkType walkType;
    private boolean poop;
    private boolean pee;
    private boolean play;
    private double rate;
    private String name;

    public Walk() {
    }

    public UserProfile getOwner() {
        return owner;
    }

    public Walk setOwner(UserProfile owner) {
        this.owner = owner;
        return this;
    }

    public long getDateTime() {
        return dateTime;
    }

    public Walk setDateTime(long dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Walk setNote(String note) {
        this.note = note;
        return this;
    }

    public WalkType getWalkType() {
        return walkType;
    }

    public Walk setWalkType(WalkType walkType) {
        this.walkType = walkType;
        return this;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public Walk setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
        return this;
    }

    public boolean getPoop() {
        return poop;
    }

    public Walk setPoop(boolean poop) {
        this.poop = poop;
        return this;
    }

    public boolean getPee() {
        return pee;
    }

    public Walk setPee(boolean pee) {
        this.pee = pee;
        return this;
    }

    public double getRate() {
        return rate;
    }

    public Walk setRate(double rate) {
        this.rate = rate;
        return this;
    }

    public String getName() {
        return name;
    }

    public Walk setName(String name) {
        this.name = name;
        return this;
    }

    public Walk setDuration(int hours, int minutes) {
        this.durationInMinutes = minutes + hours * 60;
        return this;
    }

    public boolean getPlay() {
        return play;
    }

    public Walk setPlay(boolean play) {
        this.play = play;
        return this;
    }

    public String getDurationAsString() {
        int hours = this.durationInMinutes / 60;
        int minutes = this.durationInMinutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

}
