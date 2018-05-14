package com.trailbook.trailbook.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class TrailData {
    @PrimaryKey
    @NonNull
    private String id;
    private String trailName;
    private String contact;

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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

}