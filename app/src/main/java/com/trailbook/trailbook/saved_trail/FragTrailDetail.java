package com.trailbook.trailbook.saved_trail;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trailbook.trailbook.R;
import com.trailbook.trailbook.database.AppDatabase;
import com.trailbook.trailbook.database.RunData;

public class FragTrailDetail extends Fragment {
    private static final String ARG_RUN_ID = "run-id";
    private static String mRunId;

    private String mDistanceUnits;

    TextView tv_date;
    TextView tv_duration;
    TextView tv_distance;
    TextView tv_speed;

    public static FragTrailDetail newInstance(String runId) {
        FragTrailDetail fragment = new FragTrailDetail();
        Bundle args = new Bundle();
        args.putString(ARG_RUN_ID, runId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRunId = getArguments().getString(ARG_RUN_ID);
        }
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mDistanceUnits = mSharedPreferences.getString(getString(R.string.pref_key_distance_units), "miles");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_saved_trail_detail, container, false);
        tv_date = (TextView) view.findViewById(R.id.tv_detail_date);
        tv_duration = (TextView) view.findViewById(R.id.tv_detail_duration);
        tv_distance = (TextView) view.findViewById(R.id.tv_detail_distance);
        tv_speed = (TextView) view.findViewById(R.id.tv_detail_speed);

        RunData runData = AppDatabase.getAppDatabase(getContext()).runDao().getById(mRunId);
        setTextViews(runData);
        return view;
    }

    public void setTextViews(RunData inp) {
        if (inp == null) {
            Log.d("MyTag", "FragTrailDetail received invalid RunData");
            return;
        }
        tv_date.setText(inp.getStartTimeDate());
        tv_duration.setText(inp.getDurationString());
        tv_distance.setText(inp.getCoords().getDistanceString(mDistanceUnits));
        tv_speed.setText( inp.getCoords().getSpeedString( mDistanceUnits, inp.getDuration() ) );
    }
}
