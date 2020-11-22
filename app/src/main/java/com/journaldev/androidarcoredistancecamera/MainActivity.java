package com.journaldev.androidarcoredistancecamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
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
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
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

public class MainActivity extends AppCompatActivity implements
        PixelCopy.OnPixelCopyFinishedListener {


    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final String TAG = MainActivity.class.getSimpleName();

    /*Views*/
    private ArSceneView m_arSceneView;
    private TextView m_tvDistance;
    private Button m_btnRecord;

    /*PrivateMembers*/
    private ArFragment m_arFragment;
    ModelRenderable m_cubeRenderable;
    private AnchorNode m_firstAnchorNode;
    private AnchorNode m_secondAnchorNode;
    private Bitmap m_capturedBitmap;
    private Node m_nodeForLine;
    private float m_fishSize = 0;
    private ProgressDialog m_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported",
                    Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);
        /*Permissions*/
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        /*Loading*/
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        /*Init*/
        initialize();
        getView();
        setListeners();
    }

    private boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) Objects.requireNonNull(
                        activity.getSystemService(Context.ACTIVITY_SERVICE)))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Util.toastMsg(this, "Sceneform requires OpenGL ES 3.0 or later");
            activity.finish();
            return false;
        }
        return true;
    }

    private void initialize() {
        MaterialFactory.makeTransparentWithColor(this,
                new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            float rad = (float) 0.005;
                            m_cubeRenderable = ShapeFactory.makeSphere(rad,
                                    Vector3.zero(), material);
                            m_cubeRenderable.setShadowCaster(false);
                            m_cubeRenderable.setShadowReceiver(false);
                        });
        m_arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        m_dialog = new ProgressDialog(this);
    }

    private void getView() {
        m_arSceneView = m_arFragment.getArSceneView();
        m_tvDistance = findViewById(R.id.tvDistance);
        m_btnRecord = findViewById(R.id.btnRecord);
        m_btnRecord.setEnabled(false);
    }

    private void setListeners() {
        m_arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            if (m_cubeRenderable == null)
                return;

            if (m_firstAnchorNode == null) {
                m_firstAnchorNode = createAnchorNode(hitResult);
            }
            else if (m_secondAnchorNode == null) {
                m_secondAnchorNode = createAnchorNode(hitResult);
                onDistanceMeasured();
            }
            else {
                resetScene();
            }
        });

        m_btnRecord.setOnClickListener(v->{
            try {
                takeScreenshotAndMoveToPreview();
            } catch (NotYetAvailableException e) {
                e.printStackTrace();
                Util.toastMsg(this, "saved image failed");
            }
        });
    }
    private AnchorNode createAnchorNode(HitResult hitResult) {
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        m_arSceneView.getScene().addChild(anchorNode);

        TransformableNode node = new TransformableNode(m_arFragment.getTransformationSystem());
        anchorNode.addChild(node);
        node.setRenderable(m_cubeRenderable);
        node.select();
        return anchorNode;
    }

    private void resetScene() {
        m_btnRecord.setEnabled(false);
        m_tvDistance.setText("FishNote");
        clearAnchor();
    }

    private void clearAnchor() {
        m_arFragment.getArSceneView().getScene().removeChild(m_firstAnchorNode);
        m_firstAnchorNode.getAnchor().detach();
        m_firstAnchorNode.removeChild(m_nodeForLine);
        m_firstAnchorNode = null;

        m_arFragment.getArSceneView().getScene().removeChild(m_secondAnchorNode);
        m_secondAnchorNode.getAnchor().detach();
        m_secondAnchorNode = null;

        m_nodeForLine = null;
    }

    private void onDistanceMeasured() {
        m_btnRecord.setEnabled(true);

        Pose firstPose = m_firstAnchorNode.getAnchor().getPose();
        Pose secondPose = m_secondAnchorNode.getAnchor().getPose();

        float dx = firstPose.tx() - secondPose.tx();
        float dy = firstPose.ty() - secondPose.ty();
        float dz = firstPose.tz() - secondPose.tz();

        ///Compute the straight-line distance.
        float distanceCm = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 100;
        m_fishSize = (float) (Math.round(distanceCm * 100) / 100.0);

        m_tvDistance.setText("Length Between Two Points : " + m_fishSize + " cm");
        drawLine();
    }

    private String generateFilename() {
        String date = new SimpleDateFormat("yyyyMMddHHmmss",
                        java.util.Locale.getDefault()).format(new Date());

        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/");
        if (!directory.exists()) {
            boolean retVal = directory.mkdirs();
            Log.d("generateFileName", "directory... " + (retVal));
        }

        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" +
                date + "_screenshot.jpg";
    }

    private void takeScreenshotAndMoveToPreview() throws NotYetAvailableException {
        Util.showDialog(m_dialog, "Saving Image");
        m_capturedBitmap = Bitmap.createBitmap(m_arSceneView.getWidth(), m_arSceneView.getHeight(),
                Bitmap.Config.ARGB_8888);
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        PixelCopy.request(m_arSceneView, m_capturedBitmap, this,
                new Handler(handlerThread.getLooper()));
    }

    @Override
    public void onPixelCopyFinished(int copyResult) {
        if (copyResult == PixelCopy.SUCCESS) {
            String path = generateFilename();
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                ByteArrayOutputStream previewImageStream = new ByteArrayOutputStream();
                m_capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, previewImageStream);
                previewImageStream.writeTo(outputStream);
                outputStream.flush();
                outputStream.close();

                registerToGallery(path);
                m_dialog.dismiss();
                Util.toastMsg(this, "saved image successfully");

                //move to preview activity
                Intent intent = new Intent(this, PreViewActivity.class);
                byte[] byteArray = previewImageStream.toByteArray();
                intent.putExtra("image", byteArray);
                intent.putExtra("fish_size", m_fishSize);
                startActivity(intent);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void registerToGallery(String path) {
        MediaScannerConnection.scanFile(this,
                new String[] { path }, null,
                (path1, uri) -> Log.i("TAG", "Finished scanning " + path1));
    }

    private void drawLine() {
        Vector3 firstPoint, secondPoint;
        firstPoint = m_firstAnchorNode.getWorldPosition();
        secondPoint = m_secondAnchorNode.getWorldPosition();

        final Vector3 diffVec = Vector3.subtract(firstPoint, secondPoint);
        final Quaternion rotationFromAToB = Quaternion.lookRotation(diffVec, Vector3.up());
        //calculating vector rotation
        MaterialFactory.makeOpaqueWithColor(getApplicationContext(),
                new Color(android.graphics.Color.RED))
                .thenAccept(
                        material -> {
                            ModelRenderable model = ShapeFactory.makeCube(
                                    new Vector3(.005f, .005f, diffVec.length()),
                                    Vector3.zero(), material);
                            //length with diffVec
                            m_nodeForLine = new Node();
                            m_nodeForLine.setParent(m_firstAnchorNode);
                            m_nodeForLine.setRenderable(model);
                            m_nodeForLine.setWorldPosition(
                                    Vector3.add(firstPoint, secondPoint).scaled(.5f));
                            //set position to middle point
                            m_nodeForLine.setWorldRotation(rotationFromAToB);
                            //set rotation
                        }
                );
    }
}
