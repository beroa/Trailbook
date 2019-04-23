package com.trailbook.app.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TrailDao {
    @Query("SELECT * FROM TrailData")
    List<TrailData> getAll();

    @Query("SELECT trailName FROM TrailData")
    List<String> getAllTrailNames();

    @Query("SELECT * FROM TrailData WHERE trailName LIKE :trailName LIMIT 1")
    TrailData getByName(String trailName);

    @Query("SELECT * FROM TrailData WHERE id LIKE :id LIMIT 1")
    TrailData getById(String id);

    @Query("UPDATE TrailData SET trailName = :newName where id = :id")
    void renameById(String id, String newName);

    @Query("SELECT trailName FROM TrailData WHERE id LIKE :id LIMIT 1")
    String findNameById(String id);

    @Query("SELECT COUNT(*) FROM TrailData")
    int countAll();

    @Insert
    void insertAll(TrailData... trails);

    @Delete
    void delete(TrailData trail);



//    @Query("DELETE FROM traildata")
//    void nukeTable();
}