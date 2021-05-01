package com.mongo.api.core.exceptions.customExceptions.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

//@PropertySource("classpath:exception-messages.properties")
public class Messages {

    @Autowired
    static Environment env;

    public static <T> Mono<T> userNotFoundException() {
        return Mono.error(
                new UserNotFoundException(env.getProperty("simple.UserNotFoundException")));
//                new UserNotFoundException("SimpleExc: (Service) User not Found"));
    }

    public static <T> Mono<T> postAuthorNotFoundException() {
        return Mono.error(
                new PostAuthorNotFoundException("SimpleExc: (Service) Author not Found"));
    }

    public static <T> Mono<T> postNotFoundException() {
        return Mono.error(
                new PostNotFoundException("SimpleExc: (Service) Post not Found"));
    }

    public static <T> Mono<T> commentNotFoundException() {
        return Mono.error(
                new CommentNotFoundException("SimpleExc: (Service) Comment not Found"));
    }

    public static <T> Mono<T> genericExcErrorUserNotFound() {
        return Mono.error(
                new ResponseStatusException(NOT_FOUND,
                                            "Generic Except: findErrorUserNotFound"));
    }
}



