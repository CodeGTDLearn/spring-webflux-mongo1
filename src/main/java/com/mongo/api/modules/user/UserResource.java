package com.mongo.api.modules.user;

import com.mongo.api.core.dto.PostDto;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import static com.mongo.api.core.Routes.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_USER)
public class UserResource {

    private final UserServiceInt service;

    private final ModelMapper converter;


    @GetMapping(FIND_ALL_USERS)
    @ResponseStatus(OK)
    public Flux<User> findAll() {
        return service.findAll()
                      .log();
    }


    @GetMapping(FIND_ALL_SHOW_ALL_DTO)
    @ResponseStatus(OK)
    public Flux<UserAllDto> findAllUserShowAllDto() {
        return service.findAllUserShowAll()
                      .log();
    }


    @GetMapping(FIND_ALL_USERS_DTO)
    @ResponseStatus(OK)
    public Flux<UserDto> findAllDto() {
        return service
                .findAll()
                .map(user -> {
                    return converter.map(user,UserDto.class);
                });
    }


    @GetMapping(FIND_USER_BY_ID)
    @ResponseStatus(OK)
    public Mono<UserDto> findById(@PathVariable String id) {
        return service
                .findById(id)
                .map(userFound -> converter.map(userFound,UserDto.class));
    }


    @GetMapping(ERROR_PATH)
    public Flux<User> globalExceptionError() {
        return service.globalExceptionError();
    }


    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<UserDto> save(@Valid @RequestBody UserDto userDto) {
        User user = converter.map(userDto,User.class);
        return service
                .save(user)
                .map(item -> converter.map(item,UserDto.class));
    }


    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@Valid @RequestBody UserDto userDto) {
        return service.delete(userDto.getId());
    }


    @PutMapping
    @ResponseStatus(OK)
    public Mono<User> update(@Valid @RequestBody UserDto userDto) {
        User user = converter.map(userDto,User.class);
        return service.update(user);
    }


    @GetMapping(FIND_POSTS_BY_USERID)
    @ResponseStatus(OK)
    public Flux<PostDto> findPostsByUserId(@PathVariable String userId) {
        return service
                .findPostsByUserId(userId)
                .map(post -> {
                    return converter.map(post,PostDto.class);
                });
    }
}