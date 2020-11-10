package com.journaldev.androidarcoredistancecamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
            try {
                StoreImage(generateFilename());
                toastMsg("saved image successfully");
            } catch (IOException | NotYetAvailableException e) {
                e.printStackTrace();
                toastMsg("saved image failed");
            }
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
    private void StoreImage(String path) throws IOException, NotYetAvailableException {
        byte[] data;
        Image image = arSceneView.getArFrame().acquireCameraImage();
        data = NV21toJPEG(YUV_420_888toNV21(image),
                image.getWidth(), image.getHeight());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
        bos.write(data);
        bos.flush();
        bos.close();
    }

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        return out.toByteArray();
    }

    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
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

    private void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
