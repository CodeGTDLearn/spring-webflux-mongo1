package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDtoSlim {

    private String id;
    private String title;
    private String authorName;
}
