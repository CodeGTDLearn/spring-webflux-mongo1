package com.mongo.api.modules.post;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PostRepo extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findPostsByAuthor_Id(String authorId);
    Mono<Post> findPostByComment_CommentId(String commentId);
}
