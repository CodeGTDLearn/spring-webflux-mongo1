package com.mongo.api.core.exceptions.customExceptions;

import lombok.Getter;
import lombok.Setter;




import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;

// Properties Importation: https:
// - www.baeldung.com/properties-with-spring
// - https://www.appsdeveloperblog.com/spring-boot-configurationproperties-tutorial/
// getters + setter are necessary, in order to use @ConfigurationProperties
@Component
@Getter
@Setter
@PropertySource(value = "classpath:exceptions-management.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "custom.exception")
public class CustomExceptionsProperties {

    @Size(min = 1, max = 200, message = "About Me must be between 10 and 200 characters")
    private String userNotFoundMessage;

    @NotNull
    private String usersNotFoundMessage;

    @NotEmpty
    private String postNotFoundMessage;

    @NotBlank
    private String authorNotFoundMessage;

    @Min(value = 1, message = "At least one character")
    private String commentNotFoundMessage;

}