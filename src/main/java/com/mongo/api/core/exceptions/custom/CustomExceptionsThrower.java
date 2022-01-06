package com.mongo.api.core.exceptions.custom;

import com.mongo.api.core.exceptions.custom.types.*;
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
public class CustomExceptionsThrower {

    private CustomExceptionsCustomAttributes customExceptionsMessages;


    public <T> Mono<T> userNotFoundException() {
        return Mono.error(new UserNotFoundException(
             customExceptionsMessages.getUserNotFoundMessage()));
    }

    public <T> Mono<T> usersNotFoundException() {
        return Mono.error(new UsersNotFoundException(
             customExceptionsMessages.getUsersNotFoundMessage()));
    }


    public <T> Mono<T> postNotFoundException() {
        return Mono.error(new PostNotFoundException(
             customExceptionsMessages.getPostNotFoundMessage()));
    }


    public <T> Mono<T> authorNotFoundException() {
        return Mono.error(
                new PostAuthorNotFoundException(
                     customExceptionsMessages.getAuthorNotFoundMessage()));
    }


    public <T> Mono<T> commentNotFoundException() {
        return Mono.error(new CommentNotFoundException(
             customExceptionsMessages.getCommentNotFoundMessage()));
    }

}