package com.mongo.api.modules.comment;

import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserServiceInt;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@AllArgsConstructor
public class CommentService implements CommentServiceInt {

    private final CommentRepo commentRepo;

    @Lazy
    private final PostRepo postRepo;

    @Lazy
    private final UserServiceInt userService;

    private final ModelMapper conv;

    private final CustomExceptions exceptions;


    @Override
    public Flux<Comment> findAll() {
        return commentRepo.findAll();
    }


    @Override
    public Mono<Comment> findById(String id) {
        return commentRepo.findById(id)
                          .switchIfEmpty(exceptions.commentNotFoundException());
    }


    @Override
    public Mono<User> findUserByCommentId(String id) {
        return commentRepo
                .findById(id)
                .switchIfEmpty(exceptions.commentNotFoundException())
                .flatMap(comment -> {
                    String idUser = comment.getAuthor()
                                           .getId();
                    //                    return userRepo.findById(idUser);
                    return userService.findById(idUser);
                });
    }


    @Override
    public Mono<Post> saveLinkedObject(Comment comment) {
        return userService
                .findById(comment.getAuthor().getId())
                .switchIfEmpty(exceptions.userNotFoundException())

                .then(postRepo.findById(comment.getPostId())
                              .switchIfEmpty(exceptions.postNotFoundException()))

                .then(commentRepo.save(comment))

                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getPostId())
                                         .switchIfEmpty(exceptions.postNotFoundException())
                                         .flatMap(postFound ->
                                                  {
                                                      postFound.getIdComments()
                                                               .add(commentSaved.getCommentId());
                                                      return postRepo.save(postFound);
//                                                      return Mono.just(postFound);
                                                  }
                                                 ));
    }


    @Override
    public Mono<Post> saveEmbedObjectSubst(Comment comment) {
        return userService
                .findById(comment.getAuthor()
                                 .getId())
                .switchIfEmpty(exceptions.userNotFoundException())

                .then(postRepo.findById(comment.getPostId()))
                .switchIfEmpty(exceptions.postNotFoundException())

                .then(commentRepo.save(comment))

                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getPostId())
                                         .switchIfEmpty(exceptions.postNotFoundException())
                                         .flatMap(postFound ->
                                                  {
                                                      postFound.setComment(commentSaved);
                                                      return postRepo.save(postFound);
                                                  }
                                                 ));
    }


    @Override
    public Mono<Post> saveEmbedObjectList(Comment comment) {
        return userService
                .findById(comment.getAuthor()
                                 .getId())
                .switchIfEmpty(exceptions.userNotFoundException())
                .then(postRepo.findById(comment.getPostId())
                              .switchIfEmpty(exceptions.postNotFoundException()))
                .then(commentRepo.save(comment))
                .flatMap(commentSaved ->
                                 postRepo
                                         .findById(commentSaved.getPostId())
                                         .switchIfEmpty(exceptions.postNotFoundException())
                                         .flatMap(postFound ->
                                                  {
                                                      postFound.getListComments()
                                                               .add(commentSaved);
                                                      return postRepo.save(postFound);
                                                  }
                                                 ));
    }


    @Override
    public Mono<Void> delete(Comment comment) {
        return commentRepo
                .findById(comment.getCommentId())
                .switchIfEmpty(exceptions.commentNotFoundException())
                .flatMap(comment1 -> commentRepo.deleteById(comment.getCommentId()));
    }


    @Override
    public Mono<Comment> update(Comment comment) {
        return commentRepo
                .findById(comment.getCommentId())
                .switchIfEmpty(exceptions.commentNotFoundException())
                //                .then(commentRepo.save(conv.map(comment,Comment.class)));
                .flatMap(comment1 -> {
//                    comment1.setId(comment.getId());
//                    comment1.setAuthor(comment.getAuthor());
//                    comment1.setText(comment.getText());
//                    comment1.setDate(comment.getDate());
//                    return commentRepo.save(comment1);

                    Comment userRet = conv.map(comment,Comment.class);
                    return commentRepo.save(userRet);
                });
    }


    @Override
    public Flux<Comment> findCommentsByPostId(String postId) {
        return postRepo
                .findById(postId)
                .switchIfEmpty(exceptions.postNotFoundException())
                .thenMany(commentRepo.findAll())
                .filter(commentsOfThePost -> commentsOfThePost.getPostId()
                                                              .equals(postId));

    }
}
