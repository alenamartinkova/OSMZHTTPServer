package com.vsb.kru13.osmzhttpserver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;

    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message message) {
            TextView textView = (TextView)findViewById(R.id.textView);

            textView.setText(
                    "Users count is " + message.getData().getInt("usersCount")
                    + ", available connections: " + message.getData().getInt("availablePermits")
            );
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        if (CameraHolder.checkCameraHardware(this)) {
            // Create an instance of Camera
            Camera camera = CameraHolder.getCameraInstance();

            // Create our Preview view and set it as the content of our activity.
            CameraPreview preview = new CameraPreview(this, camera);
            FrameLayout layout = (FrameLayout) findViewById(R.id.camera_preview);
            layout.addView(preview);
            camera.startPreview();

            // Add a listener to the Capture button
            Button btn3 = (Button) findViewById(R.id.button3);
            btn3.setOnClickListener(
                v -> {
                    // get an image from the camera
                    //camera.startPreview();
                    camera.takePicture(null, null, mPicture);
                }
            );
        } else {
            Log.d("CAM", "Camera perms missing.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button1) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {
                s = new SocketServer(this.handler, this);
                s.start();
            }
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    s = new SocketServer(this.handler, this);
                    s.start();
                }
                break;

            default:
                break;
        }
    }

    private Camera.PictureCallback mPicture = (data, camera) -> {
        Log.d("TAKING PIC", "Taking picture");
        File pictureFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera.jpg");

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("FNF", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("EAF", "Error accessing file: " + e.getMessage());
        }

        camera.startPreview();
    };
}
