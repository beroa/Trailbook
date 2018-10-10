package com.trailbook.app.recording;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.trailbook.app.R;

public class ActivityStartTrail extends AppCompatActivity {
    private boolean mLocationPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trail);
        Context mContext = getApplicationContext();
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        getLocationPermission();

        final EditText et_trailname = (EditText) findViewById(R.id.et_start_trail_name);
        final EditText et_contact = (EditText) findViewById(R.id.et_start_trail_contact);
        final EditText et_autofinish = (EditText) findViewById(R.id.et_start_trail_autofinish);
        et_contact.setText(mSharedPreferences.getString("default_contact", ""));

        Button start = (Button) findViewById(R.id.btn_start_trail);
        boolean validInp = false;
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int autofinish = Integer.parseInt(et_autofinish.getText().toString());
                if (et_trailname.getText().toString().trim().length() == 0) {
                    et_trailname.setError("Trail name cannot be empty.");
                } else if (autofinish < 0 || autofinish > 24) {
                    et_autofinish.setError("Trails must automatically finish after between 1-24 hours.");
                } else {
                    if (mLocationPermissionsGranted) {
                        Intent intent = new Intent(getApplicationContext(), ActivityRecording.class);
                        intent.putExtra("new-trail", true);
                        intent.putExtra("trail-name", et_trailname.getText().toString());
                        intent.putExtra("emergency-contact", et_contact.getText().toString());
                        intent.putExtra("auto-finish", autofinish);
                        startActivity(intent);
                    }
                }
            }
        });
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
