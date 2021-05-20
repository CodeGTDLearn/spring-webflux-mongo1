package com.mongo.api.modules.user;

import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.dto.UserAllDtoComment;
import com.mongo.api.core.dto.UserAllDtoPost;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.globalException.GlobalException;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostServiceInt;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service("userService")
@AllArgsConstructor
public class UserService implements UserServiceInt {

    private final UserRepo userRepo;

    private final PostServiceInt postService;

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
    public Flux<Post> findPostsByUserId(String userId) {
        return userRepo
                .findById(userId)
                .switchIfEmpty(customExceptions.userNotFoundException())
                .flatMapMany((userFound) -> {
                    var id = userFound.getId();
                    return postService.findPostsByAuthorId(id);
                });
    }


    @Override
    public Flux<UserAllDto> findAllUserShowAll() {

        List<UserAllDtoPost> dtoPosts = new ArrayList<>();
        List<UserAllDtoComment> dtoComments = new ArrayList<>();
        //        List<Comment> comments = new ArrayList<>();


        return userRepo
                .findAll()
                .flatMap(user -> {

                    final UserAllDto userRet = conv.map(user,UserAllDto.class);

                    findPostsByUserId(user.getId())
                            .flatMap(post -> {

//                                commentService
//                                        .findCommentsByPostId(post.getId())
//                                        .flatMap(comment -> {
//                                            dtoComments.add(
//                                                    conv.map(comment,UserAllDtoComment.class));
//                                        });

                                final UserAllDtoPost dtoPost = conv.map(post,UserAllDtoPost.class);
                                dtoPost.setComments(dtoComments);

                                dtoPosts.add(dtoPost);
                                userRet.setPosts(dtoPosts);

                                return Flux.fromIterable(Arrays.asList(post));
                            });


                    return Flux.fromIterable(Arrays.asList(userRet));
                })
                ;
    }
    //                    userRet = converter.map(user,UserShowAllDto.class);
    //
    //                    findPostsByUserId(user.getId())
    //                            .flatMap(post -> {
    //                                postRet = converter.map(post,UserShowAllDtoPost.class);
    //                                commentService
    //                                        .findCommentsByPostId(post.getId())
    //                                        .flatMap(comment -> {
    //                                            dtoCommentList.add(
    //                                                    converter.map(comment,
    //                                                                  UserShowAllDtoComment.class
    //                                                                 ));
    //                                            return comment;
    //                                        });
    //
    //                                postRet.setComments(dtoCommentList);
    //                                dtoPostList.add(postRet);
    //                                return post;
    //                            });
    //
    //                    userRet.setPosts(dtoPostList);
    //                    return userRet;


}
// todo: 01 suspeita 01, se esses caras estao aki nao esta sendo injetados aqui, eles nao
//  deveriam ser injetados/necessarios no UserServiceTest, no UserServiceTest esta sendo
//  necessario instanciar o UserService e dar/instanciar(via autowired) esses 2 repos aqui
//  elencados, justamente pq aqui, eles nao estao sendo instanciados/injetados corretamente, e
//  portanto, se estivesse ok aqui, estariam automaticamente fazendo parte desse UserService, que
//  por sua vez seria inserido via Autowired no UserServiceTest

// todo: 02 esses @Autowired, nao esta compativeis com o @REquiredArgsContructors, pois esses
//  dois autowireds, estao nulos , e nao estao sendo instanciados qdo o teste roda, e por isso
//  que falham os testes que possuem exceptions, pois esssas instancias nao estao sendo
//  inicializadas/criadas aqui
