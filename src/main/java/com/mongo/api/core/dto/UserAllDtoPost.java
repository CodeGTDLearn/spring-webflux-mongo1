package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserAllDtoPost{

    private String id;
    private String title;
    private List<UserAllDtoComment> comments = new ArrayList<>();

}
