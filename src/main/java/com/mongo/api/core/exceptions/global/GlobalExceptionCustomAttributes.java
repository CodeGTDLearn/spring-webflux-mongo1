package com.mongo.api.core.exceptions.global;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/*
    ╔═══════════════════════════════════════════════════════════╗
    ║              GLOBAL-EXCEPTIONS EXPLANATIONS               ║
    ╠═══════════════════════════════════════════════════════════╣
    ║             There is no Thrower in Global-Exceptions      ║
    ║             Because Global-Exceptions are throwed         ║
    ║                  for "the system by itself",              ║
    ║           not programmatically in a specific method       ║
    ║(meaning threw inside a method according the coder defined)║
    ╚═══════════════════════════════════════════════════════════╝
*/
// ================ PropertySource + ConfigurationProperties + FILES.PROPERTIES ====================
//     Check - PropertySource: https://www.baeldung.com/configuration-properties-in-spring-boot
//        https://www.appsdeveloperblog.com/spring-boot-configurationproperties-tutorial/
//       Getter+Setter are CRUCIAL for PropertySource + ConfigurationProperties works properly
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "global.exception")
@PropertySource(value = "classpath:exceptions-custom-attributes.properties", ignoreResourceNotFound = true)
public class GlobalExceptionCustomAttributes {

    // THE BEAN-VALIDATION IS VALIDATING THE MESSAGE-CONTENT
    // THAT COMES FROM THE EXCEPTIONS-MANAGEMENT.PROPERTIES FILE
    // THOSE VALIDATIONS NOT HAVE RELATION WITH THE EXCEPTIONS
    private String developerAttribute;
    private String developerMessage;
    private String globalAttribute;
    private String globalMessage;

}