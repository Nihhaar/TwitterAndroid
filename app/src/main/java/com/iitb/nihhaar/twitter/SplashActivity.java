package com.iitb.nihhaar.twitter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* Saving the session cookies */
        Log.d("SplashActivity", "Saving the session cookies");
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(this);
        CookieManager cookieManager = new CookieManager(persistentCookieStore, CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        SharedPreferences logins = getSharedPreferences(AppUtils.LOGIN_PREFS_FILE,MODE_PRIVATE);
        boolean exists = logins.getBoolean("signedin",false);
        if(exists){
            Intent loggedin = new Intent(SplashActivity.this,MainActivity.class);
            startActivity(loggedin);
        }
        else{
            Intent noacc = new Intent(SplashActivity.this,LoginActivity.class);
            startActivity(noacc);
        }
        finish();
    }
}
