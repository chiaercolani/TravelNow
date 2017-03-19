package com.example.chiaraercolani.travelpartner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class CalendarActivity extends Activity
    implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    private CalAdapter adapter;
    private List<Event> eventList;
    private Runnable run;
    public static DateTime date;
    public static DateTime tomorrow;
    /**
    * Create the main activity.
    * @param savedInstanceState previously saved instance data.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        eventList = new ArrayList<>();

        final ListView cal_view = (ListView) findViewById(R.id.list_cal);

        adapter = new CalAdapter(getApplicationContext() , eventList,cal_view);
        cal_view.setAdapter(adapter);

        run = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                cal_view.invalidateViews();
                cal_view.refreshDrawableState();
            }
        };

    // Initialize credentials and service object.
    mCredential = GoogleAccountCredential.usingOAuth2(
            getApplicationContext(), Arrays.asList(SCOPES))
            .setBackOff(new ExponentialBackOff());

    final CalendarView calendarView = (CalendarView) findViewById(R.id.cal_view_id);

    calendarView.setOnDateChangeListener( new CalendarView.OnDateChangeListener() {
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            CalendarActivity.date = new DateTime(new Date(year-1900, month, dayOfMonth).getTime());
            if ((month == 1) && (dayOfMonth >= 28)) {
                dayOfMonth = 1;
                month++;
            } else if((month == 0 || month == 2
                    || month == 4 || month == 6
                    || month == 7 || month == 9) && dayOfMonth == 31){
                dayOfMonth = 1;
                month++;
            } else if (month == 11 && dayOfMonth == 31){
                dayOfMonth = 1;
                month = 0;
                year++;
            } else if ((month == 3 || month == 5 || month == 8 || month == 10) && dayOfMonth == 30) {
                dayOfMonth = 1;
                month++;
            } else {
                dayOfMonth++;
            }
            CalendarActivity.tomorrow = new DateTime(new Date(year-1900, month, dayOfMonth).getTime());
            getResultsFromApi();
        }//met
    });

        getResultsFromApi();
    }




    /**
    * Attempt to call the API, after verifying that all the preconditions are
    * satisfied. The preconditions are: Google Play Services installed, an
    * account was selected and the device currently has online access. If any
    * of the preconditions are not satisfied, the app will prompt the user as
    * appropriate.
    */
    private void getResultsFromApi() {
    if (! isGooglePlayServicesAvailable()) {
        acquireGooglePlayServices();
    } else if (mCredential.getSelectedAccountName() == null) {
        chooseAccount();
    } else if (! isDeviceOnline()) {
//        mOutputText.setText("No network connection available.");
    } else {
        new MakeRequestTask(mCredential).execute();
    }
    }

    /**
    * Attempts to set the account used with the API credentials. If an account
    * name was previously saved it will use that one; otherwise an account
    * picker dialog will be shown to the user. Note that the setting the
    * account to use with the credentials object requires the app to have the
    * GET_ACCOUNTS permission, which is requested here if it is not already
    * present. The AfterPermissionGranted annotation indicates that this
    * function will be rerun automatically whenever the GET_ACCOUNTS permission
    * is granted.
    */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
    if (EasyPermissions.hasPermissions(
            this, Manifest.permission.GET_ACCOUNTS)) {
        String accountName = getPreferences(Context.MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            getResultsFromApi();
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        }
    } else {
        // Request the GET_ACCOUNTS permission via a user dialog
        EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account (via Contacts).",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS);
    }
    }

    /**
    * Called when an activity launched here (specifically, AccountPicker
    * and authorization) exits, giving you the requestCode you started it with,
    * the resultCode it returned, and any additional data from it.
    * @param requestCode code indicating which activity result is incoming.
    * @param resultCode code indicating the result of the incoming
    *     activity result.
    * @param data Intent (containing result data) returned by incoming
    *     activity result.
    */
    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch(requestCode) {
        case REQUEST_GOOGLE_PLAY_SERVICES:
            if (resultCode != RESULT_OK) {
            } else {
                getResultsFromApi();
            }
            break;
        case REQUEST_ACCOUNT_PICKER:
            if (resultCode == RESULT_OK && data != null &&
                    data.getExtras() != null) {
                String accountName =
                        data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                if (accountName != null) {
                    SharedPreferences settings =
                            getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, accountName);
                    editor.apply();
                    mCredential.setSelectedAccountName(accountName);
                    getResultsFromApi();
                }
            }
            break;
        case REQUEST_AUTHORIZATION:
            if (resultCode == RESULT_OK) {
                getResultsFromApi();
            }
            break;
    }
    }

    /**
    * Respond to requests for permissions at runtime for API 23 and above.
    * @param requestCode The request code passed in
    *     requestPermissions(android.app.Activity, String, int, String[])
    * @param permissions The requested permissions. Never null.
    * @param grantResults The grant results for the corresponding permissions
    *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
    */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                       @NonNull String[] permissions,
                                       @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    EasyPermissions.onRequestPermissionsResult(
            requestCode, permissions, grantResults, this);
    }

    /**
    * Callback for when a permission is granted using the EasyPermissions
    * library.
    * @param requestCode The request code associated with the requested
    *         permission
    * @param list The requested permission list. Never null.
    */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
    // Do nothing.
    }

    /**
    * Callback for when a permission is denied using the EasyPermissions
    * library.
    * @param requestCode The request code associated with the requested
    *         permission
    * @param list The requested permission list. Never null.
    */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
    // Do nothing.
    }

    /**
    * Checks whether the device currently has a network connection.
    * @return true if the device has a network connection, false otherwise.
    */
    private boolean isDeviceOnline() {
    ConnectivityManager connMgr =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
    }

    /**
    * Check that Google Play services APK is installed and up to date.
    * @return true if Google Play Services is available and up to
    *     date on this device; false otherwise.
    */
    private boolean isGooglePlayServicesAvailable() {
    GoogleApiAvailability apiAvailability =
            GoogleApiAvailability.getInstance();
    final int connectionStatusCode =
            apiAvailability.isGooglePlayServicesAvailable(this);
    return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
    * Attempt to resolve a missing, out-of-date, invalid or disabled Google
    * Play Services installation via a user dialog, if possible.
    */
    private void acquireGooglePlayServices() {
    GoogleApiAvailability apiAvailability =
            GoogleApiAvailability.getInstance();
    final int connectionStatusCode =
            apiAvailability.isGooglePlayServicesAvailable(this);
    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
        showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }
    }


    /**
    * Display an error dialog showing that Google Play Services is missing
    * or out of date.
    * @param connectionStatusCode code describing the presence (or lack of)
    *     Google Play Services on this device.
    */
    void showGooglePlayServicesAvailabilityErrorDialog(
        final int connectionStatusCode) {
    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
    Dialog dialog = apiAvailability.getErrorDialog(
            CalendarActivity.this,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES);
    dialog.show();
    }

    /**
    * An asynchronous task that handles the Google Calendar API call.
    * Placing the API calls in their own task ensures the UI stays responsive.
    */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
    private com.google.api.services.calendar.Calendar mService = null;
    private Exception mLastError = null;

    MakeRequestTask(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build();
    }

    /**
     * Background task to call Google Calendar API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {
        // List the next 10 events from the primary calendar.
        if(CalendarActivity.date == null) {
            CalendarActivity.date = new DateTime(System.currentTimeMillis() );
            CalendarActivity.tomorrow = new DateTime((System.currentTimeMillis() + 1000*3600*24));
        }
        List<String> eventStrings = new ArrayList<String>();


        Events events = mService.events().list("primary")
                .setTimeMin(CalendarActivity.date)
                .setTimeMax(CalendarActivity.tomorrow)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();

        List<Event> items_train = new LinkedList<>();

        if(items.size() != 0) {
            items_train.add(items.get(0));
            for(int i = 1; i < items.size(); i++) {
                Event e = items.get(i);
                Event pe = items.get(i-1);

                EventDateTime edt = pe.getEnd();
                DateTime dt = edt.getDateTime();
                String departure = dt.toStringRfc3339();
                String departure_day = departure.substring(0,10);
                String departure_h = pe.getStart().getDateTime().toStringRfc3339().substring(11, 16);

                edt = e.getStart();
                dt = edt.getDateTime();
                String arrival = dt.toStringRfc3339();
                String arrival_day = e.getStart().getDateTime().toStringRfc3339().substring(0,10);
                String arrival_h = e.getStart().getDateTime().toStringRfc3339().substring(11, 16);


                ConstraintSolver cs = new ConstraintSolver(pe.getLocation(), departure_day, departure_h, e.getLocation(), arrival_day, arrival_h);
                Connection c = cs.getConnection();

                if(c != null) {
                    for(int j = 0; j < c.sections.size(); j+=2){
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");

                        Checkpoint depart_chk = c.sections.get(j).departure;
                        Checkpoint arrival_chk = c.sections.get(j).arrival;

                        Event tr_e = new Event();
                        tr_e.setSummary("Train proposal.");
                        tr_e.setLocation(depart_chk.station.toString() + "->" + arrival_chk.station.toString());
                        try {
                            String dep = depart_chk.departure;
                            Date start_date = df.parse(dep);
                            DateTime start_dt = new DateTime(start_date);
                            EventDateTime startDate = new EventDateTime().setDateTime(start_dt);
                            tr_e.setStart(startDate);

                            String arr = arrival_chk.arrival;
                            Date end_date = df.parse(arr);
                            DateTime end_dt = new DateTime(end_date);
                            EventDateTime endDate = new EventDateTime().setDateTime(end_dt);
                            tr_e.setEnd(endDate);

                            items_train.add(tr_e);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                items_train.add(items.get(i));
            }
        }

        adapter.addItems(items_train);

        runOnUiThread(run);


        return eventStrings;
    }



    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(List<String> output) {
    }

    @Override
    protected void onCancelled() {
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        CalendarActivity.REQUEST_AUTHORIZATION);
            }
        }
    }
    }
}
