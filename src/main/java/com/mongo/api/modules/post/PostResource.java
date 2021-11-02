package com.mongo.api.modules.post;

import com.mongo.api.core.dto.PostDto;
import com.mongo.api.core.dto.PostDtoComments;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
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

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(REQ_POST)
public class PostResource {

  private final IUserService userService;

  private final IPostService postService;

  private final ModelMapper modelMapper;

  private final CustomExceptions customExceptions;


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
         .switchIfEmpty(customExceptions.postNotFoundException())
         .map(post -> modelMapper.map(post,PostDto.class));
  }


  @GetMapping(FIND_POSTS_BY_AUTHORID)
  @ResponseStatus(OK)
  public Flux<PostDto> findPostsByAuthorId(@PathVariable String id) {
    return userService
         .findById(id)
         .switchIfEmpty(customExceptions.authorNotFoundException())
         .flatMapMany((userFound) ->
                           postService
                                .findPostsByAuthorId(userFound.getId())
                                .map(post -> modelMapper.map(post,PostDto.class)));
  }


  @GetMapping(FIND_POST_BY_ID_SHOW_COMMENTS)
  @ResponseStatus(OK)
  public Mono<PostDtoComments> findPostByIdShowComments(@PathVariable String id) {
        return postService
             .findPostByIdShowComments(id)
             .switchIfEmpty(customExceptions.postNotFoundException())
             .map(item -> modelMapper.map(item,PostDtoComments.class));
  }


  @PostMapping(SAVE_EMBED_USER_IN_THE_POST)
  @ResponseStatus(CREATED)
  public Mono<Post> save(@RequestBody Post post) {
    return userService
         .findById(post.getAuthor()
                       .getId())

         .switchIfEmpty(customExceptions.authorNotFoundException())

         .then(postService.save(post));
  }


  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public Mono<Void> delete(@RequestBody Post post) {

    return postService
         .delete(post)
         .switchIfEmpty(customExceptions.postNotFoundException());
  }


  @PutMapping
  @ResponseStatus(OK)
  public Mono<Post> update(@RequestBody Post newPost) {
    return postService
         .findById(newPost.getPostId())
         .switchIfEmpty(customExceptions.postNotFoundException())
         .flatMap(updatedPost -> {
           return postService.save(updatedPost);
         });
  }


  @GetMapping(FIND_USER_BY_POSTID)
  @ResponseStatus(OK)
  public Mono<User> findUserByPostId(@PathVariable String id) {
    return postService
         .findById(id)
         .switchIfEmpty(customExceptions.postNotFoundException())
         .flatMap(item -> {
           return postService
                .findUserByPostId(item.getPostId())
                .switchIfEmpty(customExceptions.userNotFoundException());
         });
  }
}
