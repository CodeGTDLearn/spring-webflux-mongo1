package com.mongo.api.modules.comment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Qualifier("CommentRepo")
@Repository
public interface CommentRepo extends ReactiveMongoRepository<Comment, String> {
  Flux<Comment> findCommentsByAuthor_Id(String authorId);


}
