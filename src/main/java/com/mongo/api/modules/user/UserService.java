package com.mongo.api.modules.user;

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

    private final ModelMapper conv;

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
                .flatMap(userRepo::delete);
    }


    @Override
    public Flux<UserAllDto> findAllShowAllDto() {

        return userRepo
                .findAll()
                .flatMap(user -> {

                    UserAllDto userDto = conv.map(user,UserAllDto.class);

                    final Mono<UserAllDto> userAllDtoMono =
                            postService
                                    .findPostsByAuthorId(userDto.getId())
                                    .flatMap(post -> Flux.fromIterable(
                                            List.of(conv.map(post,PostAllDto.class))))
                                    .collectList()
                                    .flatMap(list -> {
                                        userDto.setPosts(list);
                                        return Mono.just(userDto);
                                    });

                    return userAllDtoMono.flux();
                })
                ;

        //        return userRepo
        //                .findAll()
        //                .flatMap(user -> postService.findPostsByAuthorId(user.getId()))
        //                .flatMap(post -> Flux.fromIterable(List.of(conv.map(post,PostAllDto
        //                .class))))
        //                .flatMap(postAllDto -> {
        //                    List<PostAllDto> listPostDto = new ArrayList<>();
        //                    listPostDto.add(postAllDto);
        //                    return Mono.just(listPostDto);
        //                })
        //                .flatMap(listPostDto -> {
        //
        //                    var authorId =
        //                            listPostDto.get(0)
        //                                       .getIdAuthor();
        //
        //                    Flux<UserAllDto> usersDto =
        //                            userRepo
        //                                    .findAll()
        //                                    .flatMap(
        //                                            user -> Flux.fromIterable(
        //                                                    List.of(conv.map(user,UserAllDto
        //                                                    .class))))
        //                                    .flatMap(userDto -> {
        //                                        if (userDto.getId()
        //                                                   .equals(authorId)) {
        //                                            userDto.setPosts(listPostDto);
        //                                        } else {
        //                                            userDto.setPosts(Collections.emptyList());
        //                                        }
        //                                        return Flux.fromIterable(List.of(userDto));
        //                                    });
        //
        //                    return usersDto;
        //                })
        //                ;
    }
}