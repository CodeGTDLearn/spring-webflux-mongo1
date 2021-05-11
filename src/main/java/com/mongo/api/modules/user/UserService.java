package com.mongo.api.modules.user;

import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.globalException.GlobalException;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Service
@RequiredArgsConstructor
public class UserService {

    // todo: 01 suspeita 01, se esses caras estao aki nao esta sendo injetados aqui, eles nao deveriam ser injetados/necessarios no UserServiceTest, no UserServiceTest esta sendo necessario instanciar o UserService e dar/instanciar(via autowired) esses 2 repos aqui elencados, justamente pq aqui, eles nao estao sendo instanciados/injetados corretamente, e portanto, se estivesse ok aqui, estariam automaticamente fazendo parte desse UserService, que por sua vez seria inserido via Autowired no UserServiceTest
    private final UserRepo userRepo;

    private final PostRepo postRepo;

    // todo: 02 esses @Autowired, nao esta compativeis com o @REquiredArgsContructors, pois esses dois autowireds, estao nulos , e nao estao sendo instanciados qdo o teste roda, e por isso que falham os testes que possuem exceptions, pois esssas instancias nao estao sendo inicializadas/criadas aqui

    // todo: 03 Implementar interfaces, isso pode estar dando problemas Tb. E TB, usar qualifiers, pois vc possui 2 componentes num service, dois repos num service,. isso pode estar dando conflito
    @Autowired
    private CustomExceptions customExceptions;

    @Autowired
    private GlobalException globalException;


    public Flux<User> findAll() {
        return userRepo.findAll();
    }
    

    public Mono<User> findById(String id) {
        return userRepo
                .findById(id)
                .switchIfEmpty(customExceptions.userNotFoundException());
    }


    public Flux<User> globalExceptionError() {
        return userRepo
                .findAll()
                .concatWith(globalException.globalErrorException());
    }


    public Mono<User> save(User user) {
        return userRepo.save(user);
    }


    public Mono<Void> deleteById(String id) {
        return userRepo
                .findById(id)
                .switchIfEmpty(customExceptions.userNotFoundException())
                .flatMap(userRepo::delete);
    }


    public Mono<Void> deleteAll() {
        return userRepo.deleteAll();
    }


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


    public Flux<Post> findPostsByUserId(String userId) {
        return userRepo
                .findById(userId)
                .switchIfEmpty(customExceptions.userNotFoundException())
                .flatMapMany((userFound) -> {
                    var id = userFound.getId();
                    return postRepo.findPostsByAuthor_Id(id);
                });
    }
}
