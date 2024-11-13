package com.backend.Netflix.exceptions;

import java.io.Serializable;
import java.util.UUID;

public class DBInsertException extends RuntimeException {
    public DBInsertException(UUID id){
        super("Failed to insert media with id " + id + "to the database");
    }

    public DBInsertException(String title){
        super("Failed to insert media with title " + title + "to the database");
    }
}
