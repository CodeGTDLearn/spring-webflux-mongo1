package com.mongo.api.core.exceptions.customExceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

// getters + setter are necessary, in order to use @ConfigurationProperties
@Component
@Getter
@Setter
@PropertySource(value = "classpath:exceptions-management.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "custom.exception")
public class CustomExceptionsProperties {

    private String userNotFoundMessage;
    private String postNotFoundMessage;
    private String authorNotFoundMessage;
    private String commentNotFoundMessage;

}



