package com.fotolibb.monthlyexpenses;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.fotolibb.monthlyexpenses.SettingsActivity.PREFS_KEY_CALENDAR_ID;
import static com.fotolibb.monthlyexpenses.SettingsActivity.PREFS_KEY_CALENDAR_NAME;

public class MainActivity extends AppCompatActivity {
    private int paymentDay = 10;

    private String TAG = "CALLOG";
    private List<Record> resultData = null;
    private boolean initialized = false;

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final TextView et = findViewById(R.id.editText);
        final SeekBar sk = findViewById(R.id.seekBar);
        et.setText("10");
        et.setLeft(40 + (sk.getWidth()) * 9 / 10);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                int progress = seekBar.getProgress();
                paymentDay = progress;
                if (null != resultData) {
                    ProcessData(resultData);
                    et.setLeft(40 + (sk.getWidth()) * progress / 10);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                et.setLeft(40 + (sk.getWidth()) * progress / 10);
                et.setText(Integer.toString(progress + 1));
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 5554);
        } else {
            initialized = true;
            showData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (initialized)
            showData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5554: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialized = true;
                    showData();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
        }
    }


    private void showData() {
        //String url = getResources().getString(R.string.url_data);
        //new RecordDownloaderAsync(url, this).execute();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String calendarName = mPrefs.getString(PREFS_KEY_CALENDAR_NAME, "");
        long calendarId = mPrefs.getLong(PREFS_KEY_CALENDAR_ID, -1);
        if (calendarId == -1) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        }

        List<Record> data = getOccurencesInMonth(calendarId);
        ProcessData(data);
    }

    private ArrayList<Record> getOccurencesInMonth(long calId) {
        ArrayList<Record> records = new ArrayList();

        int month = Calendar.getInstance().get(Calendar.MONTH);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        Calendar cFrom = Calendar.getInstance();
        cFrom.set(Calendar.MONTH, month);
        cFrom.set(Calendar.YEAR, year);
        cFrom.set(Calendar.DAY_OF_MONTH, 1);
        cFrom.set(Calendar.HOUR_OF_DAY, 0);
        cFrom.set(Calendar.MINUTE, 0);
        cFrom.set(Calendar.SECOND, 0);
        cFrom.set(Calendar.MILLISECOND, 0);

        Calendar cTo = Calendar.getInstance();
        cTo.set(Calendar.MONTH, month);
        cTo.set(Calendar.YEAR, year);
        cTo.set(Calendar.DAY_OF_MONTH, cFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
        cTo.set(Calendar.HOUR_OF_DAY, 23);
        cTo.set(Calendar.MINUTE, 59);
        cTo.set(Calendar.SECOND, 59);
        cTo.set(Calendar.MILLISECOND, 999);

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);

        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = getContentResolver().query(
                eventsUri,
                new String[]{CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                        CalendarContract.Instances.EVENT_ID},
                CalendarContract.Instances.BEGIN + " >= " + cFrom.getTimeInMillis() + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + cTo.getTimeInMillis() + " and " + CalendarContract.Instances.CALENDAR_ID + " = " + calId,
                null,
                CalendarContract.Instances.BEGIN + " ASC");

        Log.i(TAG, "Count total: " + cursor.getCount());
        if (cursor.moveToFirst()) {
            do {
                String title = cursor.getString(1);
                String desc = cursor.getString(2);
                long begin = cursor.getLong(3);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(begin);
                int amount = Integer.parseInt(desc);
                Record r = new Record(c.get(Calendar.DAY_OF_MONTH), amount, title);
                records.add(r);
                Log.i(TAG, String.format("Added record  [%d] %s - %d Kc", r.day, r.popis, r.amount));

            } while (cursor.moveToNext());
        }
        return records;
    }

    private int getOccurences() {
        Calendar cNow = Calendar.getInstance();

        Calendar cFrom = Calendar.getInstance();
        if (cNow.get(Calendar.DAY_OF_MONTH) < paymentDay) {
            cFrom.add(Calendar.MONTH, -1);
        }
        cFrom.set(Calendar.DAY_OF_MONTH, 1);
        cFrom.set(Calendar.HOUR_OF_DAY, 0);
        cFrom.set(Calendar.MINUTE, 0);
        cFrom.set(Calendar.SECOND, 0);

        Calendar cTo = Calendar.getInstance();
        if (cNow.get(Calendar.DAY_OF_MONTH) < paymentDay) {
            cTo.add(Calendar.MONTH, -1);
        }
        cTo.set(Calendar.DAY_OF_MONTH, cFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
        cTo.set(Calendar.HOUR_OF_DAY, 23);
        cTo.set(Calendar.MINUTE, 59);
        cTo.set(Calendar.SECOND, 59);

        Log.i(TAG, "FROM: " + cFrom.getTime().toString());
        Log.i(TAG, "TO: " + cTo.getTime().toString());

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(eventsUriBuilder, Long.MIN_VALUE);
        ContentUris.appendId(eventsUriBuilder, Long.MAX_VALUE);

        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = getContentResolver().query(
                eventsUri,
                new String[]{CalendarContract.Instances.CALENDAR_ID, CalendarContract.Instances.TITLE,
                        CalendarContract.Instances.DESCRIPTION, CalendarContract.Instances.BEGIN,
                        CalendarContract.Instances.END, CalendarContract.Instances.EVENT_LOCATION,
                        CalendarContract.Instances.EVENT_ID},
                CalendarContract.Instances.BEGIN + " >= " + cFrom.getTimeInMillis() + " and " + CalendarContract.Instances.BEGIN
                        + " <= " + cTo.getTimeInMillis() + " and " + CalendarContract.Instances.VISIBLE + " = 1" + " and " + CalendarContract.Instances.TITLE + " like '%Posilovna%'",
                null,
                CalendarContract.Instances.BEGIN + " ASC");

        int cnt = 0;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        long calendarId = mPrefs.getLong(PREFS_KEY_CALENDAR_ID, -1);
        String calendarName = mPrefs.getString(PREFS_KEY_CALENDAR_NAME, "");

        Log.i(TAG, "Count total: " + cursor.getCount());
        Log.i(TAG, String.format("Excluded calendar [%d]: %s", calendarId, calendarName));
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                if (calendarId != id) {
                    cnt += 1;
                }
                String title = cursor.getString(1);
                String desc = cursor.getString(2);
                long begin = cursor.getLong(3);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(begin);

                int d = c.get(Calendar.DAY_OF_MONTH);
                int mm = c.get(Calendar.MONTH) + 1;

                Log.i(TAG, String.format("[%d]: [%d/%d] %s - %s", id, mm, d, title, desc, c.toString()));
            } while (cursor.moveToNext());
        }
        return cnt;
    }

    public void ProcessData(List<Record> result) {
        resultData = result;
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_MONTH);

        int suma = 0;
        int sumaTotal = 0;

        TableLayout tableLayout = findViewById(R.id.mainTable);
        tableLayout.removeAllViews();

        Boolean doBg = true;
        for (Record zaznam : result) {
            Boolean active = false;

            if (((zaznam.day > today) && (today > paymentDay))
                    || ((today <= paymentDay) && (zaznam.day <= paymentDay) && (zaznam.day > today))
                    || ((today > paymentDay) && (zaznam.day <= paymentDay))
                    ) {
                if (zaznam.popis.equals("posilovna")) {
                    int repeats = getOccurences();
                    zaznam.amount = 350 * repeats;

                    String[] mesice = getResources().getStringArray(R.array.mesice);
                    Calendar cc = Calendar.getInstance();

                    if (cc.get(Calendar.DAY_OF_MONTH) < paymentDay) {
                        cc.add(Calendar.MONTH, -1);
                    }

                    zaznam.popis += String.format(" (%s: %dx)", mesice[cc.get(Calendar.MONTH)], repeats);
                }
                suma += zaznam.amount;
                active = true;
            }
            sumaTotal += zaznam.amount;

            TableRow tr = new TableRow(this);
            TableRow.LayoutParams tp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(tp);

            TextView tvDay = new TextView(this);
            tvDay.setText(Integer.toString(zaznam.day));
            tvDay.setPadding(0, 0, 40, 0);
            tvDay.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

            TextView tvAmount = new TextView(this);
            tvAmount.setText(Integer.toString(zaznam.amount));
            tvAmount.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            tvAmount.setPadding(0, 0, 40, 0);


            TextView tvDesc = new TextView(this);
            tvDesc.setText(zaznam.popis);

            if (active) {
                tvDay.setTextColor(Color.RED);
                tvAmount.setTextColor(Color.BLACK);
                tvDesc.setTextColor(Color.BLACK);
            } else {
                tvDay.setTextColor(Color.GRAY);
                tvAmount.setTextColor(Color.GRAY);
                tvDesc.setTextColor(Color.GRAY);

                tvDay.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                tvDesc.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                tvAmount.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            }

            if (doBg) {
                tvDay.setBackgroundColor(Color.argb(20, 80, 80, 80));
                tvAmount.setBackgroundColor(Color.argb(20, 80, 80, 80));
                tvDesc.setBackgroundColor(Color.argb(20, 80, 80, 80));
            }

            tvDay.setTextSize(18);
            tvAmount.setTextSize(18);
            tvDesc.setTextSize(18);

            doBg = !doBg;

            tr.addView(tvDay);
            tr.addView(tvAmount);
            tr.addView(tvDesc);
            tableLayout.addView(tr);
        }

        TableRow tr = new TableRow(this);
        TextView t = new TextView(this);

        TableRow.LayoutParams params = (TableRow.LayoutParams) tr.getLayoutParams();
        if (params == null)
            params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.span = 3;
        t.setLayoutParams(params);

        t.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        t.setText(String.format("Zbývá %d z %d", suma, sumaTotal));
        t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        t.setTextSize(32);
        tr.addView(t);

        tableLayout.addView(tr);
    }
}
