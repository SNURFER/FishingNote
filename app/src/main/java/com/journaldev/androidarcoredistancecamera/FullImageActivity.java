package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class FullImageActivity extends Activity {
    private ImageView m_ivFullimage;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullimage);
        m_ivFullimage = findViewById(R.id.ivFullImage);
        Util.setImageView(getIntent().getByteArrayExtra("image"), m_ivFullimage);
     }
}