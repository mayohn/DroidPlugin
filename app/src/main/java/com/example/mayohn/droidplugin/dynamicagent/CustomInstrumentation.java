package com.example.mayohn.droidplugin.dynamicagent;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CustomInstrumentation extends Instrumentation {
    private static String TAG = "CustomInstrumentation";
    private Instrumentation instrumentation;

    public CustomInstrumentation(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                            Intent intent, int requestCode, Bundle options) {
        // Hook之前, XXX到此一游!
        Log.d(TAG, "\n执行了startActivity, 参数如下: \n" + "who = [" + who + "], " +
                "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                "\ntarget = [" + target + "], \nintent = [" + intent +
                "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");
        Toast.makeText(who, "跳转到了" + intent, Toast.LENGTH_SHORT).show();
        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod("execStartActivity", Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(instrumentation, who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            // 某该死的rom修改了  需要手动适配
            throw new RuntimeException("do not support!!! pls adapt it");
        }
    }

    public Activity newActivity(Class<?> clazz, Context context,
                                IBinder token, Application application, Intent intent, ActivityInfo info,
                                CharSequence title, Activity parent, String id,
                                Object lastNonConfigurationInstance) throws InstantiationException,
            IllegalAccessException {
        Activity activity = (Activity) clazz.newInstance();
        // Activity.attach expects a non-null Application Object.
        if (application == null) {
            application = new Application();
        }
        try {
            Class<?> activityClass = Class.forName("android.app.Activity");
            Method[] methods=activityClass.getDeclaredMethods();
            Method attachMethod = null;
            for (int i=0;i<methods.length;i++){
                if (methods[i].getName().equals("attach")){
                    attachMethod=methods[i];
                }
            }
            attachMethod.setAccessible(true);
            attachMethod.invoke(activity,context, null, instrumentation, token, 0 /* ident */, application, intent,
                    info, title, parent, id,
                    lastNonConfigurationInstance,
                    new Configuration(), null /* referrer */, null /* voiceInteractor */,
                    null /* window */, null /* activityConfigCallback */);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }
}
