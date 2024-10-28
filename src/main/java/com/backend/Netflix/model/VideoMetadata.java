package com.backend.Netflix.model;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VideoMetadata {
    private String title;
    private String description;
    private String genre;
//    private String year;
    private String publisher;
    private String runtime;


}