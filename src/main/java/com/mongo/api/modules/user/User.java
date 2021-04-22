package com.mongo.api.modules.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 5370607688333864714L;

    @EqualsAndHashCode.Include
    @Id
    private String id;
    private String name;
    @Email
    private String email;
    private List<String> idPosts = new ArrayList<>();
}
