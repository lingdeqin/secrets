package com.lingdeqin.secrets.base;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static MyApplication instance;

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
    }

    public static Application getApplication() {
        return instance;
    }

    public static Context getContext() {
        return context;
    }

}
