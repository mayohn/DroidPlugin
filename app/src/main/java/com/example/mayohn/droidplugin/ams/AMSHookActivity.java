package com.example.mayohn.droidplugin.ams;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mayohn.droidplugin.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AMSHookActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ams_hook);
        hookAMS();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("http://www.baidu.com"));
    }

    public void hookAMS() {
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManager");
            Field IActivityManagerSingleton = activityManagerNativeClass.getDeclaredField("IActivityManagerSingleton");
            IActivityManagerSingleton.setAccessible(true);
            Object gDefault = IActivityManagerSingleton.get(null);

            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            Object rawIActivityManager = mInstanceField.get(gDefault);
            // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iActivityManagerInterface}, new IActivityManagerHandler(rawIActivityManager));
            mInstanceField.set(gDefault, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hook44AMS() {
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            // 获取 gDefault 这个字段, 想办法替换它
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object gDefault = gDefaultField.get(null);

            // 4.x以上的gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            // ActivityManagerNative 的gDefault对象里面原始的 IActivityManager对象
            Object rawIActivityManager = mInstanceField.get(gDefault);

            // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iActivityManagerInterface}, new IActivityManagerHandler(rawIActivityManager));
            mInstanceField.set(gDefault, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class IActivityManagerHandler implements InvocationHandler {
        private Object rawIActivityManager;

        public IActivityManagerHandler(Object rawIActivityManager) {
            this.rawIActivityManager = rawIActivityManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.i("test", "invoke:hahahsuscess");
            return method.invoke(rawIActivityManager, args);
        }
    }
}
