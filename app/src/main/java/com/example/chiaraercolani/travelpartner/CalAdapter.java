package com.example.chiaraercolani.travelpartner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.util.Lists;
import com.google.api.services.calendar.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chiaraercolani on 18/03/17.
 */

public class CalAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Event> mDataSource;

    public CalAdapter(Context context, List<Event> items, ListView l) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
//1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

//2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

//3
    @Override
    public long getItemId(int position) {
        return position;
    }

//4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item_cal, parent, false);

        // Get title element
        TextView titleTextView = (TextView) rowView.findViewById(R.id.cal_list_item);

        TextView locationTextView = (TextView) rowView.findViewById(R.id.cal_list_location);

        TextView dateStartTextView = (TextView) rowView.findViewById(R.id.cal_list_date_start);

        TextView dateStopTextView = (TextView) rowView.findViewById(R.id.cal_list_date_stop);

        // 1
        Event event = (Event) getItem(position);

// 2
        titleTextView.setText(event.getSummary());

        if (event.getLocation() != null) {
            locationTextView.setText(event.getLocation());
        }
        if ( event.getStart().getDateTime() != null) {
            String dateStart = event.getStart().getDateTime().toString();
            dateStart = dateStart.split("T")[1];
            dateStart = dateStart.split(".000+")[0];
            dateStartTextView.setText(dateStart);
        }
        if (event.getEnd().getDateTime()!= null) {
            String dateStop = event.getEnd().getDateTime().toString();
            dateStop = dateStop.split("T")[1];
            dateStop = dateStop.split(".000+")[0];
            dateStopTextView.setText(dateStop);
        }

        return rowView;
    }

    public void addItems(List<Event> events){
        mDataSource.clear();
        mDataSource.addAll(events);
    }

}
