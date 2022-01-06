package com.mongo.api.modules.post;

import com.mongo.api.core.dto.PostDto;
import com.mongo.api.core.dto.PostDtoComments;
import com.mongo.api.core.exceptions.custom.CustomExceptionsThrower;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.routes.RoutesPost.*;
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
@RequestMapping(REQ_POST)
public class PostResource {

  private final IUserService userService;

  private final IPostService postService;

  private final ModelMapper modelMapper;

  private final CustomExceptionsThrower customExceptionsThrower;


  @GetMapping(FIND_ALL_POSTS)
  @ResponseStatus(OK)
  public Flux<Post> findAll() {
    return postService.findAll();
  }


  @GetMapping(FIND_POST_BY_ID)
  @ResponseStatus(OK)
  public Mono<PostDto> findById(@PathVariable String id) {
    return postService
         .findById(id)
         .switchIfEmpty(customExceptionsThrower.postNotFoundException())
         .map(post -> modelMapper.map(post,PostDto.class));
  }


  @GetMapping(FIND_POSTS_BY_AUTHORID)
  @ResponseStatus(OK)
  public Flux<PostDto> findPostsByAuthor_Id(@PathVariable String id) {
    return userService
         .findById(id)
         .switchIfEmpty(customExceptionsThrower.authorNotFoundException())
         .flatMapMany((userFound) ->
                           postService
                                .findPostsByAuthor_Id(userFound.getId())
                                .map(post -> modelMapper.map(post,PostDto.class)));
  }


  @GetMapping(FIND_POST_BY_ID_SHOW_COMMENTS)
  @ResponseStatus(OK)
  public Mono<PostDtoComments> findPostByIdShowComments(@PathVariable String id) {
        return postService
             .findPostByIdShowComments(id)
             .switchIfEmpty(customExceptionsThrower.postNotFoundException())
             .map(item -> modelMapper.map(item,PostDtoComments.class));
  }


  @PostMapping(SAVE_EMBED_USER_IN_THE_POST)
  @ResponseStatus(CREATED)
  public Mono<Post> save(@RequestBody Post post) {
    return userService
         .findById(post.getAuthor()
                       .getId())

         .switchIfEmpty(customExceptionsThrower.authorNotFoundException())

         .then(postService.save(post));
  }


  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public Mono<Void> delete(@RequestBody Post post) {

    return postService
         .findById(post.getPostId())
         .switchIfEmpty(customExceptionsThrower.postNotFoundException())
         .then(postService.delete(post));
  }


  @PutMapping
  @ResponseStatus(OK)
  public Mono<Post> update(@RequestBody Post newPost) {
    return postService
         .findById(newPost.getPostId())
         .switchIfEmpty(customExceptionsThrower.postNotFoundException())
         .then(postService.update(newPost));
  }


  @GetMapping(FIND_USER_BY_POSTID)
  @ResponseStatus(OK)
  public Mono<User> findUserByPostId(@PathVariable String id) {
    return postService
         .findById(id)
         .switchIfEmpty(customExceptionsThrower.postNotFoundException())
         .flatMap(item -> {
           return postService
                .findUserByPostId(item.getPostId())
                .switchIfEmpty(customExceptionsThrower.userNotFoundException());
         });
  }
}