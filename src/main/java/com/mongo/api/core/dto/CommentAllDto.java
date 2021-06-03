package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentAllDto {
    private String commentId;
    private String postId;
    private String idAuthor;
    private String text;
}
