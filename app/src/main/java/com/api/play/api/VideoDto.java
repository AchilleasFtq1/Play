package com.api.play.api;


public class VideoDto {
    private String id;
    private String fileName;
    private String uploadDate;

    public VideoDto(String id, String fileName, String uploadDate) {
        this.id = id;
        this.fileName = fileName;
        this.uploadDate = uploadDate;
    }

    // Getters
    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public String getUploadDate() { return uploadDate; }
}
