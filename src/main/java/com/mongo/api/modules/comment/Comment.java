package com.mongo.api.modules.comment;

import com.mongo.api.modules.user.AuthorDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = -6060878415404812469L;

    @EqualsAndHashCode.Include
    @Id
    private String id;
    private String idPost;
    private Date date;
    private String text;
    private AuthorDto author;
}
