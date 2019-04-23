package com.trailbook.app.saved_trail;

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

import com.trailbook.app.R;
import com.trailbook.app.database.AppDatabase;
import com.trailbook.app.database.RunData;
import com.trailbook.app.database.TrailData;

public class FragTrailDetail extends Fragment {
    private static final String ARG_TRAIL_ID = "trail-id";
    private static String mTrailId;

    private String mDistanceUnits;

    TextView tv_date;
    TextView tv_duration;
    TextView tv_distance;
    TextView tv_speed;

    public static FragTrailDetail newInstance(String trailId) {
        FragTrailDetail fragment = new FragTrailDetail();
        Bundle args = new Bundle();
        args.putString(ARG_TRAIL_ID, trailId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTrailId = getArguments().getString(ARG_TRAIL_ID);
        }
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mDistanceUnits = mSharedPreferences.getString(getString(R.string.pref_key_distance_units), "miles");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_saved_trail_detail, container, false);
        tv_date = view.findViewById(R.id.tv_detail_date);
        tv_duration = view.findViewById(R.id.tv_detail_duration);
        tv_distance = view.findViewById(R.id.tv_detail_distance);
        tv_speed = view.findViewById(R.id.tv_detail_speed);

        TrailData trailData = AppDatabase.getAppDatabase(getContext()).trailDao().getById(mTrailId);
        setTextViews(trailData);
        return view;
    }

    public void setTextViews(TrailData inp) {
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
