package com.trailbook.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

public class FragSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
    }

    private void setSummary(Preference pref, String key) {
        if (pref instanceof EditTextPreference) {
            pref.setSummary(sharedPreferences.getString(key, "No contact set"));
        }
    }
    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            setSummary(p, p.getKey());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = getPreferenceScreen().findPreference(key);
        setSummary(pref, key);
    }

    @Override
    public void onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


}