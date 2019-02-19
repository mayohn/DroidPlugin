package com.example.mayohn.droidplugin.dynamicagent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.mayohn.droidplugin.R;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.Arrays;

public class DynamicAgentActivity extends AppCompatActivity implements View.OnClickListener {
    private Button buymyself;
    private Button buyproxy;
    private Button dynamic_buyproxy;
    private Button dynamic_hook;
    private TextView info;
    StringBuffer stringBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_agent);
        try {
            HookHelper.onCreate(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        buymyself = (Button) findViewById(R.id.buymyself);
        buyproxy = (Button) findViewById(R.id.buyproxy);
        dynamic_buyproxy = (Button) findViewById(R.id.dynamic_buyproxy);
        dynamic_hook = (Button) findViewById(R.id.dynamic_hook);
        info = (TextView) findViewById(R.id.info);
        buymyself.setOnClickListener(this);
        buyproxy.setOnClickListener(this);
        dynamic_buyproxy.setOnClickListener(this);
        dynamic_hook.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buymyself:
                stringBuffer.append("------------------BuyMyself------------------\n");
                Shopping shopping = new ShopingImpl("myself");
                stringBuffer.append("myself buy things:" + Arrays.toString(shopping.doShoppoing(156)) + "\n");
                info.setText(stringBuffer.toString());
                break;
            case R.id.buyproxy:
                stringBuffer.append("------------------BuyProxy------------------\n");
                Shopping shoppingProxy = new ProxyShopping(new ShopingImpl("proxy"));
                stringBuffer.append("proxy buy things:" + Arrays.toString(shoppingProxy.doShoppoing(156)) + "\n");
                info.setText(stringBuffer.toString());
                break;
            case R.id.dynamic_buyproxy:
                stringBuffer.append("------------------DynamicBuyProxy------------------\n");
                Shopping myShopping = new ShopingImpl("dynamic proxy");
                myShopping = (Shopping) Proxy.newProxyInstance(Shopping.class.getClassLoader(), myShopping.getClass().getInterfaces(), new ProxyInvocationHandler(myShopping));
                stringBuffer.append("dynamic proxy buy things:" + Arrays.toString(myShopping.doShoppoing(188)) + "\n");
                info.setText(stringBuffer.toString());
                break;
            case R.id.dynamic_hook:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("http://www.baidu.com"));
                //getApplicationContext().startActivity(intent);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            HookHelper.activityThreadInstrumentation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ShopingImpl implements Shopping {
        private String role;

        public ShopingImpl(String role) {
            this.role = role;
        }

        @Override
        public Object[] doShoppoing(long money) {
            stringBuffer.append(role + " walk...\n");
            stringBuffer.append(role + " expense " + money + " yuan\n");
            return new Object[]{"shoes", "clothes", "trousers"};
        }
    }

    public class ProxyShopping implements Shopping {
        Shopping shopping;

        public ProxyShopping(Shopping shopping) {
            this.shopping = shopping;
        }

        @Override
        public Object[] doShoppoing(long money) {
            stringBuffer.append("myself expense " + money + " yuan\n");
            long readCost = (long) (money * 0.5);
            Object[] objects = shopping.doShoppoing(readCost);
            stringBuffer.append("proxy buy things:" + Arrays.toString(objects) + "\n");
            if (objects != null && objects.length > 1) {
                objects[0] = "badShoes";
                stringBuffer.append("proxy change one thine\n");
            }
            return objects;
        }
    }

    private class ProxyInvocationHandler implements InvocationHandler {
        private Shopping shopping;

        public ProxyInvocationHandler(Shopping shopping) {
            this.shopping = shopping;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if ("doShoppoing".equals(method.getName())) {
                long money = (long) objects[0];
                stringBuffer.append("myself expense " + money + " yuan\n");
                long readCost = (long) (money * 0.5);
                Object[] things = (Object[]) method.invoke(shopping, readCost);
                stringBuffer.append("dynamic proxy buy things:" + Arrays.toString(things) + "\n");
                if (things != null && things.length > 1) {
                    things[0] = "badShoes";
                    stringBuffer.append("dynamic proxy change one thine\n");
                }
                return things;
            }
            return null;
        }
    }
}
