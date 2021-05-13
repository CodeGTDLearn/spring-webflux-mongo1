package com.mongo.api.modules.post;

import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.modules.comment.CommentService;
import com.mongo.api.modules.post.entity.Post;
import com.mongo.api.modules.user.UserRepo;
import com.mongo.api.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@Service
public class PostService implements PostServiceInt {

    private final PostRepo postRepo;

    private final UserRepo userRepo;

    private final CommentService commentService;

    private final CustomExceptions exceptions;


    @Override
    public Mono<Post> findPostById(String id) {
        return postRepo.findById(id)
                       .switchIfEmpty(exceptions.postNotFoundException());
    }


    @Override
    public Mono<Post> findPostByIdShowComments(String id) {
        return postRepo
                .findById(id)
                .switchIfEmpty(exceptions.postNotFoundException())
                .flatMap(postFound -> commentService
                                 .findCommentsByPostId(postFound.getId())
                                 .collectList()
                                 .flatMap(comments -> {
                                     postFound.setListComments(comments);
                                     return Mono.just(postFound);
                                 })
                        );
    }


    @Override
    public Mono<User> findUserByPostId(String id) {
        return postRepo
                .findById(id)
                .switchIfEmpty(exceptions.postNotFoundException())
                .flatMap(item -> {
                    String idUser = item.getAuthor()
                                        .getId();
                    return userRepo.findById(idUser)
                                   .switchIfEmpty(exceptions.userNotFoundException());
                });
    }


    @Override
    public Flux<Post> findAll() {
        return postRepo.findAll();
    }


    @Override
    public Mono<Post> save(Post post) {
        return userRepo
                .findById(post.getAuthor()
                              .getId())

                .switchIfEmpty(exceptions.authorNotFoundException())

                .then(postRepo.save(post))

                .flatMap(postSaved -> {
                    return userRepo
                            .findById(postSaved.getAuthor()
                                               .getId())
                            .flatMap(user -> {
                                user.getIdPosts()
                                    .add(postSaved.getId());
                                return userRepo.save(user);
                            });
                })
                .flatMap(user -> postRepo.findById(user.getIdPosts()
                                           .get(user.getIdPosts()
                                               .size() - 1)));
    }


    @Override
    public Mono<Void> delete(Post post) {
        return postRepo
                .findById(post.getId())
                .switchIfEmpty(exceptions.postNotFoundException())
                .flatMap(item -> postRepo.deleteById(post.getId()));
    }


    @Override
    public Mono<Void> deleteAll() {
        return postRepo.deleteAll();
    }


    @Override
    public Mono<Post> update(Post post) {
        return postRepo
                .findById(post.getId())
                .switchIfEmpty(exceptions.postNotFoundException())
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
