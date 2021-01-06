package com.mongo.api.modules.comment;

import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.exceptions.ExceptionTriggers.*;

@AllArgsConstructor
@Service
public class CommentService {

    private final PostRepo postRepo;

    private final CommentRepo commentRepo;

    private final UserRepo userRepo;

    public Flux<Comment> findAll() {
        return commentRepo.findAll();
    }

    public Mono<Comment> findCommentById(String id) {
        return commentRepo.findById(id)
                          .switchIfEmpty(postNotFoundException());
    }

    public Mono<User> findUserByCommentId(String id) {
        return commentRepo
                .findById(id)
                .switchIfEmpty(commentNotFoundException())
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
                .switchIfEmpty(userNotFoundException())
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getIdPost())
                                         .switchIfEmpty(postNotFoundException())
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
                .switchIfEmpty(userNotFoundException())
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getIdPost())
                                         .switchIfEmpty(postNotFoundException())
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
                .switchIfEmpty(userNotFoundException())
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getIdPost())
                                         .switchIfEmpty(postNotFoundException())
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
                .switchIfEmpty(commentNotFoundException())
                .flatMap(comment1 -> commentRepo.deleteById(comment.getId()));
    }

    public Mono<Comment> update(Comment comment) {
        return commentRepo
                .findById(comment.getId())
                .switchIfEmpty(commentNotFoundException())
                .flatMap(comment1 -> {
                    comment1.setId(comment.getId());
                    comment1.setAuthor(comment.getAuthor());
                    comment1.setText(comment.getText());
                    comment1.setDate(comment.getDate());
                    return commentRepo.save(comment1);
                });
    }

    public Flux<Comment> findCommentsByPostId(String id) {
        return postRepo
                .findById(id)
                .switchIfEmpty(postNotFoundException())
                .thenMany(commentRepo.findAll())
                .filter(comment1 -> comment1.getIdPost()
                                            .equals(id));

    }
}
