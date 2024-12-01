package com.backend.Netflix.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MediaRequestDTO {
    private MultipartFile videoFile;
    private MultipartFile thumbnail;
    private String title;
    private String description;
    private String genre;
    private Integer year;
    private String publisher;
    private Integer duration;}



