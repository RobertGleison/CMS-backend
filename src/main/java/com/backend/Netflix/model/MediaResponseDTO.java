package com.backend.Netflix.model;
import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "media")
public class MediaResponseDTO {
    @PrimaryKey
    private UUID id;
    private String title;
    private String description;
    private String genre;
    private Integer year;
    private String publisher;
    private Integer duration;
    private String filename;
    private Map<String, String> bucketPaths;
    private LocalDateTime uploadTimestamp;
}