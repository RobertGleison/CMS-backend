package com.backend.Netflix.exceptions;

public class DatabaseAccessException extends RuntimeException{
    public DatabaseAccessException(String msg){
        super(msg);
    }
}
