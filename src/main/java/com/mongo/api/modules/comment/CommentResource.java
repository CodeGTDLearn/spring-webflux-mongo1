package com.mongo.api.modules.comment;

import com.mongo.api.core.dto.CommentAllDtoFull;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.modules.post.IPostService;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.routes.RoutesComment.*;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_COMMENT)
public class CommentResource {

  private final IPostService postService;

  private final IUserService userService;

  private final ICommentService commentService;

  private final CustomExceptions customExceptions;


  @GetMapping(FIND_ALL_COMMENTS)
  @ResponseStatus(OK)
  public Flux<Comment> findAll() {
    return commentService.findAll();
  }


  @GetMapping(FIND_ALL_COMMENTS_DTO)
  @ResponseStatus(OK)
  public Flux<CommentAllDtoFull> findAllCommentsDto() {
    return commentService.findAllCommentsDto();
  }


  @GetMapping(FIND_COMMENT_BY_ID)
  @ResponseStatus(OK)
  public Mono<Comment> findById(@PathVariable String id) {
    return commentService
         .findById(id)
         .switchIfEmpty(customExceptions.commentNotFoundException());
  }


  @GetMapping(FIND_USER_BY_COMMENTID)
  @ResponseStatus(OK)
  public Mono<User> findUserByCommentId(@PathVariable String id) {
    return commentService
         .findUserByCommentId(id)
         .switchIfEmpty(customExceptions.commentNotFoundException());
  }


  @GetMapping(FIND_COMMENTS_BY_AUTHORID)
  @ResponseStatus(OK)
  public Flux<Comment> findCommentsByAuthorId(@PathVariable String authorId) {
    return userService
         .findById(authorId)
         .switchIfEmpty(customExceptions.userNotFoundException())
         .flatMapMany(user1 -> {
           return commentService.findCommentsByAuthorId(user1.getId());
         });
  }


  @GetMapping(FIND_COMMENTS_BY_POSTID)
  @ResponseStatus(OK)
  public Flux<Comment> findCommentsByPostId(@PathVariable String postId) {
    return postService
         .findById(postId)
         .switchIfEmpty(customExceptions.postNotFoundException())
         .flatMapMany(postFound -> {
           return commentService.findCommentsByPostId(postFound.getPostId());
         });
  }


  @PostMapping(SAVE_COMMENT_LINKED_OBJECT)
  @ResponseStatus(CREATED)
  public Mono<Comment> saveLinked(@RequestBody Comment comment) {
    return userService
         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptions.userNotFoundException())

         .then(postService
                    .findById(comment.getPostId())
                    .switchIfEmpty(customExceptions.postNotFoundException()))

         .then(commentService.saveLinked(comment));
  }


  @PostMapping(SAVE_COMMENT_EMBED_OBJECT_SUBST)
  @ResponseStatus(CREATED)
  public Mono<Post> saveEmbedSubst(@RequestBody Comment comment) {
    return userService

         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptions.userNotFoundException())

         .then(postService.findById(comment.getPostId()))
         .switchIfEmpty(customExceptions.postNotFoundException())

         .then(commentService.saveSubst(comment));
  }


  @PostMapping(SAVE_COMMENT_EMBED_OBJECT_LIST)
  @ResponseStatus(CREATED)
  public Mono<Post> saveEmbedList(@RequestBody Comment comment) {
    return userService
         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptions.userNotFoundException())

         .then(postService.findById(comment.getPostId())
                       .switchIfEmpty(customExceptions.postNotFoundException()))

         .then(commentService.saveList(comment));
  }


  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public Mono<Void> delete(@RequestBody Comment comment) {
    return commentService
         .delete(comment)
         .switchIfEmpty(customExceptions.commentNotFoundException());
  }


  @PutMapping
  @ResponseStatus(OK)
  public Mono<Comment> update(@RequestBody Comment comment) {
    return commentService
         .update(comment)
         .switchIfEmpty(customExceptions.commentNotFoundException());
  }

}