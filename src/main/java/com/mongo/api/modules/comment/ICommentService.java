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


  Mono<Comment> saveLinked(Comment comment);


  Mono<Post> saveEmbedSubst(Comment comment);


  Mono<Post> saveEmbedList(Comment comment);


  Mono<Void> delete(Comment comment);


  Mono<Void> deleteAll();


  Mono<Comment> update(Comment comment);


  Flux<Comment> findCommentsByPostId(String postId);


  Flux<Comment> findCommentsByAuthorIdV1(String authorId);


  Flux<Comment> findCommentsByAuthor_IdV2(String authorId);


  Flux<CommentAllDtoFull> findAllCommentsDto();
}