package com.mongo.api.core.dto;

import com.mongo.api.modules.comment.Comment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PostDtoComments {

    private String id;
    private Date date;
    private String title;
    private String body;
    private String authorName;
    private List<Comment> listComments = new ArrayList<>();
//
//    public PostDtoComments(Post post) {
//        this.id = post.getId();
//        this.date = post.getDate();
//        this.title = post.getTitle();
//        this.body = post.getBody();
//        this.authorName = post.getAuthor()
//                              .getName();
//        this.listComments = post.getListComments();
//    }
}
