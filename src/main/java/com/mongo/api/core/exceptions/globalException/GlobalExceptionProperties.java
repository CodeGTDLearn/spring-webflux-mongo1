package com.mongo.api.core.exceptions.globalException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@PropertySource(value = "classpath:exceptions-management.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "global.exception")
public class GlobalExceptionProperties {

    private String developerAttribute;
    private String developerMessage;
    private String globalAttribute;
    private String globalMessage;

}
