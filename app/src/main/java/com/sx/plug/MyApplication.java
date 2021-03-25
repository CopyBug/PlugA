package com.sx.plug;

import android.app.Application;
import android.content.res.Resources;

import com.sx.pluguntil.until.LoadClassesUntil;

public class MyApplication extends Application {

    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();
        LoadClassesUntil.load(this);
        LoadClassesUntil.proxyAMS(this);
        LoadClassesUntil.hookLauncherActivity();
        resources = LoadClassesUntil.loadResource(this);
    }

    @Override
    public Resources getResources() {
        return resources==null?super.getResources():resources;
    }
}
