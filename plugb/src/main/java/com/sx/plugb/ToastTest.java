package com.sx.plugb;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ToastTest {
    public  void getToast(Context mContext){
        Toast.makeText(mContext,"这里是插件B",Toast.LENGTH_LONG).show();
        Log.i("LoadClassesUntil", "这里是插件B: ");
    }
}
