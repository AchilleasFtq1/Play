// VideoApiService.java
package com.api.play.api;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VideoApiService {
    @GET("videos/{videoId}/download") // Adjust the endpoint as per your microservice
    Call<ResponseBody> downloadVideo(@Path("videoId") String videoId);

    // Endpoint to list videos with pagination
    @GET("videos/list")
    Call<List<VideoDto>> listVideos(@Query("page") int page, @Query("size") int size);

}
