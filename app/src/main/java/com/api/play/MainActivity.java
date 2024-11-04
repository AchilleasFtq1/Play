package com.api.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView videoPlayerView;
    private View logoImageView;
    private BroadcastReceiver playbackReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        videoPlayerView = findViewById(R.id.videoPlayerView);
        logoImageView = findViewById(R.id.logoImageView);

        initializePlayer();
        startBackgroundHumanDetection();

        // Register broadcast receiver for playback control based on human detection
        playbackReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.api.play.START_VIDEO_PLAYBACK".equals(intent.getAction())) {
                    startPlayback();
                } else if ("com.api.play.STOP_VIDEO_PLAYBACK".equals(intent.getAction())) {
                    stopPlayback();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.api.play.START_VIDEO_PLAYBACK");
        filter.addAction("com.api.play.STOP_VIDEO_PLAYBACK");
        registerReceiver(playbackReceiver, filter);
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this).build();
        videoPlayerView.setPlayer(player);

        loadDownloadedVideosInOrder();
    }

    private void loadDownloadedVideosInOrder() {
        List<File> downloadedVideos = getDownloadedVideos();
        for (File video : downloadedVideos) {
            MediaItem mediaItem = MediaItem.fromUri(video.getAbsolutePath());
            player.addMediaItem(mediaItem);
        }
        player.prepare();
    }

    private List<File> getDownloadedVideos() {
        List<File> videos = new ArrayList<>();
        File dir = getFilesDir();
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".mp4")) {
                videos.add(file);
            }
        }
        return videos;
    }

    /**
     * Starts the human detection service in the background.
     */
    private void startBackgroundHumanDetection() {
        Intent detectionIntent = new Intent(this, HumanDetectionService.class);
        startService(detectionIntent);
    }

    public void startPlayback() {
        logoImageView.setVisibility(View.GONE);
        videoPlayerView.setVisibility(View.VISIBLE);
        if (player != null && !player.isPlaying()) {
            player.play();
        }
    }

    public void stopPlayback() {
        logoImageView.setVisibility(View.VISIBLE);
        videoPlayerView.setVisibility(View.GONE);
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(playbackReceiver);
        if (player != null) {
            player.release();
        }
    }
}
