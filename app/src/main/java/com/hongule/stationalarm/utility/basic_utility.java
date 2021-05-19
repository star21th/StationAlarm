package com.hongule.stationalarm.utility;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class basic_utility {
    public static SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static SimpleDateFormat newFormat2 = new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat newFormat3 = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
    public static SimpleDateFormat newFormat4 = new SimpleDateFormat("yyyyMMdd");

    public static DecimalFormat formatter = new DecimalFormat("#,##0");
    public static DecimalFormat formatter2 = new DecimalFormat("#,###.#");

    public static DecimalFormat formatter3 = new DecimalFormat("#,###.##");

    public static String phonenumber_format(String phonenumber){
        // 전화번호 나누기, 하이픈(-) 표시
        if (phonenumber.contains("-")) {
            // 1) 하이픈(-) 있으면 그대로 표시
        }
        else {
            // 2) 구형폰(빌드 21 미만)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                phonenumber = PhoneNumberUtils.formatNumber(phonenumber);
                // 3) 신형폰
            else
                phonenumber = PhoneNumberUtils.formatNumber(phonenumber, Locale.getDefault().getCountry());
        }
        return phonenumber;
    }
    public static String getVersionInfo(Context context) {
        String version = "Unknown";
        PackageInfo packageInfo;

        if (context == null) {
            return version;
        }
        try {
            packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getApplicationContext().getPackageName(), 0 );
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("star21th", "getVersionInfo :" + e.getMessage());
        }
        return "VER : " + version;
    }


}
