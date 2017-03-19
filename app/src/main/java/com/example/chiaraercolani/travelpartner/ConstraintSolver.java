package com.example.chiaraercolani.travelpartner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by martin on 18/03/17.
 */

public class ConstraintSolver {

    String departure_day, departure_h;
    String arrival_day, arrival_h;
    String departurePlace;
    String arrivalPlace;

    public ConstraintSolver(String departurePlace, int departureYear, int departureMonth,
                            int departureDay, int departureHour, int departureMin,
                            String arrivalPlace, int arrivalYear, int arrivalMonth, int arrivalDay,
                            int arrivalHour, int arrivalMin) {
        this.departure_day = ""+departureYear+"-"+departureMonth+"-"+departureDay;
        this.departure_h = ""+departureHour+":"+departureMin;
        this.arrival_day = ""+arrivalYear+"-"+arrivalMonth+"-"+arrivalDay;
        this.arrival_h = ""+arrivalHour+":"+arrivalMin;
        this.arrivalPlace = arrivalPlace;
        this.departurePlace = departurePlace;
    }

    public ConstraintSolver(String departurePlace, String departure_day, String departure_h,
                            String arrivalPlace, String arrival_day, String arrival_h) {
        this.departure_day = departure_day;
        this.departure_h = departure_h;
        this.arrival_day = arrival_day;
        this.arrival_h = arrival_h;
        this.arrivalPlace = arrivalPlace;
        this.departurePlace = departurePlace;
    }

    public Connection getConnection() {
        DateFormat df_day = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat df_hour = new SimpleDateFormat("hh:mm");
        HTTPRequest request = new HTTPRequest();;
        List<Connection> list = request.doPathRequest(departurePlace, arrivalPlace,
                arrival_day, arrival_h);

        list.sort(new Comparator<Connection>() {
            @Override
            public int compare(Connection o1, Connection o2) {
                return o1.getScore() - o2.getScore();
            }
        });

        return list.size() == 0 ? null:list.get(0);

    }

}