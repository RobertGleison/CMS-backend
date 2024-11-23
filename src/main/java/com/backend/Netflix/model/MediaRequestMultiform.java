package com.backend.Netflix.model;

import org.springframework.web.multipart.MultipartFile;

public record MediaRequestMultiform(
        MultipartFile videoFile,
        MultipartFile thumbnail,
        String title,
        String description,
        String genre,
        Integer year,
        String publisher,
        Integer duration
) {}
