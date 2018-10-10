package com.trailbook.app.recording;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Locale;

public class Coords extends ArrayList<LatLng> {
    private double distance = 0;

    @Override
    public boolean add(LatLng latLng) {
        boolean result = super.add(latLng);
        if (this.size() > 1) {
            updateDistance();
        }
        return result;
    }

    private void updateDistance() {
        distance += SphericalUtil.computeDistanceBetween(this.get( this.size() - 2), this.get( this.size() - 1));
    }

    public double getDistance() {
        return distance;
    }

    public double getDistanceInUnits(String units) {
        switch (units) {
            case "meters":
                return distance;
            case "kilometers":
                return distance / 1000.0;
            case "miles":
                return distance / 1609.0;
            default:
                return distance;
        }
    }

    public String getDistanceString(String units) {
        switch (units) {
            case "meters":
                return String.format(Locale.US, "%.1f %s", getDistanceInUnits(units), units);
            case "kilometers":
                return String.format(Locale.US, "%.3f %s", getDistanceInUnits(units), units);
            case "miles":
                return String.format(Locale.US, "%.3f %s", getDistanceInUnits(units), units);
            default:
                return String.format(Locale.US, "%.1f %s", getDistanceInUnits(units), "meters");
        }
    }

    public String getSpeedString(String distanceUnit, long duration) {
        double speed = getDistanceInUnits(distanceUnit) / (duration / (1000.0*60*60));
        return String.format(Locale.US, "%.1f %s per hour", speed, distanceUnit);
    }

    public double getBirdsDistance() {
        return SphericalUtil.computeLength(this);
    }

}
