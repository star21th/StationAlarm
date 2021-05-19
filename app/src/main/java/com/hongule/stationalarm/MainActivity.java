package com.hongule.stationalarm;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hongule.stationalarm.data.location_data_class;
import com.hongule.stationalarm.utility.basic_utility;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.PACKAGE_USAGE_STATS
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 권한이 허용되어있지않다면 권한요청
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        startLocationService();

        Button bt_start = (Button) findViewById(R.id.bt_start);
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        Button bt_stop = (Button) findViewById(R.id.bt_stop);
        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(MainActivity.this, MyService.class));
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // TODO 동의를 얻지 못했을 경우의 처리

            } else {
                startService(new Intent(MainActivity.this, MyService.class));
            }
        }
    }
    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void checkPermission() {

        boolean granted = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                startService(new Intent(MainActivity.this, MyService.class));
            }
        } else {
            startService(new Intent(MainActivity.this, MyService.class));
        }
    }

    public void startLocationService() {
        Double latitude = 0.0;
        Double longitude = 0.0;
        String _kind = "";

        // get manager instance
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // set listener
        Listener Listener = new Listener();
        long minTime = 10000;
        float minDistance = 0;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            _kind = "1";
        }
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            _kind = "2";
        }
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastKnownLocation != null) {
            latitude = lastKnownLocation.getLatitude();
            longitude = lastKnownLocation.getLongitude();
            _kind = "3";
        }
        String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
        Log.i("GPSLocationService", msg);
        Date today = Calendar.getInstance().getTime();
        location_data_class.coord_date = basic_utility.newFormat.format(today);
        location_data_class.coord_kind = _kind;
        location_data_class.latitude = String.valueOf(latitude);
        location_data_class.longitude = String.valueOf(longitude);
        //  Toast.makeText(getApplicationContext(), msg, 2000).show();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                Listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, (LocationListener) Listener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, (LocationListener) Listener);

        //Toast.makeText(getApplicationContext(), "Location Service started.\nyou can test using DDMS.", 2000).show();
    }

    private class Listener implements LocationListener {

        public void onLocationChanged(Location location) {
            //capture location data sent by current provider
            Double latitude = 0.0;
            Double longitude = 0.0;
            String _kind = "";

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                _kind = "1";
                Log.d(TAG + " GPS : ", Double.toString(latitude) + '/' + Double.toString(longitude));
            }

            if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                _kind = "2";
                Log.d(TAG + " NETWORK : ", Double.toString(latitude) + '/' + Double.toString(longitude));
            }

            if (location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                _kind = "3";
                Log.d(TAG + " PASSIVE : ", Double.toString(latitude) + '/' + Double.toString(longitude));
            }

            String msg = "Latitude : " + latitude + "\nLongitude:" + longitude;
            Log.i("GPSLocationService", msg);
            Date today = Calendar.getInstance().getTime();
            location_data_class.coord_date = basic_utility.newFormat.format(today);
            location_data_class.coord_kind = _kind;
            location_data_class.latitude = String.valueOf(latitude);
            location_data_class.longitude = String.valueOf(longitude);
            //  Toast.makeText(getApplicationContext(), msg, 2000).show();

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }
}