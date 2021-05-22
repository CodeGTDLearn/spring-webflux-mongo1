package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserAllDto  {

    private String id;
    private String name;
    private List<PostAllDto> Posts = new ArrayList<>();

}
