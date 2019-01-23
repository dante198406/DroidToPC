package com.erobbing.adb_config_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.erobbing.adb_config_demo.sdk.service.SdkService;

public class MainActivity extends Activity {

    Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        intent = new Intent(this, SdkService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        //stopService(intent);
        super.onDestroy();

    }
}
