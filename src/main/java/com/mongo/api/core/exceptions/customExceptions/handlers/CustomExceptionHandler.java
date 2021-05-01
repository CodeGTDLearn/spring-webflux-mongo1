package com.mongo.api.core.exceptions.customExceptions.handlers;

import com.mongo.api.core.exceptions.customExceptions.simple.CommentNotFoundException;
import com.mongo.api.core.exceptions.customExceptions.simple.PostAuthorNotFoundException;
import com.mongo.api.core.exceptions.customExceptions.simple.PostNotFoundException;
import com.mongo.api.core.exceptions.customExceptions.simple.UserNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice(annotations = {RestController.class})
@AllArgsConstructor
public class CustomExceptionHandler {

    private final Environment env;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> CustomException(UserNotFoundException exception) {
        CustomExceptionDetails customExceptionDetails =
                new CustomExceptionDetails(
                        env.getProperty("title.UserNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionDetails,NOT_FOUND);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<?> CustomException(PostNotFoundException exception) {
        CustomExceptionDetails customExceptionDetails =
                new CustomExceptionDetails(
                        env.getProperty("title.PostNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionDetails,NOT_FOUND);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<?> CustomException(CommentNotFoundException exception) {
        CustomExceptionDetails customExceptionDetails =
                new CustomExceptionDetails(
                        env.getProperty("title.CommentNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionDetails,NOT_FOUND);
    }

    @ExceptionHandler(PostAuthorNotFoundException.class)
    public ResponseEntity<?> CustomException(PostAuthorNotFoundException exception) {
        CustomExceptionDetails customExceptionDetails =
                new CustomExceptionDetails(
                        env.getProperty("title.AuthorNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionDetails,NOT_FOUND);
    }
}

