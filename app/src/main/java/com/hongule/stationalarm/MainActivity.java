package com.hongule.stationalarm;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hongule.stationalarm.common.global;
import com.hongule.stationalarm.data.location_data;
import com.hongule.stationalarm.data.location_data_class;
import com.hongule.stationalarm.data.location_line_data;
import com.hongule.stationalarm.data.sort_class;
import com.hongule.stationalarm.utility.basic_utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivity";
    private final Timer mTimer = new Timer();
    private TimerTask mTimerTask;
    private boolean _isOverlayOn = false;
    LinearLayout _lin_background;
    LinearLayout _lin_title;
    LinearLayout _lin_button;
    LinearLayout _lin_1;
    TextView _tex_title;
    TextView _tex_station_name;
    Button _but_pip;
    Button _but_config;
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

    public String location_name;
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
                    _tex_station_name.setText("");
                    _isOverlayOn = false;
                    break;
                case MESSAGE_2:
                    _tex_station_name.setText(location_name);
                    _isOverlayOn = true;
                    break;
                case MESSAGE_3:
                    _lin_background.setBackgroundColor(Color.parseColor("#00000000"));
                    _lin_title.setBackgroundColor(Color.parseColor("#00000000"));
                    _tex_title.setTextColor(Color.parseColor("#2196F3"));
                    _lin_button.setVisibility(View.GONE);
                    _lin_1.setVisibility(View.GONE);
                    if (mTimerTask != null)
                    mTimerTask.cancel();
                    mTimerTask = createTimerTask();
                    mTimer.schedule(mTimerTask, 1000, 500);
                    break;
                case MESSAGE_4:
                    _lin_background.setBackgroundColor(Color.parseColor("#ffffff"));
                    _lin_title.setBackgroundColor(Color.parseColor("#2263a5"));
                    _tex_title.setTextColor(Color.parseColor("#ffffff"));
                    _lin_button.setVisibility(View.VISIBLE);
                    _lin_1.setVisibility(View.VISIBLE);
                    if (mTimerTask != null)
                        mTimerTask.cancel();
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

        global.sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);

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
        _lin_background = (LinearLayout) findViewById(R.id.lin_background);
        _lin_title = (LinearLayout) findViewById(R.id.lin_title);
        _tex_title = (TextView) findViewById(R.id.tex_title);
        _tex_station_name = (TextView) findViewById(R.id.tex_station_name);
        _lin_button = (LinearLayout) findViewById(R.id.lin_button);
        _lin_1 = (LinearLayout) findViewById(R.id.lin_1);
        _but_pip = (Button) findViewById(R.id.but_pip);
        //_but_pip.setVisibility(View.GONE);
        _but_pip.setOnClickListener(this);
        _but_config = (Button) findViewById(R.id.but_config);
        _but_config.setOnClickListener(this);
        mTimerTask = createTimerTask();
        mTimer.schedule(mTimerTask, 1000, 500);
    }
    private TimerTask createTimerTask() {
        TimerTask timerTask = new TimerTask() {
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
        return timerTask;
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
    public void onPause() {
        // If called while in PIP mode, do not pause playback
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode()) {
                Message message = handler.obtainMessage(MESSAGE_3);
                handler.sendMessage(message);
            } else {
                Message message = handler.obtainMessage(MESSAGE_4);
                handler.sendMessage(message);
            }
        }
    }
    @Override
    public void onResume() {
        // If called while in PIP mode, do not pause playback
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(isInPictureInPictureMode()) {
                Message message = handler.obtainMessage(MESSAGE_3);
                handler.sendMessage(message);
            } else {
                Message message = handler.obtainMessage(MESSAGE_4);
                handler.sendMessage(message);
            }
        }
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        return;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.but_pip:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    enterPictureInPictureMode(params);
                }
                break;
            case R.id.but_config:
                Intent intent = new Intent(this, ConfigActivity.class);
                startActivityForResult(intent, 100);
                break;
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if(isInPictureInPictureMode) {
        } else {
        }
    }

    public void location_check() {
        for (int i = 0; i < location_data.boardList.size(); i++) {
            if (location_data.boardList.get(i).lot >= Double.valueOf(location_data_class.latitude)) {
                select_temp = i;
                break;
            }
        }

        ArrayList<sort_class> data = new ArrayList<sort_class>();
        for (int i = select_temp - 10; i < select_temp + 10; i++) {
            if( i < 0 ) i=0;
            if( i > location_data.boardList.size() ) i= location_data.boardList.size();
            data.add(new sort_class(location_data.boardList.get(i).name, location_data.boardList.get(i).lon + location_data.boardList.get(i).lot));
        }
        PointDecending pointDecending = new PointDecending();
        Collections.sort(data, pointDecending);

        Double aaa = Double.valueOf(location_data_class.latitude) + Double.valueOf(location_data_class.longitude);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getPoint() <= aaa) {
                location_name = data.get(i).getName();
                break;
            }
        }


    }
    /*Test객체의 name을 기준으로 내림차순 정렬하기*/
    class NameDecending implements Comparator<sort_class> {

        @Override
        public int compare(sort_class a, sort_class b) {

            String temp1 = a.getName();
            String temp2 = b.getName();

            return temp2.compareTo(temp1);
            /*return b.getName().compareTo(a.getName());*/
        }
    }

    /*Test객체의 Point를 기준으로 내림차순 정렬하기*/
    class PointDecending implements Comparator<sort_class> {

        @Override
        public int compare(sort_class a, sort_class b) {
            Double temp1 = a.getPoint();
            Double temp2 = b.getPoint();

            return temp2.compareTo(temp1);
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
            //Toast.makeText(MainActivity.this, msg, 2000).show();

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

}