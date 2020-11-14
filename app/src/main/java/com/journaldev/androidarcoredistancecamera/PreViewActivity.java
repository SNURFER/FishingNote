package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class PreViewActivity extends Activity {
    private Button mBtnTest;
    private Button mBtnGoBack;
    private ImageView mPreviewImage;

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getView();
        setListeners();

        Intent intent = getIntent();
        byte[] arr = getIntent().getByteArrayExtra("image");
        Bitmap image = BitmapFactory.decodeByteArray(arr, 0, arr.length);
        mPreviewImage.setImageBitmap(image);

    }

    private void setListeners() {
        mBtnTest.setOnClickListener(v->{
            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);
        });
        mBtnGoBack.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getView() {
        mBtnTest = findViewById((R.id.btnTest));
        mBtnGoBack = findViewById((R.id.btnGoBack));
        mPreviewImage = findViewById(R.id.imageView);
    }
}
