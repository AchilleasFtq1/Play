// VideoRepository.java
package com.api.play;

import android.util.Log;
import com.api.play.api.VideoApiService;
import com.api.play.api.VideoDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoRepository {
    private static final String TAG = "VideoRepository";
    private static final String BASE_URL = "http://localhost:8080/api/";
    private final VideoApiService apiService;

    public VideoRepository() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(VideoApiService.class);
    }

    // Fetch a paginated list of videos
    public void fetchVideoList(int page, int size, VideoListCallback callback) {
        apiService.listVideos(page, size).enqueue(new Callback<List<VideoDto>>() {
            @Override
            public void onResponse(Call<List<VideoDto>> call, Response<List<VideoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch videos");
                }
            }

            @Override
            public void onFailure(Call<List<VideoDto>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    // Interface for callback handling
    public interface VideoListCallback {
        void onSuccess(List<VideoDto> videos);
        void onError(String error);
    }
}
