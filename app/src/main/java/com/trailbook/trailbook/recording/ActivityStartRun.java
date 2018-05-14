package com.trailbook.trailbook.recording;

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

import com.trailbook.trailbook.R;
import com.trailbook.trailbook.database.AppDatabase;
import com.trailbook.trailbook.database.TrailData;


public class ActivityStartRun extends AppCompatActivity {
    String trailId;
    TrailData trailData;
    private boolean mLocationPermissionsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);

        getLocationPermission();

        Intent intent = getIntent();
        trailId = intent.getStringExtra("trail-id");

        if (trailId == null) {
            Log.d("MyTag", "ActivityStartRun received bad trailId");
            finish();
        } else {
            trailData = AppDatabase.getAppDatabase(getApplicationContext()).trailDao().getById(trailId);
        }

        final TextView tv_newrun = (TextView) findViewById(R.id.tv_new_run);
        final EditText et_contact = (EditText) findViewById(R.id.et_start_run_contact);
        final EditText et_autofinish = (EditText) findViewById(R.id.et_start_run_autofinish);

        String s = getString(R.string.new_run_of);
        tv_newrun.setText(String.format("%s%s\"", s, trailData.getTrailName()));

        et_contact.setText(trailData.getContact());
        et_contact.setEnabled(false);

        Button start = (Button) findViewById(R.id.btn_start_run);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int autofinish = Integer.parseInt(et_autofinish.getText().toString());
                if (autofinish < 0 || autofinish > 24) {
                    et_autofinish.setError("Trails must automatically finish after between 1-24 hours.");
                } else {
                    if (mLocationPermissionsGranted) {
                        Intent intent = new Intent(v.getContext(), ActivityRecording.class);
                        intent.putExtra("new-trail", false);
                        intent.putExtra("trail-id", trailId);
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
