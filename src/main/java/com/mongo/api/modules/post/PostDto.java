package com.mongo.api.modules.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostDto implements Serializable {


    private static final long serialVersionUID = 5493912789442980032L;

    private String id;
    private Date date;
    private String title;
    private String body;
    private String authorName;
    private List<String> comments = new ArrayList<>();

    public PostDto(Post post) {
        this.id = post.getId();
        this.date = post.getDate();
        this.title = post.getTitle();
        this.body = post.getBody();
        this.authorName = post.getAuthor().getName();
        this.comments = post.getIdComments();
    }
}
