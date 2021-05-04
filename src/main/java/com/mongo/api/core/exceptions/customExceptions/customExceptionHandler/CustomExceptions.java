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
@Getter
@Setter
@Component
@NoArgsConstructor
@PropertySource(value = "classpath:exceptions-messages.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "exception.message")
public class CustomExceptions {

    private String userNotFound;
    private String postNotFound;
    private String commentNotFound;
    private String authorNotFound;


    public <T> Mono<T> userNotFoundException() {
        return Mono.error(new UserNotFoundException(userNotFound));
    }


    public <T> Mono<T> postAuthorNotFoundException() {
        return Mono.error(
                new PostAuthorNotFoundException("Author not Found-class"));
    }


    public <T> Mono<T> postNotFoundException() {
        return Mono.error(new PostNotFoundException("Post not Found-class"));
    }


    public <T> Mono<T> commentNotFoundException() {
        return Mono.error(new CommentNotFoundException("Comment not Found-class"));
    }


    public <T> Mono<T> globalErrorException() {
        return Mono.error(
                new ResponseStatusException(NOT_FOUND,
                                            "Global-Exception: Triggered-class"
                ));
    }
}



