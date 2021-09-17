package com.mongo.api.modules.comment;

import com.mongo.api.core.dto.CommentAllDtoFull;
import com.mongo.api.core.dto.PostDtoSlim;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.IPostRepo;
import com.mongo.api.modules.post.IPostService;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.IUserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service("comService")
@AllArgsConstructor
public class CommentService implements ICommentService {

  private final ICommentRepo commentRepo;

  @Lazy
  private final IPostRepo postRepo;

  @Lazy
  private final IPostService postService;

  @Lazy
  private final IUserService userService;

  @Lazy
  private final ModelMapper modelMapper;

  @Lazy
  private final CustomExceptions customExceptions;


  @Override
  public Flux<Comment> findAll() {
    return commentRepo.findAll();
  }


  @Override
  public Flux<CommentAllDtoFull> findAllCommentsDto() {
    return commentRepo
         .findAll()
         .flatMap(comment -> {
           CommentAllDtoFull dto = modelMapper.map(comment,CommentAllDtoFull.class);
           return Mono.just(dto);
         })
         .flatMap(commentAllDtoFull -> postRepo
                       .findById(commentAllDtoFull.getPostId())
                       .flatMap(post -> {
                         PostDtoSlim postSlim = modelMapper.map(post,PostDtoSlim.class);
                         commentAllDtoFull.setPost(postSlim);
                         return Mono.just(commentAllDtoFull);
                       })
                 );
  }


  @Override
  public Mono<Comment> findById(String id) {
    return commentRepo.findById(id)
                      .switchIfEmpty(customExceptions.commentNotFoundException());
  }


  @Override
  public Mono<User> findUserByCommentId(String id) {
    return commentRepo
         .findById(id)
         .switchIfEmpty(customExceptions.commentNotFoundException())
         .flatMap(comment -> {
           String idUser = comment.getAuthor()
                                  .getId();
           return userService.findById(idUser);
         });
  }


  @Override
  public Mono<Comment> saveLinkedObject(Comment comment) {
    return userService
         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptions.userNotFoundException())

         .then(postRepo.findById(comment.getPostId())
                       .switchIfEmpty(customExceptions.postNotFoundException()))

         .then(commentRepo.save(comment))

         .flatMap(comment1 ->
                       postRepo
                            .findById(comment1.getPostId())
                            .flatMap(postFound ->
                                     {
                                       postFound.getIdComments()
                                                .add(comment1.getCommentId());
                                       return postRepo.save(postFound);
                                     }
                                    )
                            .then(Mono.just(comment1))
                 );
  }


  @Override
  public Mono<Post> saveEmbedObjectSubst(Comment comment) {
    return userService
         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptions.userNotFoundException())

         .then(postRepo.findById(comment.getPostId()))
         .switchIfEmpty(customExceptions.postNotFoundException())

         .then(commentRepo.save(comment))

         .flatMap(commentSaved ->
                       postRepo
                            .findById(commentSaved.getPostId())
                            .switchIfEmpty(customExceptions.postNotFoundException())
                            .flatMap(postFound ->
                                     {
                                       postFound.setComment(commentSaved);
                                       return postRepo.save(postFound);
                                     }
                                    ));
  }


  @Override
  public Mono<Post> saveEmbedObjectList(Comment comment) {
    return userService
         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptions.userNotFoundException())
         .then(postRepo.findById(comment.getPostId())
                       .switchIfEmpty(customExceptions.postNotFoundException()))
         .then(commentRepo.save(comment))
         .flatMap(commentSaved ->
                       postRepo
                            .findById(commentSaved.getPostId())
                            .switchIfEmpty(customExceptions.postNotFoundException())
                            .flatMap(postFound ->
                                     {
                                       postFound.getListComments()
                                                .add(commentSaved);
                                       return postRepo.save(postFound);
                                     }
                                    ));
  }


  @Override
  public Mono<Void> delete(Comment comment) {
    return commentRepo
         .findById(comment.getCommentId())
         .switchIfEmpty(customExceptions.commentNotFoundException())
         .flatMap(comment1 -> postRepo
              .findById(comment1.getPostId())
              .flatMap(post -> {
                post.getIdComments().remove(comment1.getCommentId());
                return Mono.just(post);
              })
              .flatMap(postService::update)
              .then(commentRepo.delete(comment)));
  }

  @Override
  public Mono<Void> deleteAll() {
    return commentRepo.deleteAll();
  }

  @Override
  public Mono<Comment> update(Comment newComment) {
    return commentRepo
         .findById(newComment.getCommentId())
         .switchIfEmpty(customExceptions.commentNotFoundException())
         .flatMap(comment -> {
           Comment updatedComment = modelMapper.map(newComment,Comment.class);
           return commentRepo.save(updatedComment);
         });
  }


  @Override
  public Flux<Comment> findCommentsByPostId(String postId) {
    return postRepo
         .findById(postId)
         .switchIfEmpty(customExceptions.postNotFoundException())
         .thenMany(commentRepo.findAll())
         .filter(commentsOfThePost -> commentsOfThePost.getPostId()
                                                       .equals(postId));

  }


  @Override
  public Flux<Comment> findCommentsByAuthorId(String authorId) {
    return userService
         .findById(authorId)
         .switchIfEmpty(customExceptions.userNotFoundException())
         .thenMany(commentRepo.findAll())
         .filter(comment -> comment.getAuthor()
                                   .getId()
                                   .equals(authorId));
  }
}
