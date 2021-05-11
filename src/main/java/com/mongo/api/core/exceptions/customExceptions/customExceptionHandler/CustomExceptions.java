package com.mongo.api.core.exceptions.customExceptions.customExceptionHandler;

import com.mongo.api.core.exceptions.customExceptions.customExceptionTypes.CommentNotFoundException;
import com.mongo.api.core.exceptions.customExceptions.customExceptionTypes.PostAuthorNotFoundException;
import com.mongo.api.core.exceptions.customExceptions.customExceptionTypes.PostNotFoundException;
import com.mongo.api.core.exceptions.customExceptions.customExceptionTypes.UserNotFoundException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

// getters + setter are necessary, in order to use @ConfigurationProperties
@Component
@Getter
@Setter
@NoArgsConstructor
@PropertySource(value = "classpath:exceptions-management.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "custom.exception")
public class CustomExceptions {

    private String userNotFoundMessage;
    private String postNotFoundMessage;
    private String authorNotFoundMessage;
    private String commentNotFoundMessage;


    public <T> Mono<T> userNotFoundException() {
        return Mono.error(new UserNotFoundException(userNotFoundMessage));
    }


    public <T> Mono<T> postNotFoundException() {
        return Mono.error(new PostNotFoundException(postNotFoundMessage));
    }


    public <T> Mono<T> authorNotFoundException() {
        return Mono.error(
                new PostAuthorNotFoundException(authorNotFoundMessage));
    }


    public <T> Mono<T> commentNotFoundException() {
        return Mono.error(new CommentNotFoundException(commentNotFoundMessage));
    }


    public <T> Mono<T> globalErrorException() {
        return Mono.error(new ResponseStatusException(NOT_FOUND,"XXXX"));
    }
}



