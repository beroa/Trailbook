package com.trailbook.app.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.trailbook.app.recording.ActivityRecording;
import com.trailbook.app.ActivitySettings;
import com.trailbook.app.R;

/**
 * Created by Administrator on 4/8/2018.
 */

public class FragLauncher extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        TrailView trails = new TrailView();
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.add(R.id.trails_frame, trails).commit();
        return inflater.inflate(R.layout.activity_launcher, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Button settings = (Button) view.findViewById(R.id.btn_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivitySettings.class);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.btn_new_trail);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityRecording.class);
                startActivity(intent);
            }
        });

    }
}
