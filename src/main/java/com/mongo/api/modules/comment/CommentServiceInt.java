package com.mongo.api.modules.comment;

import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentServiceInt {
    Flux<Comment> findAll();

    Mono<Comment> findById(String id);

    Mono<User> findUserByCommentId(String id);

    Mono<Post> saveLinkedObject(Comment comment);

    Mono<Post> saveEmbedObjectSubst(Comment comment);

    Mono<Post> saveEmbedObjectList(Comment comment);

    Mono<Void> delete(Comment comment);

    Mono<Comment> update(Comment comment);

    Flux<Comment> findCommentsByPostId(String postId);
}