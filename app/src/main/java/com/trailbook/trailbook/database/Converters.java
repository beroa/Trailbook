package com.trailbook.trailbook.database;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trailbook.trailbook.recording.Coords;

import java.lang.reflect.Type;

public class Converters {
    @TypeConverter
    public static Coords fromString(String value) {
        Type listType = new TypeToken<Coords>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromLatLng(Coords list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

//    @TypeConverter
//    public static Date fromTimestamp(Long value) {
//        return value == null ? null : new Date(value);
//    }
//
//    @TypeConverter
//    public static Long dateToTimestamp(Date date) {
//        return date == null ? null : date.getTime();
//    }
}