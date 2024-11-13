package com.backend.Netflix.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StandardException implements Serializable {
    private String message;
    private Instant timestamp;
    private String error;
    private String path;
    private Integer statusCode;
}
