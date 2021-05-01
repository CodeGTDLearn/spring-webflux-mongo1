package com.mongo.api.core.exceptions.customExceptions.handlers;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CustomExceptionDetails {
    private String title;
    private String detail;
    private String devAeeeeloperMessage;
    private int status;
    private long timeStamp;
}
