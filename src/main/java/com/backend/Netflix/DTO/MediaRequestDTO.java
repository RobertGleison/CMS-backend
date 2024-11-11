package com.backend.Netflix.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record MediaRequestDTO(
        UUID id,
        String title,
        String description,
        String genre,
        Integer year,
        String publisher,
        Integer runtime,
        String filename,
        String bucketPath,
        LocalDateTime upload_timestamp
) {
}

