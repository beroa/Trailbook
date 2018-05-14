package com.trailbook.trailbook.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface RunDao {
    @Query("SELECT * FROM RunData")
    List<RunData> getAll();

    @Query("SELECT * FROM (" +
            "SELECT t1.*" +
            " FROM RunData t1" +
            " LEFT OUTER JOIN RunData t2" +
            " ON (t1.trailId = t2.trailId AND t1.startTime < t2.startTime)" +
            " WHERE t2.trailId IS NULL" +
            " )" +
            "WHERE trailId LIKE :trailId;")
    RunData getRecentByTrailId(String trailId);

    @Query("SELECT * FROM RunData WHERE trailId LIKE :trailId")
    List<RunData> getRunsByTrailId(String trailId);

    @Query("SELECT COUNT(*) FROM RunData WHERE trailId LIKE :trailId")
    int countRunsForTrailId(String trailId);

    @Query("SELECT * FROM RunData WHERE id LIKE :id LIMIT 1")
    RunData getById(String id);

    @Query("SELECT COUNT(*) FROM RunData")
    int countAll();

    @Insert
    void insertAll(RunData... runs);

    @Delete
    void delete(RunData run);

    @Query("DELETE FROM RunData")
    void nukeTable();
}