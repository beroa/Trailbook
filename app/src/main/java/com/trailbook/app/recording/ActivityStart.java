package com.trailbook.app.recording;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.trailbook.app.R;
import com.trailbook.app.database.AppDatabase;
import com.trailbook.app.database.TrailData;

import org.w3c.dom.Text;

// starts a run of an existing trail, gets trail-id from the intent
public class ActivityStart extends AppCompatActivity {
    private boolean mLocationPermissionsGranted = false;
    String trailId;
    TrailData trailData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getLocationPermission();

        Intent intent = getIntent();
        trailId = intent.getStringExtra("trail-id");

        Log.d("AYY", "trail-id" + trailId);

        //        final EditText et_contact = (EditText) findViewById(R.id.et_start_run_contact);
        final TextView tv_start = (TextView) findViewById(R.id.tv_start);
        final Button start = (Button) findViewById(R.id.btn_start);
        final EditText et_trailname = (EditText) findViewById(R.id.et_start_trailname);
        final TextView tv_trailname = findViewById(R.id.tv_start_trailname);
        final EditText et_autofinish = (EditText) findViewById(R.id.et_start_autofinish);

        if (trailId == null) {
            // prepare for new trail
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int autofinish = Integer.parseInt(et_autofinish.getText().toString());
                    String trailName = et_trailname.getText().toString().trim();
                    if (trailName.length() == 0) {
                        et_trailname.setError("Trail name cannot be empty.");
                    } else if (autofinish < 0 || autofinish > 24) {
                        et_autofinish.setError("Trails must automatically finish after between 1-24 hours.");
                    } else {
                        StartTrail(trailName, autofinish);
                    }
                }
            });
        } else {
            // prepare for new run
            trailData = AppDatabase.getAppDatabase(getApplicationContext()).trailDao().getById(trailId);

            String s = getString(R.string.new_run_of);
            tv_start.setText(String.format("%s%s\"", s, trailData.getTrailName()));

            et_trailname.setVisibility(View.GONE);
            tv_trailname.setVisibility(View.GONE);
            //        et_contact.setText(trailData.getContact());
            //        et_contact.setEnabled(false);

            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int autofinish = Integer.parseInt(et_autofinish.getText().toString());
                    if (autofinish < 0 || autofinish > 24) {
                        et_autofinish.setError("Trails must automatically finish after between 1-24 hours.");
                    }
                    StartRun(trailId, autofinish);
                }
            });
        }

    }

    private void StartRun(String trailId, int autofinish) {
        if (mLocationPermissionsGranted) {
            Intent intent = new Intent(getApplicationContext(), ActivityRecording.class);
            intent.putExtra("new-trail", false);
            intent.putExtra("trail-id", trailId);
            intent.putExtra("auto-finish", autofinish);
            startActivity(intent);
        }
    }

    private void StartTrail(String trailName, int autofinish) {
        if (mLocationPermissionsGranted) {
            Intent intent = new Intent(getApplicationContext(), ActivityRecording.class);
            intent.putExtra("new-trail", true);
            intent.putExtra("trail-name", trailName);
            intent.putExtra("auto-finish", autofinish);
            //intent.putExtra("emergency-contact", et_contact.getText().toString());
            startActivity(intent);
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionsGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionsGranted = true;
                    }
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


}
