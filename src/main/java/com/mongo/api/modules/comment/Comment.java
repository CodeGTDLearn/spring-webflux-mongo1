package com.mongo.api.modules.comment;

import com.mongo.api.core.dto.UserAuthorDto;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment implements Serializable {

    private static final long serialVersionUID = -6060878415404812469L;

    @EqualsAndHashCode.Include
    @Id
    private String commentId;
    private String postId;
    private Date date;
    private String text;
    private UserAuthorDto author;
}