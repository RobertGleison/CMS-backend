package com.backend.Netflix.model;

import lombok.Data;

import java.util.List;

@Data
public class VideoMetadata {
    private String title;
    private String description;
//    private List<String> genre;
    private String year;
    private String creator;
    private String runtime;
    private String thumbnailUrl;
    private String videoUrl;
}