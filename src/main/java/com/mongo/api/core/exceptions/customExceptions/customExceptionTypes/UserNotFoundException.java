package com.mongo.api.core.exceptions.customExceptions.customExceptionTypes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException implements Serializable{
    
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message)
    {
        super(message);
    }
}
