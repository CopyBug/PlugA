package com.sx.pluguntil.until;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sx.pluguntil.ProxyActivity;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class LoadClassesUntil {
    private static String TAG="LoadClassesUntil";
    //将一个apk里面的
    public static String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/$MuMu共享文件夹/plug.apk";
    public static Context cContext;
    public static void load(Context mContext) {
        cContext=mContext;
        PathClassLoader pathClassLoader = (PathClassLoader) mContext.getClassLoader();
        try {
            //获取BaseDexClassLoader父类加载器
            Class<?> aClass = Class.forName("dalvik.system.BaseDexClassLoader");
            //获取pathlist的成员变量
            Field pathList = aClass.getDeclaredField("pathList");
            //运行访问
            pathList.setAccessible(true);
            //得到全局的pathList
            Object pathListValue = pathList.get(pathClassLoader);
            //数组
            Field dexElements = pathListValue.getClass().getDeclaredField("dexElements");
            dexElements.setAccessible(true);
            Object dexElement = dexElements.get(pathListValue);
            /*------------------------------------------------------*/
            //创建一个新的Dex类加载容器
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath, mContext.getCacheDir().getAbsolutePath(), null, mContext.getClassLoader());
            Object plugList = pathList.get(dexClassLoader);
            Object plugElements = dexElements.get(plugList);
            //获取该上下文类加载容器里面的长度
            int currentLength = Array.getLength(dexElement);
            //获取插件的容器长度
            int plugLength = Array.getLength(plugElements);
            //获取数组的类型
            Class<?> componentType = dexElement.getClass().getComponentType();
            //创建一个新的数组
            Object newDexElements = Array.newInstance(componentType, currentLength + plugLength);
            //cv 第一个参数是数据源,数据源起始位置，目标数组，目标起始位置，要复制的长度
            System.arraycopy(dexElement, 0, newDexElements, 0, currentLength);
            System.arraycopy(plugElements, 0, newDexElements, currentLength, plugLength);
            //设置回去
            dexElements.set(pathListValue, newDexElements);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("LoadClassesUntil", "load: " + e.getMessage());
        }
    }

    public static final String EXTRA_ORIGIN_INTENT = "EXTRA_ORIGIN_INTENT";

    //拦截AndroidManifest.xml对Activity的安全检测
    public static void proxyAMS(Context mContext) {
        try {
            Log.i(TAG, "build: "+ Build.VERSION.SDK_INT);
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            //IActivityManager
            Field mInstanceFiled = singletonClass.getDeclaredField("mInstance");
            mInstanceFiled.setAccessible(true);
            Class<?> AMNClass = Class.forName("android.app.ActivityManagerNative");
            Class<?> iamClass = Class.forName("android.app.IActivityManager");
            Object IActivityManager=null;
            Object mInstance=null;
            if(Build.VERSION.SDK_INT==Build.VERSION_CODES.M){
                //android 6.0
                Field gDefaultField = AMNClass.getDeclaredField("gDefault");
                gDefaultField.setAccessible(true);
                //IActivityManager
                IActivityManager = gDefaultField.get(null);
                mInstance = mInstanceFiled.get(IActivityManager);

            }
            Object finalMInstance = mInstance;
            Object  IActivityManagerProxy = Proxy.newProxyInstance(LoadClassesUntil.class.getClassLoader(), new Class[]{iamClass}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    //这里进行代理操作
                    //拦截IActivityManager.startActivity(whoThread,who.getBasePackageName(), who.getAttributionTag(), intent,intent.resolveTypeIfNeeded(who.getContentResolver()), token,        target != null ? target.mEmbeddedID : null, requestCode, 0, null, options)
                    //这个方法
                    if (method.getName().equals("startActivity")) {
                        //因为这个方法的第三个参数是Intent,所以我们这里先用这个代理的activity的替换
                        Intent tempIntent = (Intent) args[2];
                        //这个ProxyActivity是已经在AndroidManifest.xml注册过了
                        Intent intent = new Intent(mContext, ProxyActivity.class);
                        intent.putExtra(EXTRA_ORIGIN_INTENT, tempIntent);
                        //替换Intent
                        args[2] = intent;

                    }
                    //((IActivityManager)IActivityManagerStatic).startActivity()
                    return method.invoke(finalMInstance, args);
                }
            });
            //重定向静态 将原来的mInstance重新赋值IActivityManagerProxy
            mInstanceFiled.set(IActivityManager, IActivityManagerProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    public static void hookLauncherActivity() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            //获取   final H mH = new H()
            Field mField = activityThreadClass.getDeclaredField("mH");
            mField.setAccessible(true);
            //    private static volatile ActivityThread sCurrentActivityThread; 静态类，获取自己
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            Object sCurrentActivityThread = sCurrentActivityThreadField.get(null);
            Object mH = mField.get(sCurrentActivityThread);
            Class<?> handleClass = Class.forName("android.os.Handler");
            Field mCallbackField = handleClass.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH,new Handler.Callback(){
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    Log.i(TAG, "handleMessage: "+msg.what);
                    switch (msg.what){
                        case 100:
                            handleLaunchActivity(msg);
                            try {
                                // 兼容AppCompatActivity报错问题
                                // 我自己执行一次那么就会创建PackageManager，系统再获取的时候就是下面的iPackageManager
                                Method getPackageManager = sCurrentActivityThread.getClass().getDeclaredMethod("getPackageManager");
                                getPackageManager.setAccessible(true);
                                Object iPackageManager = getPackageManager.invoke(sCurrentActivityThread);
                                PackageManagerHandler handler = new PackageManagerHandler(iPackageManager);
                                Class<?> iPackageManagerIntercept = Class.forName("android.content.pm.IPackageManager");
                                Object proxy = Proxy.newProxyInstance(LoadClassesUntil.class.getClassLoader(),
                                        new Class<?>[]{iPackageManagerIntercept}, handler);
                                // 获取 sPackageManager 属性
                                Field iPackageManagerField = sCurrentActivityThread.getClass().getDeclaredField("sPackageManager");
                                iPackageManagerField.setAccessible(true);
                                //启动代理
                                iPackageManagerField.set(sCurrentActivityThread, proxy);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            break;
                    }
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Resources loadResource(Context mContext){
        Class<AssetManager> assetManagerClass = AssetManager.class;
        try {
            AssetManager assetManager = assetManagerClass.newInstance();
            Method addAssetPathMethod = assetManagerClass.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager,apkPath);
            Resources resources = mContext.getResources();
            return new Resources(assetManager,resources.getDisplayMetrics(),resources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 开始启动创建Activity拦截
     *
     * @param msg
     */
    private static void handleLaunchActivity(Message msg) {
        try {
            Object record = msg.obj;
            // 1.从record 获取过安检的Intent
            Field intentField = record.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent safeIntent = (Intent) intentField.get(record);
            // 2.从safeIntent中获取原来的originIntent
            Intent originIntent = safeIntent.getParcelableExtra(EXTRA_ORIGIN_INTENT);
            // 3.重新设置回去
            if (originIntent != null) {
                intentField.set(record, originIntent);
            }
            //----------------------------------

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public static class PackageManagerHandler implements InvocationHandler {

        private Object mActivityManagerObject;

        public PackageManagerHandler(Object iActivityManagerObject) {
            this.mActivityManagerObject = iActivityManagerObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().startsWith("getActivityInfo")) {
                ComponentName componentName = (ComponentName) args[0];
                Class<?> aClass = cContext.getClassLoader().loadClass(componentName.getClassName());
                args[0]=new ComponentName(cContext,componentName.getClassName());
            }
            return method.invoke(mActivityManagerObject, args);
        }
    }
}
