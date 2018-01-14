package com.fotolibb.monthlyexpenses;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_KEY_CALENDAR_ID = "PREFS_KEY_CALENDAR_ID";
    public static final String PREFS_KEY_CALENDAR_NAME = "PREFS_KEY_CALENDAR_NAME";
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 1
            CalendarContract.Calendars.CALENDAR_COLOR                 // 2

    };
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 1;
    private static final int PROJECTION_CALENDAR_COLOR = 2;
    private static final String TAG = "CALLOG";
    ArrayList<CalendarInfo> calendars = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        calendars = new ArrayList<>();
        ((Button) findViewById(R.id.btSettingsOK)).setOnClickListener(new View.OnClickListener() {
                                                                          @Override
                                                                          public void onClick(View view) {
                                                                              onBtPressed(view);
                                                                          }
                                                                      }
        );
        loadData();
    }

    private void loadData() {
        Cursor cur;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String calendarName = mPrefs.getString(PREFS_KEY_CALENDAR_NAME, "");
        long calendarId = mPrefs.getLong(PREFS_KEY_CALENDAR_ID, -1);
        int idToselect = R.id.rbNoEvent;

        try {
            @SuppressLint("MissingPermission") Cursor cursor = cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

            RadioGroup rg = (RadioGroup) findViewById(R.id.settingsCalendarsGroup);
            while (cur.moveToNext()) {
                CalendarInfo ci = new CalendarInfo();

                ci.Name = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
                ci.Id = cur.getLong(PROJECTION_ID_INDEX);
                ci.Color = cur.getInt(PROJECTION_CALENDAR_COLOR);

                RadioButton rb = new RadioButton(getApplicationContext());
                rb.setTextColor(ci.Color);
                rg.addView(rb);

                rb.setText(ci.Name);
                if (ci.Id == calendarId) {
                    idToselect = rb.getId();
                }
                Log.i(TAG, String.format("Calendar id [%d] : %s", ci.Id, ci.Name));
                calendars.add(ci);
            }

            rg.check(idToselect);
        } catch (Exception ex) {
            Log.e("EX", ex.getMessage());
        }

    }

    private void onBtPressed(View view) {
        RadioGroup radioButtonGroup = (RadioGroup) findViewById(R.id.settingsCalendarsGroup);
        int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        if (radioButtonID != -1) {
            if (radioButtonID != R.id.rbNoEvent) {
                View radioButton = radioButtonGroup.findViewById(radioButtonID);
                int idx = radioButtonGroup.indexOfChild(radioButton);
                RadioButton r = (RadioButton) radioButtonGroup.getChildAt(idx);
                String selectedtext = r.getText().toString();
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.putString(PREFS_KEY_CALENDAR_NAME, selectedtext);
                long id = getCalendarId(selectedtext);
                prefsEditor.putLong(PREFS_KEY_CALENDAR_ID, id);
                prefsEditor.apply();
            } else {
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                prefsEditor.remove(PREFS_KEY_CALENDAR_NAME);
                prefsEditor.remove(PREFS_KEY_CALENDAR_ID);
                prefsEditor.apply();
            }
        }
        finish();
    }

    private long getCalendarId(String name) {
        for (CalendarInfo ci : calendars) {
            if (ci.Name.equals(name)) {
                return ci.Id;
            }
        }
        return -1;
    }

    private class CalendarInfo {
        public Long Id;
        public String Name;
        public Integer Color;
    }
}
