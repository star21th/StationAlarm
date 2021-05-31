package com.hongule.stationalarm;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.hongule.stationalarm.data.location_data;
import com.hongule.stationalarm.data.location_data_class;
import com.hongule.stationalarm.data.location_line_data;
import com.hongule.stationalarm.thread.ServiceThread;
import com.hongule.stationalarm.utility.basic_utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service{

    private String TAG = "MyService";
    private static Timer timer;
    private boolean _isOverlayOn = false;
    WindowManager wm;
    View mView;
    public int select_temp = 0;
    public LocationManager locationManager = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public ArrayList<location_line_data> item_list_object = new ArrayList<>();

    @Override
    public void onCreate() {

        startLocationService();
        super.onCreate();

        location_data.seoul_item();
        location_check();
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                /*ViewGroup.LayoutParams.MATCH_PARENT*/300,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;

        mView = inflate.inflate(R.layout.view_in_service, null);
        final TextView _tex_station_name = (TextView) mView.findViewById(R.id.tex_station_name);
        _tex_station_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialogIntent = new Intent(getApplication(), MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(dialogIntent);
            }
        });
        // btn_img 에 android:filterTouchesWhenObscured="true" 속성 추가하면 터치리스너가 동작한다.
        _tex_station_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("test", "touch DOWN ");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("test", "touch UP");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("test", "touch move ");
                        break;
                }
                return false;
            }
        });
        wm.addView(mView, params);

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        TimerTask location_timer = new TimerTask() {
            @Override
            public void run() {

                wm.removeView(mView);
                location_data.seoul_item();
                location_check();
                if (_isOverlayOn) {

                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            /*ViewGroup.LayoutParams.MATCH_PARENT*/300,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT);

                    params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                    mView = inflate.inflate(R.layout.view_in_service, null);
                    final TextView _tex_station_name = (TextView) mView.findViewById(R.id.tex_station_name);
                    _tex_station_name.setText("");
                    _tex_station_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent dialogIntent = new Intent(getApplication(), MainActivity.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(dialogIntent);
                        }
                    });
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 사용하고자 하는 코드

                            wm.addView(mView, params);
                        }
                    }, 0);
                    _isOverlayOn = false;
                    Log.d("star21th210529", "false");
                } else {

                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            /*ViewGroup.LayoutParams.MATCH_PARENT*/300,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT);

                    params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                    mView = inflate.inflate(R.layout.view_in_service, null);
                    final TextView _tex_station_name = (TextView) mView.findViewById(R.id.tex_station_name);
                    _tex_station_name.setText(location_data.boardList.get(select_temp).name);
                    _tex_station_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent dialogIntent = new Intent(getApplication(), MainActivity.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(dialogIntent);
                        }
                    });
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 사용하고자 하는 코드

                            wm.addView(mView, params);
                        }
                    }, 0);
                    _isOverlayOn = true;
                    Log.d("star21th210529", "true");
                }
            }
        };
        timer.schedule(location_timer, 1000, 500);
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    //Handler mHandler;
    class Threadtest extends Thread{ //스레드 함수
        Handler mHandler = new Handler(){ //핸들러
            public void handleMessage(Message msg){
                startLocationServicetest();
            }
        };

        public void run(){
           //Message msg = new Message();
           // mHandler.sendMessage(msg);
            while(true){
                Looper.prepare();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG , "aaaaaaaaaaaaa");
                Message msg = new Message();
                mHandler.sendMessage(msg);
                Looper.loop();
            }
//핸들러로 메시지를 보내 실행
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");
        // Thread를 생성한다.

        //Threadtest thread = new Threadtest();
        //thread.start(); //스레드 시작

        return super.onStartCommand(intent, flags, startId);
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
/*    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Intent mMainIntent = new Intent(this, MainActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(
                this, 1, mMainIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.btn_star)
                        .setContentTitle("음악 플레이")
                        .setContentIntent(mPendingIntent)
                        .setContentText("백그라운드에서 음악이 플레이되고있습니다.");
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(001, mBuilder.build());

        return START_NOT_STICKY;
    }*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        //startLocationService();
/*        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }*/
    }

    public void startLocationServicetest() {
        Double latitude = 0.0;
        Double longitude = 0.0;
        String _kind = "";
        long minTime = 10000;
        float minDistance = 0;
        // get manager instance
        try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
            locationManager.removeUpdates(Listener);
        } catch (SecurityException ex){
        }
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
        Toast.makeText(getApplicationContext(), msg, 2000).show();
        //Toast.makeText(getApplicationContext(), "Location Service started.\nyou can test using DDMS.", 2000).show();
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
        Toast.makeText(getApplicationContext(), msg, 2000).show();

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

            Toast.makeText(MyService.this, "11111111111", 2000).show();
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
            Toast.makeText(MyService.this, msg, 2000).show();
/*            try{  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                locationManager.removeUpdates(Listener);
            } catch (SecurityException ex){
            }
            startLocationService();*/
        }

        public void onProviderDisabled(String provider) {

            Toast.makeText(MyService.this, "222222222", 2000).show();
        }

        public void onProviderEnabled(String provider) {
            Toast.makeText(MyService.this, "3333333333333", 2000).show();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MyService.this, "444444444444444", 2000).show();
        }

    };
}