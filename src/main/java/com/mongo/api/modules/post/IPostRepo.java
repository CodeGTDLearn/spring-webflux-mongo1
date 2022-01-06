package com.mongo.api.modules.post;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository("postRepo")
public interface IPostRepo extends ReactiveMongoRepository<Post, String> {
    Flux<Post> findPostsByAuthor_Id(String authorId);
}