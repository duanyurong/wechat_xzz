package com.xajd.wechat_01;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        try {
            Thread.sleep(3000);
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
