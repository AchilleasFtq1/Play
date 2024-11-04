package com.api.play;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.graphics.ImageFormat;
import android.media.Image;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.nio.ByteBuffer;
public class HumanDetectionService extends Service {

    private static final String TAG = "HumanDetectionService";
    private static final String CHANNEL_ID = "HumanDetectionServiceChannel";

    private CascadeClassifier faceDetector;
    private boolean isHumanDetected = false;
    private ExecutorService cameraExecutor;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService();

        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed");
        } else {
            initializeOpenCV();
        }

        startCamera();
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Human Detection Service")
                .setContentText("Detecting human presence...")
                .setSmallIcon(R.drawable.logo)
                .build();
        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Human Detection Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void initializeOpenCV() {
        try {
            InputStream is = getResources().openRawResource(R.raw.a);
            File cascadeFile = new File(getFilesDir(), "a.xml");
            FileOutputStream os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (faceDetector.empty()) {
                faceDetector = null;
                Log.e(TAG, "Failed to load cascade classifier");
            } else {
                Log.d(TAG, "Cascade classifier loaded successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cascade classifier: " + e.getMessage());
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                cameraProvider.bindToLifecycle((LifecycleOwner) this, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Error binding camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeFrame(ImageProxy image) {
        Mat frame = convertImageProxyToMat(image);

        boolean humanDetected = detectHuman(frame);
        if (humanDetected && !isHumanDetected) {
            isHumanDetected = true;
            sendStartPlaybackBroadcast();
        } else if (!humanDetected && isHumanDetected) {
            isHumanDetected = false;
            sendStopPlaybackBroadcast();
        }

        image.close();
    }


    @OptIn(markerClass = ExperimentalGetImage.class)
    private Mat convertImageProxyToMat(ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image == null || image.getFormat() != ImageFormat.YUV_420_888) {
            return null;
        }

        // Get the Y buffer (luminance component)
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();

        // Create an empty Mat in the required size
        Mat mat = new Mat(height, width, CvType.CV_8UC1);
        byte[] yBytes = new byte[yBuffer.remaining()];
        yBuffer.get(yBytes);
        mat.put(0, 0, yBytes);

        // Resize the mat if needed and convert it to grayscale (optional, depending on your use case)
        Mat grayMat = new Mat();
        Imgproc.resize(mat, grayMat, new Size(width, height));

        return grayMat;
    }

    private boolean detectHuman(Mat frame) {
        // Ensure faceDetector is properly initialized
        if (faceDetector == null) {
            Log.e(TAG, "CascadeClassifier not initialized.");
            return false;
        }

        // Store detected faces in MatOfRect
        MatOfRect faces = new MatOfRect();
        faceDetector.detectMultiScale(frame, faces);

        // Convert to array and check if any faces were detected
        Rect[] facesArray = faces.toArray();
        return facesArray.length > 0;
    }

    private void sendStartPlaybackBroadcast() {
        Intent intent = new Intent("com.api.play.START_VIDEO_PLAYBACK");
        sendBroadcast(intent);
    }

    private void sendStopPlaybackBroadcast() {
        Intent intent = new Intent("com.api.play.STOP_VIDEO_PLAYBACK");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
