package com.iitb.nihhaar.twitter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Nihhaar on 10/4/2017.
 * This is class is for editing app wide properties.
 */

public class AppUtils {

    public static String servIP = "10.0.2.2";
    public static String webApp = "TwitterBackend";
    public static int servPort = 8080;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
