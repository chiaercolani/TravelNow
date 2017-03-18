package com.example.chiaraercolani.travelpartner;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by chiaraercolani on 18/03/17.
 */

public class HTTPRequest extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... params) {
        doPathRequest(params[0], params[1], params[2], params[3]);

        return new Boolean(true);
    }

    private void request(String urlString){
        try {
            URL url = new URL(urlString);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                StringBuilder sb = new StringBuilder();

                int next_char = is.read();
                while(next_char != -1){
                    sb.append((char) next_char);
                    next_char = is.read();
                }
                Log.v("MY APP", sb.toString());
            } catch (IOException e) {
                Log.e("MY APP", e.toString());
            }
            finally {
                urlConnection.disconnect();
            }

        } catch (MalformedURLException e) {
            Log.e("MY APP", e.toString());
        } catch (IOException e) {
            Log.e("MY APP", e.toString());
        }
    }

    public String doRequest(Message m){
        String toReturn="";
        try {
            URL url = new URL(m.toStringRequest());
            Log.v("MY APP", "Request : " + m.toStringRequest());

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                StringBuilder sb = new StringBuilder();

                int next_char = is.read();
                while(next_char != -1){
                    sb.append((char) next_char);
                    next_char = is.read();
                }
                Log.v("MY APP", sb.toString());
                toReturn = sb.toString();
            } catch (IOException e) {
                Log.e("MY APP", e.toString());
            }
            finally {
                urlConnection.disconnect();
            }

        } catch (MalformedURLException e) {
            Log.e("MY APP", e.toString());
        } catch (IOException e) {
            Log.e("MY APP", e.toString());
        }

        return toReturn;
    }

    public void doStationRequest(String stationName){
        StationRequest sr = new StationRequest(stationName);
        List<Location> list = Location.locationList(doRequest(sr));
    }

    public void doPathRequest(String from, String to, String date, String time){
        PathRequest pr = new PathRequest(from, to, date, time);
        List<Connection> list = Connection.connectionList(doRequest(pr));
        Log.v("MY APP", doRequest(pr));
    }

    private class Message {
        String str;

        public Message() {
            str = "http://transport.opendata.ch/v1/";
        }

        public final String toStringRequest() {
            return str;
        }
    }

    private class LocationRequest extends Message {
        public LocationRequest() {
            super();
            super.str += "locations";
        }
    }

    private class StationRequest extends LocationRequest {
        public StationRequest(String name) {
            super();
            super.str += "?query="+name;
        }
    }

    private class ConnectionRequest extends Message {
        public ConnectionRequest() {
            super();
            super.str += "connections";
        }
    }

    private class PathRequest extends ConnectionRequest {
        public PathRequest(String from, String to, String date, String arrivalTime) {
            super();
            super.str += "?from="+from+"&to="+to+"&date="+date+"&time="+arrivalTime+"&isArrivalTime=1";
        }
    }
}
