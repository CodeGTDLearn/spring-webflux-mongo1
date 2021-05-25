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

import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class UserService implements UserServiceInt {

    private final UserRepo userRepo;

    private final PostServiceInt postService;

    private final CommentServiceInt commentService;

    private final ModelMapper conv;

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
                .flatMap(userRepo::delete);
    }


    @Override
    public Flux<UserAllDto> findAllShowAllDto() {

        return userRepo
                .findAll()
                .flatMap(user -> Flux.fromIterable(List.of(conv.map(user,UserAllDto.class))))
                .flatMap(userAllDto -> postService
                                 .findPostsByAuthorId(userAllDto.getId())
                                 .flatMap(
                                         post -> Flux.fromIterable(List.of(conv.map(post,
                                                                                    PostAllDto.class
                                                                                   ))))
                        )
                .flatMap(postAllDto -> commentService
                                 .findCommentsByPostId(postAllDto.getId())
                                 .map(comment -> conv.map(comment,CommentAllDto.class))
                                 .collectList()
                                 .flatMap(list -> {
                                     postAllDto.setListComments(list);
                                     return Mono.just(List.of(postAllDto));
                                 })
                        )
                .flatMap(ListPostAllDto ->
                                 userRepo.findAll()
                                         .flatMap(user -> Flux.fromIterable(
                                                 List.of(conv.map(user,UserAllDto.class))))
                                         .flatMap(userAllDto -> {
                                             if (!userAllDto.getPosts()
                                                            .isEmpty()) {
                                                 userAllDto.setPosts(ListPostAllDto);
                                                 //como saber se essa lista de post e realmente deste cara
                                             }
                                             return Flux.fromIterable(List.of(userAllDto));
                                         }));

    }
}