package com.backend.Netflix.exceptions;

import java.util.UUID;

public class MediaNotFoundException extends RuntimeException {

    private MediaNotFoundException(String message) {
        super(message);
    }

    private MediaNotFoundException(UUID id) {
        super("Media with id " + id + " not found.");
    }

    public static MediaNotFoundException byId(UUID id) {
        return new MediaNotFoundException(id);
    }

    public static MediaNotFoundException byTitle(String title) {
        return new MediaNotFoundException("Media with title " + title + " not found");
    }

    public static MediaNotFoundException byGenre(String genre) {
        return new MediaNotFoundException("Media with genre " + genre + " not found");
    }
}