package com.sx.pluguntil;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.fragment.app.FragmentActivity;

import com.sx.pluguntil.until.LoadClassesUntil;

public class ProxyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
    }
    @Override
    public Resources getResources() {
        if(getApplication()!=null&&getApplication().getResources()!=null){
            return getApplication().getResources();
        }
        return super.getResources();
    }
}