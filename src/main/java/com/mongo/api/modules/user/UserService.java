package com.mongo.api.modules.user;

import com.mongo.api.core.dto.CommentAllDto;
import com.mongo.api.core.dto.PostAllDto;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.globalException.GlobalException;
import com.mongo.api.modules.comment.CommentServiceInt;
import com.mongo.api.modules.post.PostServiceInt;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;


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
                .flatMap(user -> {
                    UserAllDto userDto = conv.map(user,UserAllDto.class);

                    return postService
                            .findPostsByAuthorId(userDto.getId())
                            .flatMap(post -> {
                                PostAllDto postDto = conv.map(post,PostAllDto.class);

                                return commentService
                                        .findCommentsByPostId(postDto.getId())
                                        .flatMap(comment -> {
                                            postDto.getListComments()
                                                   .add(conv.map(comment,CommentAllDto.class));

                                            userDto.getPosts()
                                                   .add(postDto);

                                            return Flux
                                                    .fromIterable(
                                                            Collections.singletonList(userDto));
                                        });
                            });
                });
    }
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
