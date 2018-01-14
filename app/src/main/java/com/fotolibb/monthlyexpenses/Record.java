package com.fotolibb.monthlyexpenses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Libb on 03.11.2017.
 */

public class Record {
    public String popis;
    public int day;
    public int amount;

    public Record(JSONObject jsonEventData) throws JSONException {
        day = jsonEventData.getInt("den");
        popis = jsonEventData.getString("popis");
        amount = jsonEventData.getInt("castka");
    }

    public Record(int day, int amount, String popis) {
        this.day = day;
        this.amount = amount;
        this.popis = popis;
    }
}
