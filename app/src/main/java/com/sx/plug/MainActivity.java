package com.sx.plug;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    Class<?> MPlugBActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Class<?> aClass = getClassLoader().loadClass("com.sx.plugb.ToastTest");
            Method toast = aClass.getDeclaredMethod("getToast", Context.class);
            MPlugBActivity = getClassLoader().loadClass("com.sx.plugb.MPlugBActivity");
            toast.setAccessible(true);
            toast.invoke(aClass.newInstance(), this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("LoadClassesUntil", "load: " + e.getMessage());
        }
        findViewById(R.id.act_main_plug_bt)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转到插件
                        Intent intent = null;
                        try {
                            intent = new Intent(MainActivity.this, MPlugBActivity);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
    }


}