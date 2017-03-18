package com.example.chiaraercolani.travelpartner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity  {

    private final static int CALENDAR_ACTIVITY=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //new HTTPRequest().execute("Lausanne", "Geneve", "18-03-2017", "15:00");
    }

    public void buttonClicked(View view){
        Intent intent;
        switch(view.getId()){
            case R.id.btn_init:
                intent = new Intent(this,CalendarActivity.class);
                startActivityForResult(intent,CALENDAR_ACTIVITY);
                break;
        }
    }
}
