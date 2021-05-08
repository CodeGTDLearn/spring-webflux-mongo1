package com.mongo.api.core.exceptions.globalException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
@Getter
@Setter
@NoArgsConstructor
@PropertySource(value = "classpath:exceptions-management.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "global.exception")
public class GlobalExceptionAttributes extends DefaultErrorAttributes {

    private String developerAttribute;
    private String developerMessage;
    private String generalAttribute;


    @Override
    public Map<String, Object> getErrorAttributes(
            ServerRequest request,
            ErrorAttributeOptions options) {

        Map<String, Object> globalExceptionAttributes =
                super.getErrorAttributes(request,options);

        //ADICIONA A GLOBAL-EXCEPTION(ResponseStatusException)
        //POIS NAO SE TRATA DE NENHUMA DAS 'CUSTOM-EXCEPTIONS'
        Throwable throwable = getError(request);
        if (throwable instanceof ResponseStatusException) {

            ResponseStatusException errorFound = (ResponseStatusException) throwable;

            //DETECTADA UMA GLOBAL-EXCEPTION(ResponseStatusException)
            // adiciona ATTRIBUTES no globalExceptionAttributes
            //            globalExceptionAttributes.put("Global-AtribMessage",errorFound
            //            .getMessage());
            //            globalExceptionAttributes.put("Global-devAtribMsg","Generic Exception");
            globalExceptionAttributes.put(generalAttribute,errorFound.getMessage());
            globalExceptionAttributes.put(developerAttribute,developerMessage);
        }


        // NAO SENDO UMA GLOBAL-EXCEPTION(ResponseStatusException)
        // PORTANTO SENDO, UMA CUSTOM-EXCEPTION
        // retorna o valor PADRAO de ATTRIBUTES ou seja,
        // o globalExceptionAttributes "PURO", sem insercao(.put's do IF acima) de qquer atributo
        // personalizado
        // OU SEJA, nao se acrescenta os atributos definidos no IF-ACIMA
        return globalExceptionAttributes;
    }

}
