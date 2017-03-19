package com.example.chiaraercolani.travelpartner;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by martin on 18/03/17.
 */

public class DummyAsyncTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        String dep_date="2017-03-18T00:59:00+0200";

        ConstraintSolver cs = new ConstraintSolver("Malley", 2017, 03, 18, 00, 59, "Paris", 2017, 03, 19, 7, 0);
        Connection c = cs.getConnection();

        Section first_sec = c.sections.get(0);
        Location init_loc = first_sec.departure.station;
        Checkpoint dep_c = new Checkpoint(init_loc, dep_date, null, -1);
        Section init_sec = new Section(new Journey(), 1, dep_c, first_sec.departure);
        c.sections.add(init_sec);
        Log.v("MY APP", c.duration_day+"d"+c.duration_hour+":"+c.duration_min);



        HTTPRequest r = new HTTPRequest();
        Log.v("MY APP", r.doPointOfInterest("Zurich"));

        return null;
    }

}
