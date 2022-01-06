package com.mongo.api.core.exceptions.custom;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.validation.constraints.*;

// ================ PropertySource + ConfigurationProperties + FILES.PROPERTIES ====================
//     Check - PropertySource: https://www.baeldung.com/configuration-properties-in-spring-boot
//        https://www.appsdeveloperblog.com/spring-boot-configurationproperties-tutorial/
//       Getter+Setter are CRUCIAL for PropertySource + ConfigurationProperties works properly
@Component
@Getter
@Setter
@PropertySource(value = "classpath:exceptions-custom-attributes.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "custom.exception")
public class CustomExceptionsCustomAttributes {

    // THE BEAN-VALIDATION IS VALIDATING THE MESSAGE-CONTENT
    // THAT COMES FROM THE EXCEPTIONS-MANAGEMENT.PROPERTIES FILE
    // THOSE VALIDATIONS NOT HAVE RELATION WITH THE EXCEPTIONS
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