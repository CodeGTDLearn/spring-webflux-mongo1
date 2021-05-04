package com.mongo.api.core.exceptions.globalException;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static org.springframework.boot.web.error.ErrorAttributeOptions.*;

@Component
@Order(-2)
//CustomGlobalExceptionHandler COMES BEFORE the SpringWebFluxGlobalExceptionHandlerDefault
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final String completeStackTrace = "completeStackTrace=true";


    public GlobalExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties.Resources resources,
            ApplicationContext applicationContext,
            ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes,resources,applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }


    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions
                .route(
                        RequestPredicates.all(),
                        this::formatErrorResponse
                      );
    }


    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {

        String query =
                request
                        .uri()
                        .getQuery();

        ErrorAttributeOptions errorAttributeOptions =
                isTraceEnabled(query)
                        ? of(Include.STACK_TRACE)
                        : defaults();

        Map<String, Object>
                errorAttribsMap =
                getErrorAttributes(request,errorAttributeOptions);


        int status = (int) Optional
                .ofNullable(errorAttribsMap
                                    .get("status"))
                .orElse(500);

        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorAttribsMap));
    }


    // Hence: ?trace=true in the url show the COMPLETE STACK-TRACK IN THE BROWSER
    // instead the CustomErrorHandlingException(simplified) as Stack-Trace
    private boolean isTraceEnabled(String query) {
        return StringUtils.hasText(query) && query.contains(completeStackTrace);
    }
}
