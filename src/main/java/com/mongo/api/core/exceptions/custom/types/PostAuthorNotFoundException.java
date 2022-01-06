package com.mongo.api.core.exceptions.custom.types;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostAuthorNotFoundException extends RuntimeException implements Serializable{

    private static final long serialVersionUID = 1L;

    public PostAuthorNotFoundException(String message)
    {
        super(message);
    }
}