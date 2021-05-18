package com.mongo.api.modules.comment;

import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@AllArgsConstructor
@Service
public class CommentService {

    private final PostRepo postRepo;

    private final CommentRepo commentRepo;

    private final UserRepo userRepo;

    @Autowired
    private final CustomExceptions exceptions;


    public Flux<Comment> findAll() {
        return commentRepo.findAll();
    }


    public Mono<Comment> findCommentById(String id) {
        return commentRepo.findById(id)
                          .switchIfEmpty(exceptions.commentNotFoundException());
    }


    public Mono<User> findUserByCommentId(String id) {
        return commentRepo
                .findById(id)
                .switchIfEmpty(exceptions.commentNotFoundException())
                .flatMap(comment -> {
                    String idUser = comment.getAuthor()
                                           .getId();
                    return userRepo.findById(idUser);
                });
    }


    public Mono<Post> saveCommentLinkedObject(Comment comment) {
        return userRepo
                .findById(comment.getAuthor()
                                 .getId())
                .switchIfEmpty(exceptions.userNotFoundException())
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getIdPost())
                                         .switchIfEmpty(exceptions.postNotFoundException())
                                         .flatMap(postFound ->
                                                  {
                                                      postFound.getIdComments()
                                                               .add(commentSaved.getId());
                                                      return postRepo.save(postFound);
                                                  }
                                                 ));
    }


    public Mono<Post> saveCommentEmbedObject(Comment comment) {
        return userRepo
                .findById(comment.getAuthor()
                                 .getId())
                .switchIfEmpty(exceptions.userNotFoundException())
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getIdPost())
                                         .switchIfEmpty(exceptions.postNotFoundException())
                                         .flatMap(postFound ->
                                                  {
                                                      postFound.setComment(commentSaved);
                                                      return postRepo.save(postFound);
                                                  }
                                                 ));
    }


    public Mono<Post> saveCommentEmbedObjectList(Comment comment) {
        return userRepo
                .findById(comment.getAuthor()
                                 .getId())
                .switchIfEmpty(exceptions.userNotFoundException())
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getIdPost())
                                         .switchIfEmpty(exceptions.postNotFoundException())
                                         .flatMap(postFound ->
                                                  {
                                                      postFound.getListComments()
                                                               .add(commentSaved);
                                                      return postRepo.save(postFound);
                                                  }
                                                 ));
    }


    public Mono<Void> delete(Comment comment) {
        return commentRepo
                .findById(comment.getId())
                .switchIfEmpty(exceptions.commentNotFoundException())
                .flatMap(comment1 -> commentRepo.deleteById(comment.getId()));
    }


    public Mono<Comment> update(Comment comment) {
        return commentRepo
                .findById(comment.getId())
                .switchIfEmpty(exceptions.commentNotFoundException())
                .flatMap(comment1 -> {
                    comment1.setId(comment.getId());
                    comment1.setAuthor(comment.getAuthor());
                    comment1.setText(comment.getText());
                    comment1.setDate(comment.getDate());
                    return commentRepo.save(comment1);
                });
    }


    public Flux<Comment> findCommentsByPostId(String postId) {
        return postRepo
                .findById(postId)
                .switchIfEmpty(exceptions.postNotFoundException())
                .thenMany(commentRepo.findAll())
                .filter(commentsOfThePost -> commentsOfThePost.getIdPost()
                                            .equals(postId));

    }
}
