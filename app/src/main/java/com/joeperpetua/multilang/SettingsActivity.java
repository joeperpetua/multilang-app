package com.joeperpetua.multilang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.concurrent.ExecutionException;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_btn, new SettingsFragment())
                    .commit();
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        // myToolbar.setTitle(R.string.title_activity_settings);
        setSupportActionBar(myToolbar);
    }

    public void returnToMain(View view) throws ExecutionException, InterruptedException {
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(i);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            // MultiSelectListPreference languageList = findPreference("languages_list");
        }

    }
}