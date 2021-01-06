package com.mongo.api.core.exceptions;

import com.mongo.api.core.exceptions.custom.exceptions.AuthorNotFoundException;
import com.mongo.api.core.exceptions.custom.exceptions.CommentNotFoundException;
import com.mongo.api.core.exceptions.custom.exceptions.PostNotFoundException;
import com.mongo.api.core.exceptions.custom.exceptions.UserNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class ExceptionTriggers {
    public static <T> Mono<T> userNotFoundException() {
        return Mono.error(
                new UserNotFoundException("CustomExc: (Service) User not Found"));
    }

    public static <T> Mono<T> authorNotFoundException() {
        return Mono.error(
                new AuthorNotFoundException("CustomExc: (Service) Author not Found"));
    }

    public static <T> Mono<T> postNotFoundException() {
        return Mono.error(
                new PostNotFoundException("CustomExc: (Service) Post not Found"));
    }

    public static <T> Mono<T> commentNotFoundException() {
        return Mono.error(
                new CommentNotFoundException("CustomExc: (Service) Comment not Found"));
    }

    public static <T> Mono<T> genericExcErrorUserNotFound() {
        return Mono.error(
                new ResponseStatusException(NOT_FOUND,
                                            "Generic Except: findErrorUserNotFound"));
    }
}



