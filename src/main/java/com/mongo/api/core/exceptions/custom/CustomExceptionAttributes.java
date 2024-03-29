package com.mongo.api.core.exceptions.custom;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CustomExceptionAttributes {
//    private String title;
    private String detail;
    private String classType;
    private int status;
    private long timeStamp;
}