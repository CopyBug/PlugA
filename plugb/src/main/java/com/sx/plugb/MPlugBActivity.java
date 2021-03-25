package com.sx.plugb;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;

public class MPlugBActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_plug_b);
    }

    @Override
    public Resources getResources() {
        if(getApplication()!=null&&getApplication().getResources()!=null){
            return getApplication().getResources();
        }
        return super.getResources();
    }
}