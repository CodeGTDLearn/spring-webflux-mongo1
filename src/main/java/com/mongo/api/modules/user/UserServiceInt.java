package com.mongo.api.modules.user;

import com.mongo.api.modules.post.entity.Post;
import com.mongo.api.modules.user.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserServiceInt {
    Flux<User> findAll();

    Mono<User> findById(String id);

    Flux<User> globalExceptionError();

    Mono<User> save(User user);

    Mono<Void> deleteById(String id);

    Mono<Void> deleteAll();

    Mono<User> update(User user);

    Flux<Post> findPostsByUserId(String userId);
}
