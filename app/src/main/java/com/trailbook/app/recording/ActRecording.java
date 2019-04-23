package com.trailbook.app.recording;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trailbook.app.R;
import com.trailbook.app.database.AppDatabase;
import com.trailbook.app.database.RunData;
import com.trailbook.app.database.TrailData;
import com.trailbook.app.launcher.ActLauncher;
import com.trailbook.app.saved_trail.ActSavedTrail;

import java.util.Timer;
import java.util.TimerTask;

public class ActRecording extends AppCompatActivity {
    private Context mContext;
    private TrailData trailData;
//    private RunData runData;
    private Coords coords = new Coords();
    private boolean isNewTrail;

    private GoogleMap mGoogleMap;
    private Polyline mPolyline;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private PowerManager.WakeLock wakeLock;

    private TextView tv_duration;
    private TextView tv_distance;
    private TextView tv_speed;

    private int mAutoFinish;

    private String prefDistanceUnits;
    private int prefGPSInterval;
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID_RECORDING = 0;
    private static final int NOTIFICATION_ID_AUTOCOMPLETED = 1;

    private boolean inBackground = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_recording);
        mContext = getApplicationContext();

        // initialize notification manager
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // get sharedPreferences
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefDistanceUnits = mSharedPreferences.getString(getString(R.string.pref_key_distance_units), "miles");
        prefGPSInterval = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_key_gps_interval), "10000"));
        setTitle("Recording");

        // initialize trailData
        unpackIntent();

        // initialize runData
//        runData = new RunData();
//        String runId = AppDatabase.newUUID();
//        runData.setId(runId);
//        runData.setTrailId(trailData.getId());
//        runData.setStartTime(System.currentTimeMillis());

        showRecordingNotification();
        startRecording();

        Button end = findViewById(R.id.btn_end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishTrail();
            }
        });

        TextView trailname = findViewById(R.id.tv_recording_name);
        tv_duration = findViewById(R.id.tv_recording_duration);
        tv_distance = findViewById(R.id.tv_recording_distance);
        tv_speed = findViewById(R.id.tv_recording_speed);
        trailname.setText(trailData.getTrailName());
        updateDistanceText();
        updateSpeedText(1);

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        inBackground = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        inBackground = false;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "You must end the current trail before you can go back.", Toast.LENGTH_LONG).show();
    }

    public void unpackIntent() {
        Intent intent = getIntent();
        isNewTrail = intent.getBooleanExtra("new-trail", false);
        mAutoFinish = intent.getIntExtra("auto-finish", 0);
        if (mAutoFinish == 0) {
            mAutoFinish = 5000; // if autofinish not defined, autofinish is 5 seconds
        } else {
            mAutoFinish = mAutoFinish * 60 * 60 * 1000;
        }
        String trailId;
        trailData = new TrailData();
        trailId = AppDatabase.newUUID();
        trailData.setId(trailId);
        trailData.setTrailName(intent.getStringExtra("trail-name"));
        trailData.setContact(intent.getStringExtra("emergency-contact"));
        trailData.setStartTime(System.currentTimeMillis());
    }

    public void startRecording() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "trailbook:TrailRecordingWakelock");
        }
        wakeLock.acquire(mAutoFinish);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapRecording);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mGoogleMap = map;
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(prefGPSInterval);
                mLocationRequest.setFastestInterval(prefGPSInterval);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        Log.d("TAG", "location" + location.toString());
                        LatLng c = new LatLng(location.getLatitude(), location.getLongitude());
                        // add coord
                        if (!coords.isEmpty()) {
                            // nonduplicate
                            if (c != coords.get(coords.size() - 1)) {
                                coords.add(c);
                                updateDistanceText();
                            }
                        } else {
                            // first coord
                            coords.add(c);
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(c, 15));
                        }
                        // update mPolyline
                        if (mPolyline != null) {
                            mPolyline.setPoints(coords);
                        }
                    }
                };

                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "REQUESTING UPDATES");
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mGoogleMap.setMyLocationEnabled(true);
                    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                } else {
                    Log.d("TAG", "ActRecording started without GPS permission, finished");
                    finish();
                }

                mPolyline = mGoogleMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(coords)
                        .width(10)
                        .color(Color.BLUE));
            }
        });
    }

    public void finishTrail() {
        // insert coords to db
//        if (isNewTrail) {
//            AppDatabase.getAppDatabase(mContext).trailDao().insertAll(trailData);
//        }
        trailData.setEndTime(System.currentTimeMillis());
        trailData.setCoords(coords);
        AppDatabase.getAppDatabase(mContext).trailDao().insertAll(trailData);

        // end locationupdates, wakelock and notifications
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        mNotificationManager.cancel(NOTIFICATION_ID_RECORDING);
        finish();
    }

    private void TimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }
    private Runnable Timer_Tick = new Runnable() {
        boolean finished = false;
        public void run() {
            if (!finished) {
                long duration = System.currentTimeMillis() - trailData.getStartTime();
                if (duration > mAutoFinish) {
                    finished = true;
                    showAutocompletedNotification();
                    finishTrail();
                } else {
                    updateDurationText(duration);
                    updateSpeedText(duration);
                }
            }
        }
    };

    private void updateDurationText(long duration) {
        if (!inBackground) {
            tv_duration.setText( trailData.makeDurationString(duration) );
        }
    }
    private void updateSpeedText(long duration) {
        if (!inBackground) {
            tv_speed.setText(coords.getSpeedString(prefDistanceUnits, duration));
        }
    }
    private void updateDistanceText() {
        if (!inBackground) {
            tv_distance.setText(coords.getDistanceString(prefDistanceUnits));
        }
    }

    // show notifications
    private void showRecordingNotification() {
        Intent intent = new Intent(this, ActLauncher.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        String message = "Currently recording: " + trailData.getTrailName();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.channel_ID))
                .setSmallIcon(R.drawable.ic_notifications_green_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Recording Trail")
                .setContentText(message)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager.notify(NOTIFICATION_ID_RECORDING, builder.build());
    }
    private void showAutocompletedNotification() {
        Intent intent = new Intent(this, ActSavedTrail.class);
        intent.putExtra("trail-id", trailData.getId());
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        String message = trailData.getTrailName() + " autocompleted.";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.channel_ID))
                .setSmallIcon(R.drawable.ic_notifications_green_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Trail Autocompleted")
                .setContentText(message)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        mNotificationManager.notify(NOTIFICATION_ID_AUTOCOMPLETED, builder.build());
    }
}

