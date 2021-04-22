package com.mongo.api.modules.user;

import com.mongo.api.modules.post.PostDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.Routes.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_USER)
public class UserResource {

    private final UserService service;


    @GetMapping(FIND_ALL_USERS)
    @ResponseStatus(OK)
    public Flux<User> findAll() {
        return service.findAll()
                      .log();
    }


    @GetMapping(FIND_ALL_USERS_DTO)
    @ResponseStatus(OK)
    public Flux<UserDto> findAllDto() {
        return service
                .findAll()
                .map(item -> {
                    return new UserDto(item);
                });
    }


    @GetMapping(FIND_USER_BY_ID)
    @ResponseStatus(OK)
    public Mono<UserDto> findById(@PathVariable String id) {
        return service
                .findById(id)
                .map(UserDto::new);
    }


    @GetMapping(ERROR_PATH)
    public Flux<User> findErrorUserNotFound() {
        return service.findErrorUserNotFound();
    }


    @PostMapping
    @ResponseStatus(CREATED)
    public Mono<UserDto> save(@RequestBody UserDto userdto) {
        User user = new UserDto().fromDtoToUser(userdto);
        return service
                .save(user)
                .map(item -> new UserDto(item));
    }


    @DeleteMapping
    @ResponseStatus(NO_CONTENT)
    public Mono<Void> delete(@RequestBody UserDto userDto) {
        return service.deleteById(userDto.getId());
    }


    @PutMapping
    @ResponseStatus(OK)
    public Mono<User> update(@RequestBody UserDto userDto) {
        User user = new UserDto().fromDtoToUser(userDto);
        return service
                .update(user);
    }


    @GetMapping(FIND_POSTS_BY_USERID)
    @ResponseStatus(OK)
    public Flux<PostDto> findPostsByUserId(@PathVariable String id) {
        return service
                .findPostsByUserId(id)
                .map(PostDto::new);
    }
}