package com.trailbook.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ActivitySettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,new FragSettings()).commit();
    }
}