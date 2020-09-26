package com.journaldev.androidarcoredistancecamera;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
        }

        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        tvDistance = findViewById(R.id.tvDistance);


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

        Log.d("API123", "onUpdateframe... current anchor node " + (firstAnchorNode == null));


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



            /*float[] distance_vector = firstAnchor.getPose().inverse()
                    .compose(secondPose).getTranslation();
            float totalDistanceSquared = 0;
            for (int i = 0; i < 3; ++i)
                totalDistanceSquared += distance_vector[i] * distance_vector[i];*/
        }
    }

}
