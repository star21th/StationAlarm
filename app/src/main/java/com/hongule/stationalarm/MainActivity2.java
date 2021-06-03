package com.hongule.stationalarm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.util.Rational;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.data.location_data;
import com.example.myapplication.data.location_data_class;
import com.example.myapplication.data.location_line_data;
import com.example.myapplication.utility.basic_utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private static Timer timer;
    private boolean _isOverlayOn = false;
    TextView _tex_station_name;
    Button _but_pip;
    public int select_temp = 0;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public LocationManager locationManager = null;
    public ArrayList<location_line_data> item_list_object = new ArrayList<>();
    PictureInPictureParams params;
    public static final int MESSAGE_1   = 1;
    public static final int MESSAGE_2   = 2;
    public static final int MESSAGE_3   = 3;
    public static final int MESSAGE_4   = 4;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            location_check();
            switch (message.what) {
                case MESSAGE_1:
                    //try { Thread.sleep(300);} catch (InterruptedException e) {}
                    _tex_station_name.setText("");
                    _isOverlayOn = false;
                    Log.d("star21th210529", "false");
                    break;
                case MESSAGE_2:
                    //try { Thread.sleep(300);} catch (InterruptedException e) {}
                    _tex_station_name.setText(location_data.boardList.get(select_temp).name);
                    _isOverlayOn = true;
                    Log.d("star21th210529", "true");
                    break;
                case MESSAGE_3:
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    _but_pip.setBackgroundColor(Color.parseColor("#ecf5fa"));
                    break;
                case MESSAGE_4:
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    _but_pip.setBackgroundColor(Color.parseColor("#aaaaaa"));
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 권한이 허용되어있지않다면 권한요청
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            checkPermission();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Display display=getWindowManager().getDefaultDisplay();
            Point size=new Point();
            display.getSize(size);
            int width=size.x;
            int height=size.y;
            Rational aspectRatio = new Rational(192, 108);
            params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio).build();
            enterPictureInPictureMode(params);
        }

        startLocationService();
        location_data.seoul_item();
        location_check();
        _tex_station_name = (TextView) findViewById(R.id.tex_station_name);
        _but_pip = (Button) findViewById(R.id.but_pip);
        //_but_pip.setVisibility(View.GONE);
        _but_pip.setOnClickListener(this);
        timer = new Timer();
        TimerTask location_timer = new TimerTask() {
            @Override
            public void run() {
                if (_isOverlayOn) {
                    Message message = handler.obtainMessage(MESSAGE_1);
                    handler.sendMessage(message);

                } else {

                    Message message = handler.obtainMessage(MESSAGE_2);
                    handler.sendMessage(message);

                }
            }
        };
        timer.schedule(location_timer, 1000, 500);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // TODO 동의를 얻지 못했을 경우의 처리

                } else {
                    //startService(new Intent(MainActivity.this, MyService.class));
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.but_pip:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode(params);
                }
                break;
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if(isInPictureInPictureMode) {
            Message message = handler.obtainMessage(MESSAGE_3);
            handler.sendMessage(message);
            Toast.makeText(this, "PIP Mode", Toast.LENGTH_SHORT).show();
        } else {
            Message message = handler.obtainMessage(MESSAGE_4);
            handler.sendMessage(message);
            Toast.makeText(this, "not PIP Mode", Toast.LENGTH_SHORT).show();
        }
    }

    public void location_check() {
        for (int i = 0; i < location_data.boardList.size(); i++) {
            if (location_data.boardList.get(i).lot >= Double.valueOf(location_data_class.latitude)) {
                select_temp = i;
                break;
            }
        }

        for (int i = select_temp - 1; i < select_temp + 1; i++) {
            if (location_data.boardList.get(i).lon >= Double.valueOf(location_data_class.longitude)) {
                select_temp = i;
                break;
            }
            select_temp = i;
        }

    }
    public void startLocationService() {
        Double latitude = 0.0;
        Double longitude = 0.0;
        String _kind = "";
        long minTime = 10000;
        float minDistance = 0;
        // get manager instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
        //Toast.makeText(getApplicationContext(), msg, 2000).show();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                (LocationListener) Listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, (LocationListener) Listener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minTime, minDistance, (LocationListener) Listener);

        //Toast.makeText(getApplicationContext(), "Location Service started.\nyou can test using DDMS.", 2000).show();
    }


    private LocationListener  Listener  = new LocationListener(){

        public void onLocationChanged(Location location) {
            //capture location data sent by current provider
            Double latitude = 0.0;
            Double longitude = 0.0;
            String _kind = "";

            //Toast.makeText(MyService.this, "11111111111", 2000).show();
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
            Toast.makeText(MainActivity.this, msg, 2000).show();
/*            try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                locationManager.removeUpdates(Listener);
            } catch (SecurityException ex){
            }
            startLocationService();*/
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

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
            }
        }
    }
    <?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
            package="com.example.myapplication">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.Transparent">
        <activity
    android:name=".MainActivity"
    android:supportsPictureInPicture="true">
            <layout
    android:defaultWidth="450dp"
    android:defaultHeight="350dp"
    android:gravity="start|left" />

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
}