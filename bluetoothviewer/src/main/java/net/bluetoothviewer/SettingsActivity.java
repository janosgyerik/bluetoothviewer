package net.bluetoothviewer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.bluetoothviewer.full.R;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setContentView(R.layout.settings_activity);
    }
}
