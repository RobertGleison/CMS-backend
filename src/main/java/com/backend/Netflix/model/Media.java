package com.backend.Netflix.model;
import lombok.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Media {
    private UUID id;
    private String title;
    private String description;
    private String genre;
    private Integer year;
    private String publisher;
    private Integer runtime;
    private String filename;
    private String bucketPath;
    private LocalDateTime upload_timestamp;
}