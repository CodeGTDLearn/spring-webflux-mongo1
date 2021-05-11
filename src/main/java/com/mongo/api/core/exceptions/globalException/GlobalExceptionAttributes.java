package com.mongo.api.core.exceptions.globalException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
@Getter
@Setter
@AllArgsConstructor
public class GlobalExceptionAttributes extends DefaultErrorAttributes {


    private GlobalExceptionProperties properties;


    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  ErrorAttributeOptions options) {

        Map<String, Object> globalExceptionAttribs = super.getErrorAttributes(request,options);

        // ADICIONA A GLOBAL-EXCEPTION(ResponseStatusException)
        // POIS NAO SE TRATA DE NENHUMA DAS 'CUSTOM-EXCEPTIONS'
        Throwable throwable = getError(request);
        if (throwable instanceof ResponseStatusException) {

            ResponseStatusException error = (ResponseStatusException) throwable;

            // SENDO UMA GLOBAL-EXCEPTION(ResponseStatusException)
            // adiciona ATTRIBUTES no globalExceptionAttribs
            //            globalExceptionAttribs.put(globalAttribute,error.getMessage() + "|" +
            //            globalMessage);
            globalExceptionAttribs.put(properties.getGlobalAttribute(),
                                       error.getMessage()
                                      );

            globalExceptionAttribs.put(properties.getDeveloperAttribute(),
                                       properties.getDeveloperMessage()
                                      );
        }

        // NAO SENDO UMA GLOBAL-EXCEPTION(ResponseStatusException)
        // PORTANTO SENDO, UMA CUSTOM-EXCEPTION
        // retorna o valor PADRAO de ATTRIBUTES ou seja,
        // o globalExceptionAttribs "PURO", sem insercao(.put's do IF acima) de qquer atributo
        // personalizado
        // OU SEJA, nao se acrescenta os atributos definidos no IF-ACIMA
        return globalExceptionAttribs;
    }

}
