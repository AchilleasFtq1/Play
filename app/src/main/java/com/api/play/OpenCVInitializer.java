package com.api.play;

import org.opencv.android.OpenCVLoader;
import android.util.Log;

public class OpenCVInitializer {
    private static final String TAG = "OpenCVInitializer";

    public static void initialize() {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed!");
        } else {
            Log.d(TAG, "OpenCV initialized successfully!");
        }
    }
}
