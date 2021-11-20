package com.mongo.api.core.exceptions.globalException;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
// ========================== PropertySource + ConfigurationProperties =============================
//Check - PropertySource: https://www.baeldung.com/configuration-properties-in-spring-boot
// Setter are CRUCIAL for PropertySource + ConfigurationProperties works properly
@Setter
@PropertySource(value = "classpath:exceptions-management.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "global.exception")
// ========================== PropertySource + ConfigurationProperties =============================
@Component
@Getter
public class GlobalExceptionProperties {

    private String developerAttribute;
    private String developerMessage;
    private String globalAttribute;
    private String globalMessage;

}