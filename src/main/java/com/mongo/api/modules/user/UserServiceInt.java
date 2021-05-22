package com.mongo.api.modules.user;

import com.mongo.api.core.dto.UserAllDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserServiceInt {
    Flux<User> findAll();

    Mono<User> findById(String id);

    Flux<User> globalExceptionError();

    Mono<User> save(User user);

    Mono<Void> delete(String id);

    Mono<Void> deleteAll();

    Mono<User> update(User user);

    Flux<UserAllDto> findAllShowAllDto();
}
