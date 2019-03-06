package com.example.mayohn.droidplugin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mayohn.droidplugin.ams.AMSHookActivity;
import com.example.mayohn.droidplugin.bindhook.BindHookActivity;
import com.example.mayohn.droidplugin.dynamicagent.DynamicAgentActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button dynamic_agent;
    private Button bind_hook;
    private Button ams_hook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dynamic_agent = (Button) findViewById(R.id.dynamic_agent);
        bind_hook = (Button) findViewById(R.id.bind_hook);
        ams_hook = (Button) findViewById(R.id.ams_hook);
        dynamic_agent.setOnClickListener(this);
        bind_hook.setOnClickListener(this);
        ams_hook.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dynamic_agent:
                Intent intent = new Intent(MainActivity.this, DynamicAgentActivity.class);
                startActivity(intent);
                break;
            case R.id.bind_hook:
                Intent intent1 = new Intent(MainActivity.this, BindHookActivity.class);
                startActivity(intent1);
                break;
            case R.id.ams_hook:
                Intent amsIntent = new Intent(MainActivity.this, AMSHookActivity.class);
                startActivity(amsIntent);
                break;
        }
    }
}
