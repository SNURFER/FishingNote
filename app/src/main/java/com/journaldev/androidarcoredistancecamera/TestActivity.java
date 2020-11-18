package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class TestActivity extends Activity {
    private Button m_btnGoBack;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        m_btnGoBack = findViewById(R.id.btnGoBack);

        /*Set listener*/
        m_btnGoBack.setOnClickListener(v->{
            Intent intent = new Intent(this, PreViewActivity.class);
            startActivity(intent);
            finish();
        });

     }
}