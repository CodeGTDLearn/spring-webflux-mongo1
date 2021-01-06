package com.mongo.api.modules.comment;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepo extends ReactiveMongoRepository<Comment, String> {
}
