package com.mongo.api.modules.post;

import com.mongo.api.modules.post.entity.Post;
import com.mongo.api.modules.user.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostServiceInt {
    Mono<Post> findPostById(String id);

    Mono<Post> findPostByIdShowComments(String id);

    Mono<User> findUserByPostId(String id);

    Flux<Post> findAll();

    Mono<Post> save(Post post);

    Mono<Void> delete(Post post);

    Mono<Void> deleteAll();

    Mono<Post> update(Post post);
}
