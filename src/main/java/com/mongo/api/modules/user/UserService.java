package com.mongo.api.modules.user;

import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.exceptions.ExceptionTriggers.genericExcErrorUserNotFound;
import static com.mongo.api.core.exceptions.ExceptionTriggers.userNotFoundException;

@AllArgsConstructor
@Service
public class UserService {

    private UserRepo userRepo;

    private PostRepo postRepo;

    public Flux<User> findAll() {
        return userRepo.findAll();
    }

    public Mono<User> findById(String id) {
        return userRepo
                .findById(id)
                .switchIfEmpty(userNotFoundException());
    }

    public Flux<User> findErrorUserNotFound() {
        return userRepo
                .findAll()
                .concatWith(genericExcErrorUserNotFound());
    }

    public Mono<User> save(User user) {
        return userRepo.save(user);
    }

    public Mono<Void> deleteById(String id) {
        return userRepo
                .findById(id)
                .switchIfEmpty(userNotFoundException())
                .flatMap(userRepo::delete);
    }

    public Mono<Void> deleteAll() {
        return userRepo.deleteAll();
    }

    public Mono<User> update(User user) {
        return userRepo
                .findById(user.getId())
                .switchIfEmpty(userNotFoundException())
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
                .switchIfEmpty(userNotFoundException())
                .flatMapMany((userFound) -> {
                    var id = userFound.getId();
                    return postRepo.findPostsByAuthor_Id(id);
                });
    }
}
