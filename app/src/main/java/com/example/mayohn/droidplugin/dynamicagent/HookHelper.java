package com.example.mayohn.droidplugin.dynamicagent;

import android.app.Activity;
import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HookHelper {
    /**
     * 代理getApplicationContext:contextImpl中的execStartActivity
     *
     * @throws Exception
     */
    public static void activityThreadInstrumentation() {
        try {
            //获取activityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //静态方法可以使用null
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);

            //创建代理变量
            Instrumentation customInstrumentation = new CustomInstrumentation(mInstrumentation);
            mInstrumentationField.set(currentActivityThread, customInstrumentation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void onCreate(Activity activity) throws Exception{
        // 先获取到当前的ActivityThread对象
        Class<?> activityClass = Class.forName("android.app.Activity");
        // 拿到原始的 mInstrumentation字段
        Field mInstrumentationField = activityClass.getDeclaredField("mInstrumentation");
        mInstrumentationField.setAccessible(true);
        Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(activity);

        // 创建代理对象
        Instrumentation evilInstrumentation = new CustomInstrumentation(mInstrumentation);

        // 偷梁换柱
        mInstrumentationField.set(activity, evilInstrumentation);
    }
}
