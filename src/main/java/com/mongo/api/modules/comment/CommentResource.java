package com.mongo.api.modules.comment;

import com.mongo.api.core.dto.CommentAllDtoFull;
import com.mongo.api.core.exceptions.custom.CustomExceptionsThrower;
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

// ==> EXCEPTIONS IN CONTROLLER:
// *** REASON: IN WEBFLUX, EXCEPTIONS MUST BE IN CONTROLLER - WHY?
//     - "Como stream pode ser manipulado por diferentes grupos de thread,
//     - caso um erro aconteça em uma thread que não é a que operou a controller,
//     - o ControllerAdvice não vai ser notificado "
//     - https://medium.com/nstech/programa%C3%A7%C3%A3o-reativa-com-spring-boot-webflux-e-mongodb-chega-de-sofrer-f92fb64517c3
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_COMMENT)
public class CommentResource {

  private final IPostService postService;

  private final IUserService userService;

  private final ICommentService commentService;

  private final CustomExceptionsThrower customExceptionsThrower;


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
         .switchIfEmpty(customExceptionsThrower.commentNotFoundException());
  }


  @GetMapping(FIND_USER_BY_COMMENTID)
  @ResponseStatus(OK)
  public Mono<User> findUserByCommentId(@PathVariable String id) {
    return commentService
         .findUserByCommentId(id)
         .switchIfEmpty(customExceptionsThrower.commentNotFoundException());
  }


  @GetMapping(FIND_COMMENTS_BY_AUTHORIDV1)
  @ResponseStatus(OK)
  public Flux<Comment> findCommentsByAuthorIdV1(@PathVariable String id) {
    return userService
         .findById(id)
         .switchIfEmpty(customExceptionsThrower.userNotFoundException())
         .flatMapMany(user1 -> commentService.findCommentsByAuthorIdV1(user1.getId()));
  }

  @GetMapping(FIND_COMMENTS_BY_AUTHORIDV2)
  @ResponseStatus(OK)
  public Flux<Comment> findCommentsByAuthor_IdV2(@PathVariable String id) {
    return userService
         .findById(id)
         .switchIfEmpty(customExceptionsThrower.userNotFoundException())
         .flatMapMany(user1 -> commentService.findCommentsByAuthor_IdV2(user1.getId()));
  }


  @GetMapping(FIND_COMMENTS_BY_POSTID)
  @ResponseStatus(OK)
  public Flux<Comment> findCommentsByPostId(@PathVariable String id) {
    return postService
         .findById(id)
         .switchIfEmpty(customExceptionsThrower.postNotFoundException())
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
         .switchIfEmpty(customExceptionsThrower.userNotFoundException())

         .then(postService
                    .findById(comment.getPostId())
                    .switchIfEmpty(customExceptionsThrower.postNotFoundException()))

         .then(commentService.saveLinked(comment));
  }


  @PostMapping(SAVE_COMMENT_EMBED_OBJECT_SUBST)
  @ResponseStatus(CREATED)
  public Mono<Post> saveEmbedSubst(@RequestBody Comment comment) {
    return userService

         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptionsThrower.userNotFoundException())

         .then(postService.findById(comment.getPostId()))
         .switchIfEmpty(customExceptionsThrower.postNotFoundException())

         .then(commentService.saveEmbedSubst(comment));
  }


  @PostMapping(SAVE_COMMENT_EMBED_OBJECT_LIST)
  @ResponseStatus(CREATED)
  public Mono<Post> saveEmbedList(@RequestBody Comment comment) {
    return userService
         .findById(comment.getAuthor()
                          .getId())
         .switchIfEmpty(customExceptionsThrower.userNotFoundException())

         .then(postService.findById(comment.getPostId())
                       .switchIfEmpty(customExceptionsThrower.postNotFoundException()))

         .then(commentService.saveEmbedList(comment));
  }


  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public Mono<Void> delete(@RequestBody Comment comment) {
    return commentService
         .findById(comment.getCommentId())
         .switchIfEmpty(customExceptionsThrower.commentNotFoundException())
         .then(commentService.delete(comment));
  }


  @PutMapping
  @ResponseStatus(OK)
  public Mono<Comment> update(@RequestBody Comment comment) {
    return commentService
         .findById(comment.getCommentId())
         .switchIfEmpty(customExceptionsThrower.commentNotFoundException())
         .then(commentService.update(comment));
  }

}