package com.iitb.nihhaar.twitter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* Saving the session cookies */
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(this), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
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
