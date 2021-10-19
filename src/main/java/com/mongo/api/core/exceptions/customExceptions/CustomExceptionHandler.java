package com.mongo.api.core.exceptions.customExceptions;

import com.mongo.api.core.exceptions.customExceptions.customExceptionTypes.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice(annotations = {RestController.class})
@AllArgsConstructor
public class CustomExceptionHandler {


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> CustomExceptionWithCustomAttributes(UserNotFoundException exception) {
        CustomExceptionAttributes customExceptionAttributes =
                new CustomExceptionAttributes(
                        //                        "User not Found",
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionAttributes,NOT_FOUND);
    }

    @ExceptionHandler(UsersNotFoundException.class)
    public ResponseEntity<?> CustomExceptionWithCustomAttributes(UsersNotFoundException exception) {
        CustomExceptionAttributes customExceptionAttributes =
             new CustomExceptionAttributes(
                  //                        "User not Found",
                  exception.getMessage(),
                  exception.getClass()
                           .getName(),
                  NOT_FOUND.value(),
                  new Date().getTime()
             );
        return new ResponseEntity<>(customExceptionAttributes,NOT_FOUND);
    }


    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<?> CustomExceptionWithCustomAttributes(PostNotFoundException exception) {
        CustomExceptionAttributes customExceptionAttributes =
                new CustomExceptionAttributes(
                        //                        env.getProperty("title.PostNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionAttributes,NOT_FOUND);
    }


    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<?> CustomExceptionWithCustomAttributes(CommentNotFoundException exception) {
        CustomExceptionAttributes customExceptionAttributes =
                new CustomExceptionAttributes(
                        //                        env.getProperty("title.CommentNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionAttributes,NOT_FOUND);
    }


    @ExceptionHandler(PostAuthorNotFoundException.class)
    public ResponseEntity<?> CustomExceptionWithCustomAttributes(PostAuthorNotFoundException exception) {
        CustomExceptionAttributes customExceptionAttributes =
                new CustomExceptionAttributes(
                        //                        env.getProperty("title.AuthorNotFoundException"),
                        exception.getMessage(),
                        exception.getClass()
                                 .getName(),
                        NOT_FOUND.value(),
                        new Date().getTime()
                );
        return new ResponseEntity<>(customExceptionAttributes,NOT_FOUND);
    }
}

