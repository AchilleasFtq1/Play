// VideoAdapter.java
package com.api.play;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.api.play.api.VideoDto;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<VideoDto> videoList;
    private Context context;
    private OnItemClickListener listener;

    // Listener interface to handle item clicks
    public interface OnItemClickListener {
        void onItemClick(VideoDto video);
    }

    public VideoAdapter(Context context, List<VideoDto> videoList, OnItemClickListener listener) {
        this.context = context;
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoDto video = videoList.get(position);
        holder.videoName.setText(video.getFileName());
        holder.videoDate.setText(video.getUploadDate());
        holder.videoThumbnail.setImageResource(R.drawable.logo); // Placeholder image

        // Set click listener to handle item click
        holder.itemView.setOnClickListener(v -> listener.onItemClick(video));
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView videoName, videoDate;
        ImageView videoThumbnail;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.videoName);
            videoDate = itemView.findViewById(R.id.videoDate);
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
        }
    }
}
