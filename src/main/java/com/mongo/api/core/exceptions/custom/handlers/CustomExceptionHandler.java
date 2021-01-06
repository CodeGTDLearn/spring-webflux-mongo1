package com.mongo.api.core.exceptions.custom.handlers;

import com.mongo.api.core.exceptions.custom.exceptions.AuthorNotFoundException;
import com.mongo.api.core.exceptions.custom.exceptions.CommentNotFoundException;
import com.mongo.api.core.exceptions.custom.exceptions.PostNotFoundException;
import com.mongo.api.core.exceptions.custom.exceptions.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@AllArgsConstructor
public class CustomExceptionHandler {

    private Environment env;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> CustomException(UserNotFoundException exception) {
        ExceptionDetails exceptionDetails =
                new ExceptionDetails(
                        env.getProperty("title.UserNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(exceptionDetails,NOT_FOUND);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<?> CustomException(PostNotFoundException exception) {
        ExceptionDetails exceptionDetails =
                new ExceptionDetails(
                        env.getProperty("title.PostNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(exceptionDetails,NOT_FOUND);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<?> CustomException(CommentNotFoundException exception) {
        ExceptionDetails exceptionDetails =
                new ExceptionDetails(
                        env.getProperty("title.CommentNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(exceptionDetails,NOT_FOUND);
    }

    @ExceptionHandler(AuthorNotFoundException.class)
    public ResponseEntity<?> CustomException(AuthorNotFoundException exception) {
        ExceptionDetails exceptionDetails =
                new ExceptionDetails(
                        env.getProperty("title.AuthorNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(exceptionDetails,NOT_FOUND);
    }
}

