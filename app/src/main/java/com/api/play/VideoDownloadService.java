package com.api.play;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

import com.api.play.api.VideoApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoDownloadService extends Service {

    public static final String EXTRA_VIDEO_ID = "video_id";
    public static final String EXTRA_BASE_URL = "base_url";
    private static final String TAG = "VideoDownloadService";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String videoId = intent.getStringExtra(EXTRA_VIDEO_ID);
        String baseUrl = intent.getStringExtra(EXTRA_BASE_URL);

        if (videoId != null && baseUrl != null) {
            downloadVideoWithResume(videoId, baseUrl);
        } else {
            Log.e(TAG, "No video ID or base URL provided!");
        }
        return START_NOT_STICKY;
    }

    private void downloadVideoWithResume(String videoId, String baseUrl) {
        int attempts = 0;
        boolean downloadSuccessful = false;

        while (attempts < MAX_RETRY_ATTEMPTS && !downloadSuccessful) {
            attempts++;
            downloadSuccessful = attemptDownload(videoId, baseUrl);

            if (!downloadSuccessful) {
                Log.w(TAG, "Download failed, attempt " + attempts + " of " + MAX_RETRY_ATTEMPTS);
            }
        }

        if (!downloadSuccessful) {
            Log.e(TAG, "Failed to download video after " + MAX_RETRY_ATTEMPTS + " attempts.");
        }
    }

    private boolean attemptDownload(String videoId, String baseUrl) {
        File videoFile = new File(getFilesDir(), "video_" + videoId + ".mp4");
        long downloadedLength = videoFile.exists() ? videoFile.length() : 0;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VideoApiService apiService = retrofit.create(VideoApiService.class);
        Call<ResponseBody> call = apiService.downloadVideoWithRange(videoId, "bytes=" + downloadedLength + "-");

        try {
            retrofit2.Response<ResponseBody> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                saveVideoToFile(response.body(), videoFile, downloadedLength);
                return true;
            } else {
                Log.e(TAG, "Failed to download video with response code: " + response.code());
            }
        } catch (Exception e) {
            Log.e(TAG, "Download error: " + e.getMessage());
        }
        return false;
    }

    private void saveVideoToFile(ResponseBody body, File videoFile, long offset) {
        try (RandomAccessFile outputFile = new RandomAccessFile(videoFile, "rw");
             InputStream inputStream = body.byteStream()) {

            outputFile.seek(offset);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputFile.write(buffer, 0, bytesRead);
            }
            Log.i(TAG, "Video downloaded and saved as " + videoFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error saving video: " + e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
