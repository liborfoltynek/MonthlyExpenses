package com.fotolibb.monthlyexpenses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Libb on 03.11.2017.
 */

public class RecordAdapter  extends BaseAdapter {

        Context context;
        List<Record> rowItems;


        public RecordAdapter(Context context, List<Record> items) {
            this.context = context;
            this.rowItems = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder hld = null;
            LayoutInflater lInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = lInflater.inflate(R.layout.list_item, null);
                hld = new ViewHolder();
                hld.txtDay = (TextView) convertView.findViewById(R.id.listItemDay);
                hld.txtDesc= (TextView) convertView.findViewById(R.id.listItemDesc);
                hld.txtAmount = (TextView) convertView.findViewById(R.id.listItemAmount);
                convertView.setTag(hld);
            } else {
                hld = (ViewHolder) convertView.getTag();
            }
            Record polozka = (Record) getItem(position);
            hld.txtDay.setText(Integer.toString(polozka.day));
            hld.txtDesc.setText(polozka.popis);
            hld.txtAmount.setText(Integer.toString(polozka.amount));

            return convertView;
        }

        @Override
        public int getCount() {
            return rowItems.size();
        }

        @Override
        public Object getItem(int position) {
            return rowItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return rowItems.indexOf(getItem(position));
        }

        public class ViewHolder {

            TextView txtDay;
            TextView txtDesc;
            TextView txtAmount;
        }
    }

