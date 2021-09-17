package com.mongo.api.core.exceptions.customExceptions;

import com.mongo.api.core.exceptions.customExceptions.customExceptionTypes.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

// getters + setter are necessary, in order to use @ConfigurationProperties
@Component("customExceptions")
@Getter
@Setter
@AllArgsConstructor
public class CustomExceptions {

    private CustomExceptionsProperties customExceptionsProperties;


    public <T> Mono<T> userNotFoundException() {
        return Mono.error(new UserNotFoundException(
             customExceptionsProperties.getUserNotFoundMessage()));
    }

    public <T> Mono<T> usersNotFoundException() {
        return Mono.error(new UsersNotFoundException(
             customExceptionsProperties.getUsersNotFoundMessage()));
    }


    public <T> Mono<T> postNotFoundException() {
        return Mono.error(new PostNotFoundException(
             customExceptionsProperties.getPostNotFoundMessage()));
    }


    public <T> Mono<T> authorNotFoundException() {
        return Mono.error(
                new PostAuthorNotFoundException(
                     customExceptionsProperties.getAuthorNotFoundMessage()));
    }


    public <T> Mono<T> commentNotFoundException() {
        return Mono.error(new CommentNotFoundException(
             customExceptionsProperties.getCommentNotFoundMessage()));
    }

}



