package com.trailbook.app.recording;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.trailbook.app.launcher.ActivityLauncher;
import com.trailbook.app.saved_trail.ActivitySavedTrail;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ActivityRecording extends AppCompatActivity {
    private Context mContext;
    private TrailData trailData;
    private RunData runData;
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
    private static final int RESULT_PICK_CONTACT = 10;

    private boolean inBackground = false;
//    private boolean mSendSMSPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        mContext = getApplicationContext();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefDistanceUnits = mSharedPreferences.getString(getString(R.string.pref_key_distance_units), "miles");
        prefGPSInterval = Integer.parseInt(mSharedPreferences.getString(getString(R.string.pref_key_gps_interval), "10000"));
        setTitle("Recording");

        // initialize trailData
        unpackIntent();

        // initialize runData
        runData = new RunData();
        String runId = AppDatabase.newUUID();
        runData.setId(runId);
        runData.setTrailId(trailData.getId());
        runData.setStartTime(System.currentTimeMillis());

        showRecordingNotification();
        startRecording();

        Button end = findViewById(R.id.btn_end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishTrail();
            }
        });

//        Button send = findViewById(R.id.btn_send);
//        send.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getSendSMSPermission();
//                if (mSendSMSPermissionsGranted) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecording.this);
//                    builder.setCancelable(true);
//                    builder.setTitle("SEND SMS");
//                    builder.setMessage("Use contact for this trail or pick from contacts list?");
//                    builder.setPositiveButton("Contact List", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
//                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
//                            startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
//                        }
//                    });
//                    builder.setNegativeButton(trailData.getContact(),
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (coords != null && !coords.isEmpty()) {
//                                        LatLng currLoc = coords.get(coords.size() - 1);
//                                        String message = "My last location was (" + currLoc.latitude + ", " + currLoc.longitude + ")\n - Sent by TrailBook";
//                                        sendSMS(trailData.getContact(), message);
//                                    }
//                                }
//                            });
//
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                    final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                    final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
//                    layoutParams.weight=10;
//                    positiveButton.setLayoutParams(layoutParams);
//                    negativeButton.setLayoutParams(layoutParams);
//                }
//            }
//        });

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
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//                case RESULT_PICK_CONTACT:
//                    String[] contact = contactPicked(data);
//                    if (coords != null && !coords.isEmpty()) {
//                        LatLng currLoc = coords.get(coords.size() - 1);
//                        String message = "My last location was (" + currLoc.latitude + ", " + currLoc.longitude + ")\n - Sent by TrailBook";
//                        sendSMS(Objects.requireNonNull(contactPicked(data))[1], message);
//                    }
//                    break;
//            }
//        } else {
//            Log.e("MainActivity", "Failed to pick contact");
//        }
//    }
//    private String[] contactPicked(Intent data) {
//        Cursor cursor;
//        try {
//            String phoneNo;
//            String name;
//            // getData() method will have the Content Uri of the selected contact
//            Uri uri = data.getData();
//            //Query the content uri
//            assert uri != null;
//            cursor = getContentResolver().query(uri, null, null, null, null);
//            assert cursor != null;
//            cursor.moveToFirst();
//            // column index of the phone number
//            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
//            // column index of the contact name
//            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
//            phoneNo = cursor.getString(phoneIndex);
//            name = cursor.getString(nameIndex);
//            cursor.close();
//            return new String[]{name, phoneNo};
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 1235;
//    private void getSendSMSPermission() {
//        String[] permissions = {Manifest.permission.SEND_SMS};
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.SEND_SMS)
//                == PackageManager.PERMISSION_GRANTED) {
//            mSendSMSPermissionsGranted = true;
//        } else {
//            ActivityCompat.requestPermissions(this, permissions, SEND_SMS_PERMISSION_REQUEST_CODE);
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        mSendSMSPermissionsGranted = false;
//        switch (requestCode) {
//            case SEND_SMS_PERMISSION_REQUEST_CODE: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.checkSelfPermission(this,
//                            Manifest.permission.SEND_SMS)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        mSendSMSPermissionsGranted = true;
//                    }
//                }
//            }
//        }
//    }
//    private void sendSMS(String phoneNumber, String message) {
//        String SENT = "SMS_SENT";
//        String DELIVERED = "SMS_DELIVERED";
//        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
//        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
//        //---when the SMS has been sent---
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(getBaseContext(), "SMS sent!", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                        Toast.makeText(getBaseContext(), "SMS send failed! (Generic failure)", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//                        Toast.makeText(getBaseContext(), "SMS send failed! (No service)", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_NULL_PDU:
//                        Toast.makeText(getBaseContext(), "SMS send failed! (Null PDU)", Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//                        Toast.makeText(getBaseContext(), "SMS send failed! (Radio off)", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        }, new IntentFilter(SENT));
//        //---when the SMS has been delivered---
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        }, new IntentFilter(DELIVERED));
//        SmsManager sms = SmsManager.getDefault();
//        try {
//            sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
//        } catch (IllegalArgumentException e) {
//            Toast.makeText(getBaseContext(), "SMS not delivered! (Invalid Phone Number)", Toast.LENGTH_SHORT).show();
//        }
//    }
    private void TimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }
    private Runnable Timer_Tick = new Runnable() {
        boolean finished = false;
        public void run() {
            if (!finished) {
                long duration = System.currentTimeMillis() - runData.getStartTime();
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
            tv_duration.setText( runData.makeDurationString(duration) );
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

    private void showRecordingNotification() {
        Intent intent = new Intent(this, ActivityLauncher.class);
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
        Intent intent = new Intent(this, ActivitySavedTrail.class);
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
        if (isNewTrail) {
            trailData = new TrailData();
            trailId = AppDatabase.newUUID();
            trailData.setId(trailId);
            trailData.setTrailName(intent.getStringExtra("trail-name"));
            trailData.setContact(intent.getStringExtra("emergency-contact"));
        } else {
            trailId = intent.getStringExtra("trail-id");
            trailData = AppDatabase.getAppDatabase(mContext).trailDao().getById(trailId);
        }
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
                    Log.d("TAG", "ActivityRecording started without GPS permission, finished");
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

    public void saveToDB(RunData runData, TrailData trailData) {
        if (isNewTrail) {
            AppDatabase.getAppDatabase(mContext).trailDao().insertAll(trailData);
        }
        runData.setEndTime(System.currentTimeMillis());
        runData.setCoords(coords);
        AppDatabase.getAppDatabase(mContext).runDao().insertAll(runData);
    }

    public void finishTrail() {
        saveToDB(runData, trailData);
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        mNotificationManager.cancel(NOTIFICATION_ID_RECORDING);
        finish();
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
}

