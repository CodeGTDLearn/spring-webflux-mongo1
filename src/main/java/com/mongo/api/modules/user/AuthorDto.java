package com.mongo.api.modules.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class AuthorDto implements Serializable {

    private static final long serialVersionUID = 6623330750803811928L;

    private String id;
    private String name;

    public AuthorDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }
}
