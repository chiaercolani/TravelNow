package com.example.chiaraercolani.travelpartner;

import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

/**
 * Created by martin on 18/03/17.
 */

public class Section {
    Journey journey;
    int walk;
    Checkpoint departure;
    Checkpoint arrival;

    public Section(Journey journey, int walk, Checkpoint departure, Checkpoint arrival) {
        this.journey = journey;
        this.walk = walk;
        this.departure = departure;
        this.arrival = arrival;
    }

    public Section(String json_string) {
        try {
            JSONObject parser = new JSONObject(json_string);
            this.departure = new Checkpoint(parser.getString("departure"));
            this.arrival = new Checkpoint(parser.getString("arrival"));
            this.walk = parser.getInt("walk");
        } catch (JSONException e) {
            System.err.println(e.toString());
        }
    }
}
