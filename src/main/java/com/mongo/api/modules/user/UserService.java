package com.mongo.api.modules.user;

import com.mongo.api.core.dto.CommentAllDto;
import com.mongo.api.core.dto.PostAllDto;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.globalException.GlobalException;
import com.mongo.api.modules.comment.CommentServiceInt;
import com.mongo.api.modules.post.PostServiceInt;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserServiceInt {

  private final UserRepo userRepo;

  private final PostServiceInt postService;

  private final ModelMapper mapper;

  private final CommentServiceInt commentService;

  private final CustomExceptions customExceptions;

  private final GlobalException globalException;


  @Override
  public Flux<User> findAll() {
    return userRepo.findAll();
  }


  @Override
  public Mono<User> findById(String id) {
    return userRepo
           .findById(id)
           .switchIfEmpty(customExceptions.userNotFoundException());
  }


  @Override
  public Flux<User> globalExceptionError() {
    return userRepo
           .findAll()
           .concatWith(globalException.globalErrorException());
  }


  @Override
  public Mono<User> save(User user) {
    return userRepo.save(user);
  }


  @Override
  public Mono<Void> deleteAll() {
    return userRepo.deleteAll();
  }


  @Override
  public Mono<User> update(User user) {
    return userRepo
           .findById(user.getId())
           .switchIfEmpty(customExceptions.userNotFoundException())
           .flatMap((item) -> {
             item.setId(user.getId());
             item.setName(user.getName());
             item.setEmail(user.getEmail());
             return userRepo.save(item);
           });
  }


  @Override
  public Mono<Void> delete(String id) {
    return userRepo
           .findById(id)
           .switchIfEmpty(customExceptions.userNotFoundException())
           .flatMapMany(user -> {
             userRepo.delete(user);
             return postService.findPostsByAuthorId(user.getId());
           })
           .flatMap(post -> {
             postService.delete(post);
             return commentService.findCommentsByPostId(post.getPostId());
           })
           .flatMap(comment -> commentService.delete(comment)
                   )

           .then()
           ;
  }


  @Override
  public Flux<UserAllDto> findAllShowAllDto() {

    return userRepo
           .findAll()
           .flatMap(user -> {
             UserAllDto userDto = mapper.map(user,UserAllDto.class);

             final Mono<UserAllDto> userAllDtoMono =
                    postService
                           .findPostsByAuthorId(userDto.getId())
                           .flatMap(post -> {
                             PostAllDto postDto = mapper.map(post,PostAllDto.class);

                             final Mono<PostAllDto> postAllDtoMono =
                                    commentService.findCommentsByPostId(
                                           postDto.getPostId())
                                                  .map(c -> mapper.map(c,
                                                                       CommentAllDto.class
                                                                      ))
                                                  .collectList()
                                                  .flatMap(list -> {
                                                    postDto.setListComments(list);
                                                    return Mono.just(postDto);
                                                  });
                             return postAllDtoMono.flux();
                           })
                           .collectList()
                           .flatMap(list -> {
                             userDto.setPosts(list);
                             return Mono.just(userDto);
                           });
             return userAllDtoMono.flux();
           });
  }
}