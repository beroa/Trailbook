package com.trailbook.app.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.trailbook.app.recording.Coords;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Entity(foreignKeys = @ForeignKey(entity = TrailData.class,
        parentColumns = "id",
        childColumns = "trailId"), indices = @Index("trailId"))
public class RunData {
    @PrimaryKey
    @NonNull
    private String id;

    private String trailId;
    private Coords coords;
    private long startTime;
    private long endTime;

    // setters/getters
    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String inpId) {
        id = inpId;
    }

    public String getTrailId() {
        return trailId;
    }

    public Coords getCoords() {
        return coords;
    }

    public void setTrailId(String newTrailId) {
        this.trailId = newTrailId;
    }

    public void setCoords(Coords newCoords) {
        this.coords = newCoords;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
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