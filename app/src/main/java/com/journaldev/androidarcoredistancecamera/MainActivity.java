package com.journaldev.androidarcoredistancecamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Scene.OnUpdateListener {


    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String TAG = MainActivity.class.getSimpleName();

    private ArFragment arFragment;
    private AnchorNode firstAnchorNode;
    private TextView tvDistance;
    ModelRenderable cubeRenderable;
    private Anchor firstAnchor = null;
    private Anchor secondAnchor = null;
    private AnchorNode secondAnchorNode;
    private Image mCurrentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        tvDistance = findViewById(R.id.tvDistance);

        arFragment.getArSceneView().getScene().addOnUpdateListener((this::onSceneUpdate));

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);


        initModel();

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (cubeRenderable == null)
                return;

            if (firstAnchor == null){
                // Creating Anchor.
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                firstAnchor = anchor;
                firstAnchorNode = anchorNode;


                TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                node.setRenderable(cubeRenderable);
                node.setParent(anchorNode);
                arFragment.getArSceneView().getScene().addOnUpdateListener(this);
                arFragment.getArSceneView().getScene().addChild(anchorNode);
                node.select();
            }
            else if (secondAnchor == null){
                // Creating Anchor.
                Anchor anchor = hitResult.createAnchor();
                AnchorNode anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                secondAnchor = anchor;
                secondAnchorNode = anchorNode;


                TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
                node.setRenderable(cubeRenderable);
                node.setParent(anchorNode);
                arFragment.getArSceneView().getScene().addOnUpdateListener(this);
                arFragment.getArSceneView().getScene().addChild(anchorNode);
                node.select();

            }
            else {
                clearAnchor();

            }

        });


    }

    public boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        String openGlVersionString =
                ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    private void initModel() {
        MaterialFactory.makeTransparentWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            Vector3 vector3 = new Vector3(0.05f, 0.01f, 0.01f);
                            cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material);
                            cubeRenderable.setShadowCaster(false);
                            cubeRenderable.setShadowReceiver(false);
                        });
    }

    private void clearAnchor() {
        firstAnchor = null;
        secondAnchor = null;


        if (firstAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(firstAnchorNode);
            firstAnchorNode.getAnchor().detach();
            firstAnchorNode.setParent(null);
            firstAnchorNode = null;
        }

        if (secondAnchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(secondAnchorNode);
            secondAnchorNode.getAnchor().detach();
            secondAnchorNode.setParent(null);
            secondAnchorNode = null;
        }
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

       // Log.d("API123", "onUpdateframe... current anchor node " + (firstAnchorNode == null));


        if (firstAnchorNode != null && secondAnchorNode != null) {
            Pose firstPose = firstAnchor.getPose();
            Pose secondPose = secondAnchor.getPose();

            float dx = firstPose.tx() - secondPose.tx();
            float dy = firstPose.ty() - secondPose.ty();
            float dz = firstPose.tz() - secondPose.tz();

            ///Compute the straight-line distance.
            float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            float convertCm = distanceMeters * 100;
//            convertCm = Math.round((convertCm * 100000) / 100000.0);
            tvDistance.setText("Length Between Two Points : " + convertCm + " cm");

        }
    }

    public void onSavePicture(View view) {
        //flag set here
        //override render interface
        //onSurfaceChanged, onDrawFrame
        //need also saving bitmap logic
        //https://stackoverflow.com/questions/48191513/how-to-take-picture-with-camera-using-arcore
        float ab = 1;

        try {
            WriteImageInformation(mCurrentImage, generateFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        toastMsg("saved image successfully");

    }

    private void onSceneUpdate(FrameTime frameTime) {
        Frame currentFrame = arFragment.getArSceneView().getArFrame();
        if (mCurrentImage != null) {
            mCurrentImage.close();
            mCurrentImage = null;
        }
        try {
            mCurrentImage = currentFrame.acquireCameraImage();
        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }
        if (mCurrentImage != null) {
            int imageFormat = mCurrentImage.getFormat();
            if (imageFormat == ImageFormat.YUV_420_888) {
                Log.d("ImageFormat", "Image format is YUV_420_888");
            }
        }
    }

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        return out.toByteArray();
    }

    public static void WriteImageInformation(Image image, String path) throws IOException {
        byte[] data = null;
        data = NV21toJPEG(YUV_420_888toNV21(image),
                image.getWidth(), image.getHeight());
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
        bos.write(data);
        bos.flush();
        bos.close();
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

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

}
