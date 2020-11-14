package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class TestActivity extends Activity {
    private Button btnGoBack;
    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btnGoBack = findViewById(R.id.btnGoBack);
        btnGoBack.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}