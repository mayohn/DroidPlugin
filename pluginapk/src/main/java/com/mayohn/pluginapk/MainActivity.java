package com.mayohn.pluginapk;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hookClipboard();
        findViewById(R.id.install).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File("storage/emulated/0/plugin.apk");
                try {
                    PluginManager.getInstance().installPackage("storage/emulated/0/plugin.apk", PackageManagerCompat.INSTALL_REPLACE_EXISTING);
                    Toast.makeText(MainActivity.this, "安装成功", Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.run).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage("com.example.mayohn.droidplugin");
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                Toast.makeText(MainActivity.this, "粘贴板的内容有" + clipData.getItemCount() + "个:" + clipData.getItemAt(0).getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 粘贴板hook
     */
    public static void hookClipboard() {
        try {
            final String CLIPBOARD_SERVICE = "clipboard";
            // 下面这一段的意思实际就是: ServiceManager.getService("clipboard");
            // 只不过 ServiceManager这个类是@hide的
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getDeclaredMethod("getService", String.class);
            // ServiceManager里面管理的原始的Clipboard Binder对象
            // 一般来说这是一个Binder代理对象
            IBinder rawBinder = (IBinder) getService.invoke(null, CLIPBOARD_SERVICE);

            // Hook 掉这个Binder代理对象的 queryLocalInterface 方法
            // 然后在 queryLocalInterface 返回一个IInterface对象, hook掉我们感兴趣的方法即可.
            IBinder hookedBinder = (IBinder) Proxy.newProxyInstance(serviceManager.getClassLoader(),
                    new Class<?>[]{IBinder.class},
                    new BinderHookProxyHandler(rawBinder));

            // 把这个hook过的Binder代理对象放进ServiceManager的cache里面
            // 以后查询的时候 会优先查询缓存里面的Binder, 这样就会使用被我们修改过的Binder了
            Field cacheField = serviceManager.getDeclaredField("sCache");
            cacheField.setAccessible(true);
            Map<String, IBinder> cache = (Map) cacheField.get(null);
            cache.put(CLIPBOARD_SERVICE, hookedBinder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class BinderHookProxyHandler implements InvocationHandler {
        private static final String TAG = "BinderHookProxyHandler";
        IBinder base;
        Class<?> stub;
        Class<?> iinterface;

        public BinderHookProxyHandler(IBinder base) {
            this.base = base;
            try {
                this.stub = Class.forName("android.content.IClipboard$Stub");
                this.iinterface = Class.forName("android.content.IClipboard");
            } catch (Exception e) {
                throw new RuntimeException("hooks failed!");
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("queryLocalInterface".equals(method.getName())) {
                Log.i(TAG, "hook queryLocalInterface");
                // 这里直接返回真正被Hook掉的Service接口
                // 这里的 queryLocalInterface 就不是原本的意思了
                // 我们肯定不会真的返回一个本地接口, 因为我们接管了 asInterface方法的作用
                // 因此必须是一个完整的 asInterface 过的 IInterface对象, 既要处理本地对象,也要处理代理对象
                // 这只是一个Hook点而已, 它原始的含义已经被我们重定义了; 因为我们会永远确保这个方法不返回null
                // 让 IClipboard.Stub.asInterface 永远走到if语句的else分支里面
                return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                        // asInterface 的时候会检测是否是特定类型的接口然后进行强制转换
                        // 因此这里的动态代理生成的类型信息的类型必须是正确的
                        new Class[]{IBinder.class, IInterface.class, this.iinterface},
                        new BinderHookHandler(base, stub)
                );
            }
            Log.d(TAG, "method:" + method.getName());
            return method.invoke(base, args);
        }
    }

    public static class BinderHookHandler implements InvocationHandler {
        private static final String TAG = "BinderHookHandler";
        private Object base;

        public BinderHookHandler(IBinder base, Class<?> stubClass) {
            try {
                Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder.class);
                this.base = asInterfaceMethod.invoke(null, base);
            } catch (Exception e) {
                throw new RuntimeException("hook failed!");
            }

        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("getPrimaryClip".equals(method.getName())) {
                Log.d(TAG, "hook getPrimaryClip");
                return ClipData.newPlainText(null, "hello hook");
            }
            if ("hasPrimaryClip".equals(method.getName())) {
                return true;
            }
            return method.invoke(base, args);
        }
    }
}
