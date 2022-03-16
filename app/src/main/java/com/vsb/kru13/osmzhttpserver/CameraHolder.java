package com.vsb.kru13.osmzhttpserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

public class CameraHolder {

    public Camera.PictureCallback mPicture;
    public byte[] picData;
    private static CameraHolder instance = null;

    /**
     * Constructor
     */
    CameraHolder () {
        this.picData = new byte[4096];
        this.mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("MJPEG-CALLBACK", "Taking picture");
                picData = data;
                camera.startPreview();
            }
        };

    }

    /**
     * Get CameraHolder instance
     *
     * @return
     */
    public static CameraHolder getInstance() {
        if (instance == null)
            instance = new CameraHolder();

        return instance;
    }

    /**
     * Get current picture data
     *
     * @return
     */
    public byte[] getPicData() {
        return picData;
    }

    /** Check if this device has a camera */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
