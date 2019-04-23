package com.trailbook.app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.trailbook.app.recording.Coords;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Entity
public class TrailData {
    @PrimaryKey
    @NonNull
    private String id;
    private String trailName;
    private String contact;
    private Coords coords;
    private long startTime;
    private long endTime;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String newId) {
        this.id = newId;
    }

    public String getTrailName() {
        return trailName;
    }

    public void setTrailName(String newTrailName) {
        this.trailName = newTrailName;
    }

    String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public  void setCoords(Coords newCoords) {
        this.coords = newCoords;
    }

    public Coords getCoords() {
        return coords;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public String getStartTimeDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        Date resultdate = new Date( getStartTime() );
        return sdf.format(resultdate);
    }

    public String getStartTimeOfDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
        Date resultdate = new Date( getStartTime() );
        return sdf.format(resultdate);
    }

    public String makeDurationString(long duration) {
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        duration -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        StringBuilder sb = new StringBuilder(64);
        if (hours > 0) {
            sb.append(hours);
            if (hours == 1) {
                sb.append(" Hour ");
            } else {
                sb.append (" Hours ");
            }
        }
        if (minutes > 0) {
            sb.append(minutes);
            if (hours == 1) {
                sb.append(" Minute ");
            } else {
                sb.append (" Minutes ");
            }
        }
        sb.append(seconds);
        if (seconds == 1) {
            sb.append( " Second ");
        } else {
            sb.append( " Seconds ");
        }
        return sb.toString();
    }

    public String getDurationString() {
        return makeDurationString( getDuration() );
    }

}