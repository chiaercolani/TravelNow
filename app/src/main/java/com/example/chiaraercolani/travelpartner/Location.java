package com.example.chiaraercolani.travelpartner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by chiaraercolani on 18/03/17.
 */

public class Location {
    private int id;
    private String name;
    private int score;
    private Coordinate coordinate;
    private int distance;

    static public List<Location> locationList(String json_array){
        List<Location> toReturn = new LinkedList<>();

        try {
            JSONObject parser = new JSONObject(json_array);
            JSONArray locationArray = parser.getJSONArray("stations");

            for(int i = 0; i < locationArray.length(); i++) {
                Location new_loc = new Location(locationArray.getJSONObject(i).toString());
                toReturn.add(new_loc);
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + json_array);
        }

        return toReturn;
    }

    public Location(String json_string){
        try {
            JSONObject parser = new JSONObject(json_string);
            this.id = parser.getInt("id");
            this.name = parser.getString("name");
            this.coordinate = new Coordinate(parser.getString("coordinate")); // TODO
            this.distance = parser.getInt("distance");
            this.score = parser.getInt("score");

        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + json_string);
            System.err.println(e.toString());
        }
    }

}

