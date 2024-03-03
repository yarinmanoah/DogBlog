package com.example.dogblog.utils;

import com.example.dogblog.model.WalkType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeConverter {

    public static LocalDate longToLocalDate(long date) {
        return Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static long localDateToLong(LocalDate date) {
        return date.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public static String localDateToString(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(Constants.FORMAT_DATE));
    }

    public static String longToStringDate(long date) {
        return DateTimeConverter.longToLocalDate(date).format(DateTimeFormatter.ofPattern(Constants.FORMAT_DATE));
    }

    public static LocalDateTime longToLocalDateTime(long dateTime) {
        return Instant.ofEpochMilli(dateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static long localDateTimeToLong(LocalDateTime dateTime) {
        return dateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    public static String localDateTimeToStringDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(Constants.FORMAT_DATE));
    }

    public static String localDateTimeToStringTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME));
    }

    public static String longToStringTime(long dateTime) {
        return DateTimeConverter.longToLocalDateTime(dateTime).format(DateTimeFormatter.ofPattern(Constants.FORMAT_TIME));
    }

    public static int durationToDurationInMinutes(int minutes, int hours) {
        return minutes + hours * 60;
    }

    public static String durationToString(int hours, int minutes) {
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    public static String durationInMinutesToString(int durationInMinutes) {
        int hours = durationInMinutes / 60;
        int minutes = durationInMinutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }



}
