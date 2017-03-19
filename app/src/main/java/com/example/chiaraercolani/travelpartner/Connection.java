package com.example.chiaraercolani.travelpartner;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by martin on 18/03/17.
 */

public class Connection {
    Checkpoint from;
    Checkpoint to;
    int duration_day;
    int duration_hour;
    int duration_min;
    Service service;
    List<String> product;
    int capa_first_class;
    int capa_second_class;
    List<Section> sections;

    static public List<Connection> connectionList(String json_array){
        List toReturn = new LinkedList();

        try {
            JSONObject parser = new JSONObject(json_array);
            JSONArray locationArray = parser.getJSONArray("connections");

            for(int i = 0; i < locationArray.length(); i++) {
                Connection new_con = new Connection(locationArray.getJSONObject(i).toString());
                toReturn.add(new_con);
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + json_array);
        }

        return toReturn;
    }

    public Connection(String json_string) {
        try {
            JSONObject parser = new JSONObject(json_string);
            this.from = new Checkpoint(parser.getString("from").toString());
            this.to = new Checkpoint(parser.getString("to").toString());
            String nb_day = parser.getString("duration").substring(0, 2);
            String nb_hours = parser.getString("duration").substring(3, 5);
            String nb_min = parser.getString("duration").substring(6, 8);
            this.duration_day = Integer.parseInt(nb_day);
            this.duration_hour = Integer.parseInt(nb_hours);
            this.duration_min = Integer.parseInt(nb_min);
            this.service = new Service(parser.getString("service").toString());

            JSONArray product_array = parser.getJSONArray("products");

            product = new LinkedList<>();
            for(int i = 0; i < product_array.length(); i++){
                product.add(product_array.getString(i));
            }

            sections = new LinkedList<>();
            JSONArray section_array = parser.getJSONArray("sections");
            for(int i = 0; i < section_array.length(); i++){
                sections.add(new Section(section_array.getString(i)));
            }

            this.capa_first_class = parser.getInt("capacity1st");
            this.capa_second_class = parser.getInt("capacity2nd");

        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + json_string);
            System.err.println(e.toString());
        }
    }

    public int getScore() {
        return this.duration_day*10000 + this.duration_hour*100 + this.duration_min;
    }

    public List<Location> getWaitingLocations(int waitingMS){
        List<Location> toReturn = new LinkedList<>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZZZZ");

        for(int i = 0 ; i < this.sections.size(); i++)
        {
            Location st_d = this.sections.get(i).departure.station;
            Location st_a = this.sections.get(i).arrival.station;
            if(st_d.equals(st_a)) {
                try {
                    String arr_s = this.sections.get(i).arrival.arrival;
                    String dep_s = this.sections.get(i).departure.departure;
                    Calendar arr = Calendar.getInstance();
                    Date t = df.parse(arr_s);
                    arr.setTime(t);
                    Calendar dep = Calendar.getInstance();
                    t = df.parse(dep_s);
                    dep.setTime(t);

                    Calendar dep2 = (Calendar) arr.clone();
                    dep2.setTime(t);
                    dep2.setTimeInMillis(dep2.getTimeInMillis() + waitingMS);

                    if(dep2.before(arr)) {
                        // Find activity nearby
                        String m = "There is time in "+this.sections.get(i).departure.station.toString();
                        m+=": arrival at "+arr_s + " and departure at " + dep_s;
                        Log.v("MY APP", m);
                        toReturn.add(this.sections.get(i).departure.station);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return toReturn;
    }
}

