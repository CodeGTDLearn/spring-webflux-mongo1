package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentAllDtoFull {
    private String commentId;
    private String postId;
//    private String authorId;
    private String text;
    private PostDtoSlim post;
}
