package com.mongo.api.core.exceptions.globalException;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class GlobalExceptionAttributes extends DefaultErrorAttributes {

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
            globalExceptionAttributes.put("Global-AtribMessage",errorFound.getMessage());
            globalExceptionAttributes.put("Global-devAtribMsg","Generic Exception");
        }


        // NAO SENDO UMA GLOBAL-EXCEPTION(ResponseStatusException)
        // PORTANTO SENDO, UMA CUSTOM-EXCEPTION
        // retorna o valor PADRAO de ATTRIBUTES ou seja,
        // o globalExceptionAttributes "PURO", sem insercao(.put's do IF acima) de qquer atributo personalizado
        // OU SEJA, nao se acrescenta os atributos definidos no IF-ACIMA
        return globalExceptionAttributes;
    }

}
