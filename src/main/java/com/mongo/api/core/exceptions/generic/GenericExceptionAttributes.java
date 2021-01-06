package com.mongo.api.core.exceptions.generic;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class GenericExceptionAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(
            ServerRequest request,
            ErrorAttributeOptions options) {

        Map<String, Object> errorAttributesMap =
                super.getErrorAttributes(request,options);

        //Adiciona a excecao se for do tipo: ResponseStatusException
        Throwable throwable = getError(request);

        if (throwable instanceof ResponseStatusException) {

            ResponseStatusException errorFound = (ResponseStatusException) throwable;

            //Custom Attributes
            errorAttributesMap.put("AtribMessage",errorFound.getMessage());
            errorAttributesMap.put("devAtribMsg","Generic Exception");
        }
        //Se nao for do tipo ResponseStatusException,
        // retorna o padrao encontrado no errorAttributesMap
        return errorAttributesMap;
    }

}
