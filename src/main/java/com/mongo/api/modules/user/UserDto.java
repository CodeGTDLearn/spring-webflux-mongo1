package com.mongo.api.modules.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class UserDto implements Serializable {

    private static final long serialVersionUID = 5370607688333864714L;

    private String id;
    private String name;
    private String email;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    public User fromDtoToUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail(),
                null
        );
    }
}
