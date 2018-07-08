
// Track longitude, latitude, altitude and speed.

package com.example.snippet.gpstracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    static final int LOCATION_REQUEST_CODE = 1;

    private LocationManager locman;
    private LocationListener loclis;

    private TextView textView1, textView2, textView3,  textView4, textView5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);

        // Define a listener that responds to location updates
        loclis = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                // get info
                long time        = location.getTime();
                float speed      = location.getSpeed();
                double longitude = location.getLongitude();
                double latitude  = location.getLatitude();
                double altitude  = location.getAltitude();

                // display
                textView1.setText(String.format("%s", toDegMinSec(longitude, true)));
                textView2.setText(String.format("%s", toDegMinSec(latitude, false)));
                textView3.setText(String.format("%3.0f m", altitude));
                textView4.setText(String.format("%s", tokph(speed)));
                textView5.setText(String.format("%s", toMMDDYYYYhhmmss(time)));

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Toast.makeText(MainActivity.this, String.format("%s status changed", s), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(String s) {
                Toast.makeText(MainActivity.this, String.format("%s provider enabled", s), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String s) {
                Toast.makeText(MainActivity.this, String.format("%s provider disabled", s), Toast.LENGTH_SHORT).show();
            }
        };

        // check if it has permission
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // permission is not granted, request the permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        else
            // permission already granted, initalize location
            initialize();
    }

    // start update location
    @Override
    protected void onResume() {
        super.onResume();

        try {
            if(locman != null)
                locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, loclis);
        }catch (SecurityException e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    // stop update location
    @Override
    protected void onPause() {
        super.onPause();

        if(locman != null)
            locman.removeUpdates(loclis);
    }

    // callback method that get the result of the request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // responds to same request code you pass in requestPermissions
        if(requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initialize();
         }
    }

    //
    private void initialize(){

        try {
            locman = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location location = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location != null) {
                // get info
                long time        = location.getTime();
                float speed      = location.getSpeed();
                double longitude = location.getLongitude();
                double latitude  = location.getLatitude();
                double altitude  = location.getAltitude();

                // display
                textView1.setText(String.format("%s", toDegMinSec(longitude, true)));
                textView2.setText(String.format("%s", toDegMinSec(latitude, false)));
                textView3.setText(String.format("%3.0f m", altitude));
                textView4.setText(String.format("%s", tokph(speed)));
                textView5.setText(String.format("%s", toMMDDYYYYhhmmss(time)));
           }

        }catch (SecurityException e){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //
    private void modulus(double num, int[] inte, double[] frac){
        inte[0] = (int)num;
        frac[0] = num - (double)inte[0];
    }

    // 000°00'00.00" N
    private String toDegMinSec(double loc, boolean direction){
        int[] whl = new int[1];
        double[] frac = new double[1];
        int deg, min, sec;
        double num;
        char ch;

        //
        if(direction)
            ch = loc < 0.0?'W':'E'; // longitude
        else
            ch = loc < 0.0?'S':'N'; // latidue

        // gawing positive
        num = Math.abs(loc);

        // paghiwahiwalayin
        modulus(num, whl, frac);
        deg = whl[0];
        num = frac[0] * 60.0;
        modulus(num, whl, frac);
        min = whl[0];
        num = frac[0] * 60.0;
        modulus(num, whl, frac);
        sec = whl[0];

        return String.format("%3d°%2d'%2d\" %c", deg, min, sec, ch);
    }

    // MM-DD-YYYY HH:MM:SS
    private String toMMDDYYYYhhmmss(long time){
        Calendar c;
        int YYYY, MM, DD, hh, mm, ss, ms;

        c = Calendar.getInstance();
        c.setTimeInMillis(time);
        YYYY = c.get(Calendar.YEAR);
        MM = c.get(Calendar.MONTH) + 1;
        DD  = c.get(Calendar.DAY_OF_MONTH);
        hh  = c.get(Calendar.HOUR_OF_DAY);
        mm  = c.get(Calendar.MINUTE);
        ss  = c.get(Calendar.SECOND);
        ms  = c.get(Calendar.MILLISECOND);

        return String.format("%2d-%02d-%02d   %2d:%02d:%02d",MM,DD,YYYY,hh,mm,ss);
    }

    //  m    3600 sec     1 km
    // --- * -------- * ----------- =  3.6 kph
    //  s     1 hour    1000 m
    //
    private String tokph(float speed){
        float uph = speed *  3.6f;
        return String.format("%4.0f kph", uph);
    }
}
