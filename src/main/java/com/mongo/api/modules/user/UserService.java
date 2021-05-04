package com.mongo.api.modules.user;

import com.mongo.api.core.exceptions.customExceptions.customExceptionHandler.CustomExceptions;
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

    private final UserRepo userRepo;

    private final PostRepo postRepo;

    @Autowired
    private CustomExceptions exceptions;


    public Flux<User> findAll() {
        return userRepo.findAll();
    }
    

    public Mono<User> findById(String id) {
        return userRepo
                .findById(id)
                .switchIfEmpty(exceptions.userNotFoundException());
    }


    public Flux<User> globalExceptionError() {
        return userRepo
                .findAll()
                .concatWith(exceptions.globalErrorException());
    }


    public Mono<User> save(User user) {
        return userRepo.save(user);
    }


    public Mono<Void> deleteById(String id) {
        return userRepo
                .findById(id)
                .switchIfEmpty(exceptions.userNotFoundException())
                .flatMap(userRepo::delete);
    }


    public Mono<Void> deleteAll() {
        return userRepo.deleteAll();
    }


    public Mono<User> update(User user) {
        return userRepo
                .findById(user.getId())
                .switchIfEmpty(exceptions.userNotFoundException())
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
                .switchIfEmpty(exceptions.userNotFoundException())
                .flatMapMany((userFound) -> {
                    var id = userFound.getId();
                    return postRepo.findPostsByAuthor_Id(id);
                });
    }
}
