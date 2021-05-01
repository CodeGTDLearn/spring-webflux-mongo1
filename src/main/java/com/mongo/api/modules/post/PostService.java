package com.mongo.api.modules.post;

import com.mongo.api.modules.comment.CommentService;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.exceptions.customExceptions.simple.Messages.postAuthorNotFoundException;
import static com.mongo.api.core.exceptions.customExceptions.simple.Messages.postNotFoundException;

@Slf4j
@AllArgsConstructor
@Service
public class PostService {

    private final PostRepo postRepo;

    private final UserRepo userRepo;

    private final CommentService commentService;

    public Mono<Post> findPostById(String id) {
        return postRepo.findById(id)
                       .switchIfEmpty(postNotFoundException());
    }

    public Mono<Post> findPostByIdShowComments(String id) {
        return postRepo
                .findById(id)
                .switchIfEmpty(postNotFoundException())
                .flatMap(postFound -> commentService
                                 .findCommentsByPostId(postFound.getId())
                                 .collectList()
                                 .flatMap(comments -> {
                                     postFound.setListComments(comments);
                                     return Mono.just(postFound);
                                 })
                        );
    }

    public Mono<User> findUserByPostId(String id) {
        return postRepo
                .findById(id)
                .flatMap(item -> {
                    String idUser = item.getAuthor()
                                        .getId();
                    return userRepo.findById(idUser);
                });
    }

    public Flux<Post> findAll() {
        return postRepo.findAll();
    }

    public Mono<Post> saveEmbedObject(Post post) {
        return userRepo
                .findById(post.getAuthor()
                              .getId())
                .switchIfEmpty(postAuthorNotFoundException())
                .then(postRepo.save(post))
                .flatMap(postSaved -> {
                    Mono<User> userMono = findUserByPostId(postSaved.getId());
                    userMono.map(userPost -> {
                        userPost.getIdPosts()
                                .add(postSaved.getId());
                        return userRepo.save(userPost);
                    });
                    return userMono;
                })
                .flatMap(user -> findPostById(user.getId()));
    }

    public Mono<Void> delete(Post post) {
        return postRepo
                .findById(post.getId())
                .switchIfEmpty(postNotFoundException())
                .flatMap(item -> postRepo.deleteById(post.getId()));
    }

    public Mono<Void> deleteAll() {
        return postRepo.deleteAll();
    }

    public Mono<Post> update(Post post) {
        return postRepo
                .findById(post.getId())
                .switchIfEmpty(postNotFoundException())
                .flatMap(item -> {
                    item.setId(post.getId());
                    item.setIdComments(post.getIdComments());
                    item.setAuthor(post.getAuthor());
                    item.setBody(post.getBody());
                    item.setDate(post.getDate());
                    item.setTitle(post.getTitle());
                    return postRepo.save(item);
                });
    }
}
