package com.example.chiaraercolani.travelpartner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by martin on 18/03/17.
 */

public class Coordinate {
    double longitude;
    double latitude;

    public Coordinate(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Coordinate(String json_string){
        try {
            JSONObject parser = new JSONObject(json_string);
            this.latitude = parser.getDouble("x");
            this.longitude = parser.getDouble("y");
        } catch (JSONException e) {
            System.err.println(e.toString());
        }
    }
}