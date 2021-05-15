package com.mongo.api.modules.user.entity;

import com.mongo.api.modules.post.entity.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserShowAllDto implements Serializable {

    private static final long serialVersionUID = -814599357052357343L;

    private String id;
    private String name;
    private String email;
    private List<Post> Posts = new ArrayList<>();


    public UserShowAllDto(User user) {
        this.id = user.getId();
        this.name = user.getName();

    }


    public User fromDtoToUser(UserShowAllDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail(),
                null
        );
    }
}
