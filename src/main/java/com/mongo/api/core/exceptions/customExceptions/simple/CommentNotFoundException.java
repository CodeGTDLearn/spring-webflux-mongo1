package com.mongo.api.core.exceptions.customExceptions.simple;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommentNotFoundException extends RuntimeException implements Serializable{

    private static final long serialVersionUID = 1L;

    public CommentNotFoundException(String message)
    {
        super(message);
    }
}
