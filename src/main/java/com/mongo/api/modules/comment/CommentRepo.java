package com.mongo.api.modules.comment;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Qualifier("CommentRepo")
@Repository
public interface CommentRepo extends ReactiveMongoRepository<Comment, String> {
}
