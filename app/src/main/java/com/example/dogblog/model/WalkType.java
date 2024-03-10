package com.example.dogblog.model;

import com.example.dogblog.utils.Constants;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class WalkType {
    private long time;
    private int durationInMinutes;
    private boolean poop;
    private boolean pee;
    private boolean play;
    private double rate;
    private String name;

    public WalkType() {
    }

    public long getTime() {
        return time;
    }

    public WalkType setTime(long time) {
        this.time = time;
        return this;
    }

    public String getName() {
        return name;
    }

    public WalkType setName(String name) {
        this.name = name;
        return this;
    }

    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    public WalkType setDurationInMinutes(int durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
        return this;
    }

    public boolean getPoop() {
        return poop;
    }

    public WalkType setPoop(boolean poop) {
        this.poop = poop;
        return this;
    }

    public boolean getPee() {
        return pee;
    }

    public WalkType setPee(boolean pee) {
        this.pee = pee;
        return this;
    }

    public double getRate() {
        return rate;
    }

    public WalkType setRate(double rate) {
        this.rate = rate;
        return this;
    }

    public WalkType setDuration(int hours, int minutes) {
        this.durationInMinutes = minutes + hours * 60;
        return this;
    }

    public String getDurationAsString() {
        int hours = this.durationInMinutes / 60;
        int minutes = this.durationInMinutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    public WalkType setTimeFromString(String timeString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.FORMAT_TIME);
        this.time = LocalTime.parse(timeString, formatter).toSecondOfDay();
        return this;
    }

    public boolean getPlay() {
        return play;
    }

    public WalkType setPlay(boolean play) {
        this.play = play;
        return this;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalkType walkType = (WalkType) o;
        return Objects.equals(name, walkType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}