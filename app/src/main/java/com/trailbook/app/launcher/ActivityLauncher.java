package com.trailbook.app.launcher;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.trailbook.app.ActivitySettings;
import com.trailbook.app.R;
import com.trailbook.app.database.AppDatabase;
import com.trailbook.app.database.TrailData;
import com.trailbook.app.recording.ActivityStart;
import com.trailbook.app.saved_trail.ActivitySavedTrail;

public class ActivityLauncher extends AppCompatActivity implements TrailView.OnListFragmentInteractionListener {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        mContext = getApplicationContext();

        createNotificationChannel();

        int trailCount = AppDatabase.getAppDatabase(mContext).trailDao().countAll();
        if (trailCount == 0) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.trails_frame, new FragLauncherEmpty()).commit();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.trails_frame, new TrailView()).commit();
        }

        Button settings = (Button) findViewById(R.id.btn_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitySettings.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btn_new_trail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ActivityStart.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onListFragmentInteraction(TrailData item) {
        Intent intent = new Intent(mContext, ActivitySavedTrail.class);
        intent.putExtra("trail-id", item.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshTrailView();
    }

    public void RefreshTrailView() {
        int trailCount = AppDatabase.getAppDatabase(mContext).trailDao().countAll();
        if (trailCount == 0) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.trails_frame, new FragLauncherEmpty()).commit();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.trails_frame, new TrailView()).commit();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = getString(R.string.channel_ID);
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
