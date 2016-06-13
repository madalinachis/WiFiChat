package com.example.madalina.wifigroupchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;

public class Application extends android.app.Application {

    public static final boolean APPDEBUG = false;
    public static final String APPTAG = "Chat";

    private static SharedPreferences preferences;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }

}