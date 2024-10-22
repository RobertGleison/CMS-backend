package com.backend.Netflix.model;

import java.util.List;

public class VideoMetadata {
    private String title;
    private String description;
    private List<String> genre;
    private String year;
    private String creator;
    private String runtime;
    private String thumbnailUrl;
    private String videoUrl;

    public VideoMetadata(String title, String description, List<String> genre, String year, String creator, String runtime, String thumbnailUrl, String videoUrl) {
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.year = year;
        this.creator = creator;
        this.runtime = runtime;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
    }

    public VideoMetadata(){
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}