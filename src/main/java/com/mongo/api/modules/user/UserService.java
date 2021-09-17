package com.mongo.api.modules.user;

import com.mongo.api.core.dto.CommentAllDto;
import com.mongo.api.core.dto.PostAllDto;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.dto.UserAuthorDto;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.globalException.GlobalException;
import com.mongo.api.modules.comment.ICommentService;
import com.mongo.api.modules.post.IPostService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service("userService")
@AllArgsConstructor
public class UserService implements IUserService {

  private final IUserRepo userRepo;

  @Lazy
  private final IPostService postService;

  @Lazy
  private final ICommentService commentService;

  @Lazy
  private final ModelMapper modelMapper;

  @Lazy
  private final CustomExceptions customExceptions;

  @Lazy
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

         .thenMany(postService.findPostsByAuthorId(user.getId()))
         .flatMap(post -> Mono.just(post.getPostId()))
         .collectList()
         .flatMap(listPosts -> {
           user.setIdPosts(listPosts);
           return userRepo.save(user);
         })

         .thenMany(postService.findPostsByAuthorId(user.getId()))
         .flatMap(post -> {
           UserAuthorDto authorDto = modelMapper.map(user,UserAuthorDto.class);
           post.setAuthor(authorDto);
           return postService.update(post);
         })

         .thenMany(commentService.findCommentsByAuthorId(user.getId()))
         .flatMap(comment -> {
           UserAuthorDto authorDto = modelMapper.map(user,UserAuthorDto.class);
           comment.setAuthor(authorDto);
           return commentService.update(comment);
         })

         .then(userRepo.findById(user.getId()))
         ;
  }


  @Override
  public Mono<Void> delete(String id) {
    return userRepo
         .findById(id)
         .switchIfEmpty(customExceptions.userNotFoundException())
         .flatMap(
              user -> postService
                   .findPostsByAuthorId(user.getId())
                   .flatMap(
                        post -> commentService.findCommentsByPostId(
                             post.getPostId())
                                              .flatMap(commentService::delete)
                                              .thenMany(
                                                   postService.findPostsByAuthorId(
                                                        post.getAuthor()
                                                            .getId()))
                                              .flatMap(postService::delete)
                           )
                   .then(userRepo.delete(user))
                 );
  }


  @Override
  public Flux<UserAllDto> findAllShowAllDto() {

    return userRepo
         .findAll()
         .flatMap(user -> {
           UserAllDto userDto = modelMapper.map(user,UserAllDto.class);

           final Mono<UserAllDto> userAllDtoMono =
                postService
                     .findPostsByAuthorId(userDto.getId())
                     .flatMap(post -> {
                       PostAllDto postDto = modelMapper.map(post,PostAllDto.class);

                       final Mono<PostAllDto> postAllDtoMono =
                            commentService.findCommentsByPostId(
                                 postDto.getPostId())
                                          .map(c -> modelMapper.map(c,
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