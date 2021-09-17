package com.mongo.api.modules.comment;

import com.mongo.api.core.dto.CommentAllDtoFull;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICommentService {
  Flux<Comment> findAll();

  Mono<Comment> findById(String id);

  Mono<User> findUserByCommentId(String id);

  Mono<Comment> saveLinkedObject(Comment comment);

  Mono<Post> saveEmbedObjectSubst(Comment comment);

  Mono<Post> saveEmbedObjectList(Comment comment);

  Mono<Void> delete(Comment comment);

  Mono<Void> deleteAll();

  Mono<Comment> update(Comment comment);

  Flux<Comment> findCommentsByPostId(String postId);

  Flux<Comment> findCommentsByAuthorId(String authorId);

  Flux<CommentAllDtoFull> findAllCommentsDto();
}
