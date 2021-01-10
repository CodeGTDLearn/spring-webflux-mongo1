package com.mongo.api.core.exceptions.custom.handlers;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ExceptionDetails {
    private String title;
    private String detail;
    private String devAeeeeloperMessage;
    private int status;
    private long timeStamp;
}
