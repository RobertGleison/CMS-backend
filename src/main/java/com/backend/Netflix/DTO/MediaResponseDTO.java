package com.backend.Netflix.DTO;

public record MediaResponseDTO(
    String title,
    String description,
    String genre,
    Integer year,
    String publisher,
    Integer duration
) {}

