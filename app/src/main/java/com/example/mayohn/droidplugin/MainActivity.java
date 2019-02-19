package com.example.mayohn.droidplugin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mayohn.droidplugin.dynamicagent.DynamicAgentActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button dynamic_agent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dynamic_agent = (Button) findViewById(R.id.dynamic_agent);
        dynamic_agent.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dynamic_agent:
                Intent intent = new Intent(MainActivity.this, DynamicAgentActivity.class);
                startActivity(intent);
                break;
        }
    }
}
