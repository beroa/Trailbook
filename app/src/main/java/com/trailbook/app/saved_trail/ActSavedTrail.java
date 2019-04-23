package com.trailbook.app.saved_trail;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.trailbook.app.recording.ActStart;
import com.trailbook.app.recording.Coords;
import com.trailbook.app.R;
import com.trailbook.app.database.AppDatabase;
import com.trailbook.app.database.RunData;
import com.trailbook.app.database.TrailData;

import java.util.List;


public class ActSavedTrail extends AppCompatActivity implements OnMapReadyCallback {
    private String mTrailId;
    private TrailData trailData;
    private int runListIndex = 0;
    private FragTrailDetail fragTrailDetail;

    private static GoogleMap map;
    private static Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_saved_trail);

        Intent intent = getIntent();
        mTrailId = intent.getStringExtra("trail-id");
        trailData = AppDatabase.getAppDatabase(getApplicationContext()).trailDao().getById(mTrailId);

        // get google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapSaved);
        mapFragment.getMapAsync(this);

//         add trail detail fragment
        fragTrailDetail = FragTrailDetail.newInstance( mTrailId );
        getSupportFragmentManager().beginTransaction()
                .add(R.id.details_frame, fragTrailDetail)
                .commit();

        setTitle(trailData.getTrailName());
    }

//    @Override
//    public void onListFragmentInteraction(RunData item) {
//        // store selection
//        runListIndex = runList.indexOf(item);
//        // update details fragment
//        fragTrailDetail.setTextViews(item);
//        // update polyline
//        setPolylineFromRun(item);
//    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setPolylineFromRun( trailData );
    }
    public void setPolylineFromRun(TrailData trailData) {
        Coords coords = trailData.getCoords();

        if (polyline != null) {
            polyline.remove();
        }
        if (!coords.isEmpty()) {
            polyline = map.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll( coords )
                    .width(10)
                    .color(Color.BLUE));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom( coords.get(0) , 14));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_trail, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_rename:
                showRenameMenu();
                return true;
            case R.id.menu_delete:
                deleteTrail(mTrailId);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteTrail(String trailId) {
//        int runCount = AppDatabase.getAppDatabase(getApplicationContext()).runDao().countRunsForTrailId(trailId);
//        List<RunData> oldRuns = AppDatabase.getAppDatabase(getApplicationContext()).runDao().getRunsByTrailId(trailId);
//        for (int i = 0; i < runCount; i++) {
//            AppDatabase.getAppDatabase(getApplicationContext()).runDao().delete(oldRuns.get(i));
//        }
        AppDatabase.getAppDatabase(getApplicationContext()).trailDao().delete(trailData);
    }

    public void showRenameMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_rename,null);
        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final EditText et = dialogView.findViewById(R.id.et_dialog);
                        String newName = et.getText().toString();
                        AppDatabase.getAppDatabase(getApplicationContext()).trailDao().renameById(mTrailId, newName);
                        trailData = AppDatabase.getAppDatabase(getApplicationContext()).trailDao().getById(mTrailId);
                        setTitle(trailData.getTrailName());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        final Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        layoutParams.weight=10;
        positiveButton.setLayoutParams(layoutParams);
        negativeButton.setLayoutParams(layoutParams);
    }

}
