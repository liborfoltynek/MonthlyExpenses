package com.fotolibb.monthlyexpenses;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Libb on 03.11.2017.
 */

public class RecordDownloaderAsync extends AsyncTask<Object, Object, ArrayList<Record>> {

    private String sURL;
    private ArrayList<Record> records;
    private MainActivity callingActivity;


    public RecordDownloaderAsync(String sURL, MainActivity activity) {
        this.sURL = sURL;
        this.callingActivity = activity;
    }

    protected ArrayList<Record> doInBackground(Object... arg0) {
        String sJSON = null;
        InputStream in = null;

        try {
            Log.i("ME", sURL);
            URL url = new URL(sURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new
                    InputStreamReader(in, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String radek = null;

            while ((radek = reader.readLine()) != null) {
                sb.append(radek + "\n");
            }
            sJSON = sb.toString();
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
            } catch (Exception e) {
                Log.e(TAG, "Exception: ");
                String s = e.getMessage();
                Log.e(TAG, "Exception: " + s);
            }
        }
        JSONArray jsonObject;
        try {
            records = new ArrayList<Record>();
            jsonObject = new JSONArray(sJSON);
            //JSONArray eventsJSONObject = jsonObject.getJSONArray();
            for (int i = 0; i < jsonObject.length(); i++) {
                {
                    JSONObject jsonEventData = jsonObject.getJSONObject(i);
                    Record r = new Record(jsonEventData);
                    Log.i("ME", String.format("%d - %d (%s)", r.day, r.amount, r.popis));
                    records.add(r);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    protected void onPostExecute(ArrayList<Record> result) {
        callingActivity.ProcessData(result);
    }

}