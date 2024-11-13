package com.backend.Netflix.model;

public record MediaResponseDTO(
    String title,
    String description,
    String genre,
    Integer year,
    String publisher,
    Integer duration
) {}

