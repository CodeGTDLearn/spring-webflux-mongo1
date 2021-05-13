package com.mongo.api.modules.post.entity;

import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.user.entity.AuthorDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post")
public class Post implements Serializable {

    private static final long serialVersionUID = -6281811500337260230L;

    @EqualsAndHashCode.Include
    @Id
    private String id;
    private Date date;
    private String title;
    private String body;
    private AuthorDto author;
    private Comment comment;
    private List<Comment> listComments = new ArrayList<>();
    private List<String> idComments = new ArrayList<>();
}
