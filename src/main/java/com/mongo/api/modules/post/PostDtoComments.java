package com.mongo.api.modules.post;

import com.mongo.api.modules.comment.Comment;
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
public class PostDtoComments implements Serializable {

    private static final long serialVersionUID = -1312581280754786925L;

    private String id;
    private Date date;
    private String title;
    private String body;
    private String authorName;
    private List<Comment> listComments = new ArrayList<>();

    public PostDtoComments(Post post) {
        this.id = post.getId();
        this.date = post.getDate();
        this.title = post.getTitle();
        this.body = post.getBody();
        this.authorName = post.getAuthor()
                              .getName();
        this.listComments = post.getListComments();
    }
}
