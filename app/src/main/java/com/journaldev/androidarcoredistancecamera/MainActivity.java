package com.journaldev.androidarcoredistancecamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.PixelCopy;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {


    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String TAG = MainActivity.class.getSimpleName();

    /*Views*/
    private ArSceneView arSceneView;
    private TextView tvDistance;
    private Button btnRecord;

    /*PrivateMembers*/
    private ArFragment arFragment;
    ModelRenderable cubeRenderable;
    private AnchorNode firstAnchorNode;
    private AnchorNode secondAnchorNode;
    private ByteArrayOutputStream mCapturedImageByteArrayStrem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);
        /*Permissions*/
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        /*Init*/
        Initialize();
        getView();
        setListeners();
    }

    private boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            toastMsg("Sceneform requires OpenGL ES 3.0 or later");
            activity.finish();
            return false;
        }
        return true;
    }

    private void Initialize() {
        MaterialFactory.makeTransparentWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            float rad = (float) 0.005;
                            cubeRenderable = ShapeFactory.makeSphere(rad, Vector3.zero(), material);
                            cubeRenderable.setShadowCaster(false);
                            cubeRenderable.setShadowReceiver(false);
                        });
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    }

    private void getView() {
        arSceneView = arFragment.getArSceneView();
        tvDistance = findViewById(R.id.tvDistance);
        btnRecord = findViewById(R.id.btnRecord);
    }

    private void setListeners() {
        arSceneView.getScene().addOnUpdateListener(this);

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (cubeRenderable == null)
                return;

            if (firstAnchorNode == null) {
                firstAnchorNode = CreateAnchorNode(hitResult);
            }
            else if (secondAnchorNode == null) {
                secondAnchorNode = CreateAnchorNode(hitResult);
            }
            else {
                clearAnchor();
            }
        });

        btnRecord.setOnClickListener(v->{
            String path = generateFilename();
            try {
                takeScreenshot(path);
                SystemClock.sleep(1000);
            } catch (IOException | NotYetAvailableException e) {
                e.printStackTrace();
                toastMsg("saved image failed");
            }

            toastMsg("saved image successfully");
            Intent intent = new Intent(this, PreViewActivity.class);
            byte[] byteArray = mCapturedImageByteArrayStrem.toByteArray();
            intent.putExtra("image",byteArray);
            startActivity(intent);
        });

    }
    private AnchorNode CreateAnchorNode(HitResult hitResult) {
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arSceneView.getScene());

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(cubeRenderable);
        node.setParent(anchorNode);
        arSceneView.getScene().addChild(anchorNode);
        node.select();
        return anchorNode;
    }

    private void clearAnchor() {
        arFragment.getArSceneView().getScene().removeChild(firstAnchorNode);
        firstAnchorNode.getAnchor().detach();
        firstAnchorNode.setParent(null);
        firstAnchorNode = null;

        arFragment.getArSceneView().getScene().removeChild(secondAnchorNode);
        secondAnchorNode.getAnchor().detach();
        secondAnchorNode.setParent(null);
        secondAnchorNode = null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUpdate(FrameTime frameTime) {
        if (IsDistanceMeasured()) {
            Pose firstPose = firstAnchorNode.getAnchor().getPose();
            Pose secondPose = secondAnchorNode.getAnchor().getPose();

            float dx = firstPose.tx() - secondPose.tx();
            float dy = firstPose.ty() - secondPose.ty();
            float dz = firstPose.tz() - secondPose.tz();

            ///Compute the straight-line distance.
            float distanceCm = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 100;
            float convertCm = (float) (Math.round(distanceCm * 100) / 100.0);
            tvDistance.setText("Length Between Two Points : " + convertCm + " cm");
        }
    }

    private boolean IsDistanceMeasured() {
        return firstAnchorNode != null && secondAnchorNode != null;
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/");
        if (!directory.exists()) {
            boolean retVal = directory.mkdirs();
            Log.d("generateFileName", "directory... " + (retVal));
        }

        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    private void takeScreenshot(String path) throws IOException, NotYetAvailableException {
        Bitmap captureBitmap = Bitmap.createBitmap(arSceneView.getWidth(), arSceneView.getHeight(), Bitmap.Config.ARGB_8888);
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        PixelCopy.request(arSceneView, captureBitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int copyResult) {
                if (copyResult == PixelCopy.SUCCESS && captureBitmap != null) {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(path);
                        mCapturedImageByteArrayStrem = new ByteArrayOutputStream();
                        captureBitmap.compress(Bitmap.CompressFormat.JPEG, 90, mCapturedImageByteArrayStrem);
                        mCapturedImageByteArrayStrem.writeTo(outputStream);
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                handlerThread.quitSafely();
            }
        }, new Handler(handlerThread.getLooper()));
    }

    private void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
