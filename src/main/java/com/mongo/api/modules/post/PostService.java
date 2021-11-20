package com.mongo.api.modules.post;

import com.mongo.api.modules.comment.ICommentService;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service("postService")
@AllArgsConstructor
public class PostService implements IPostService {

  private final IPostRepo postRepo;

  private final IUserService userService;

  private final ICommentService commentService;

  private final ModelMapper modelMapper;


  @Override
  public Flux<Post> findAll() {
    return postRepo
         .findAll()
         .flatMap(post -> commentService
                       .findCommentsByPostId(post.getPostId())
                       .collectList()
                       .flatMap(commentList -> {
                         post.setListComments(commentList);
                         return Mono.just(post);
                       })
                 )
         ;
  }


  @Override
  public Mono<Post> findById(String id) {
    return postRepo.findById(id);
  }


  @Override
  public Flux<Post> findPostsByAuthor_Id(String userId) {
    return userService
         .findById(userId)

         .flatMapMany((userFound) -> {
           var id = userFound.getId();
           return postRepo.findPostsByAuthor_Id(id);
         });
  }


  @Override
  public Mono<Post> findPostByIdShowComments(String id) {
    return postRepo
         .findById(id)

         .flatMap(postFound -> commentService
                       .findCommentsByPostId(postFound.getPostId())
                       .collectList()
                       .flatMap(comments -> {
                         postFound.setListComments(comments);
                         return Mono.just(postFound);
                       })
                 );
  }


  @Override
  public Mono<Post> save(Post post) {
    return
         postRepo.save(post)
                 .flatMap(postSaved -> userService
                      .findById(postSaved.getAuthor()
                                         .getId())
                      .flatMap(user -> {
                        user.getIdPosts()
                            .add(postSaved.getPostId());
                        return userService.update(user);
                      }))
                 .flatMap(user -> postRepo.findById(user.getIdPosts()
                                                        .get(user.getIdPosts()
                                                                 .size() - 1)));
  }


  @Override
  public Mono<Void> delete(Post post) {
    return postRepo
         .findById(post.getPostId())

         .flatMap(post1 -> commentService
                       .findCommentsByPostId(post1.getPostId())
                       .flatMap(commentService::delete)
                       .then(findUserByPostId(post1.getPostId()))
                       .flatMap(user -> {
                         user.getIdPosts()
                             .remove(post1.getPostId());
                         return userService.save(user);
                       })
                       .then(postRepo.findById(post1.getPostId()))
                       .flatMap(postRepo::delete)
                 );
  }


  @Override
  public Mono<Post> update(Post newPost) {
    return postRepo
         .findById(newPost.getPostId())

         .flatMap(post -> {
           Post updatedPost = modelMapper.map(newPost,Post.class);
           return postRepo.save(updatedPost);
         });
  }


  @Override
  public Mono<Void> deleteAll() {
    return postRepo.deleteAll();
  }


  @Override
  public Mono<User> findUserByPostId(String id) {
    return postRepo
         .findById(id)

         .flatMap(item -> {
           String idUser = item.getAuthor()
                               .getId();
           return userService
                .findById(idUser);
         });
  }
}