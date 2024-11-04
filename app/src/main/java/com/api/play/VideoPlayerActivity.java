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

public class VideoPlayerActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView videoPlayerView;
    private View logoImageView;
    private BroadcastReceiver playbackReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoPlayerView = findViewById(R.id.videoPlayerView);
        logoImageView = findViewById(R.id.logoImageView);
        initializePlayer();

        // Register broadcast receiver for playback control (assumes detection is managed elsewhere)
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

        loadDownloadedVideosInOrder(); // Load all downloaded videos in order
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

    private void startPlayback() {
        if (player != null && !player.isPlaying()) {
            // Hide the logo and start playing
            logoImageView.setVisibility(View.GONE);
            videoPlayerView.setVisibility(View.VISIBLE);
            player.play();
        }
    }

    private void stopPlayback() {
        if (player != null && player.isPlaying()) {
            player.pause();
            // Show the logo and hide the video player
            logoImageView.setVisibility(View.VISIBLE);
            videoPlayerView.setVisibility(View.GONE);
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
