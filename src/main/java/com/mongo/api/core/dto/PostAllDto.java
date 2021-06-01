package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PostAllDto {
    private String postId;
    private String title;
    private String idAuthor;
    private List<CommentAllDto> listComments = new ArrayList<>();

}
