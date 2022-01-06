package com.mongo.api.modules.comment;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository("commentRepo")
public interface ICommentRepo extends ReactiveMongoRepository<Comment, String> {
  Flux<Comment> findCommentsByAuthor_Id(String authorId);
}