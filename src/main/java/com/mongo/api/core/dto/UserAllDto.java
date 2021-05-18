package com.mongo.api.core.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserAllDto  {

    private String id;
    private String name;
    private List<UserAllDtoPost> Posts = new ArrayList<>();


//    public UserShowAllDto(User user) {
//        this.id = user.getId();
//        this.name = user.getName();
//
//    }
//
//
//    public User fromDtoToUser(UserShowAllDto userDto) {
//        return new User(
//                userDto.getId(),
//                userDto.getName(),
//                userDto.getEmail(),
//                null
//        );
//    }
}
