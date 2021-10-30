package config.utils;

import com.mongo.api.core.dto.UserAuthorDto;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.comment.ICommentService;
import com.mongo.api.modules.post.IPostService;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
public class TestDbUtils {

  @Autowired
  private IUserService userService;

  @Autowired
  private IPostService postService;

  @Autowired
  private ICommentService commentService;


  public void countAndExecuteUserFlux(Flux<User> flux,int totalElements) {
    StepVerifier
         .create(flux)
         .expectSubscription()
         .expectNextCount(totalElements)
         .verifyComplete();
  }


  public void countAndExecutePostFlux(Flux<Post> flux,int totalElements) {
    StepVerifier
         .create(flux)
         .expectSubscription()
         .expectNextCount(totalElements)
         .verifyComplete();
  }


  public Flux<User> saveUserList(List<User> userList) {
    return userService.deleteAll()
                      .thenMany(Flux.fromIterable(userList))
                      .flatMap(userService::save)
                      .doOnNext(item -> userService.findAll())
                      .doOnNext(item -> System.out.println(
                           "\n--> Saved 'User' in DB: \n    --> " + item.toString() + "\n"));
  }


  public Flux<Post> savePostList(List<Post> postList) {
    return postService.deleteAll()
                      .thenMany(Flux.fromIterable(postList))
                      .flatMap(postService::save)
                      .doOnNext(item -> postService.findAll())
                      .doOnNext(item -> System.out.println(
                           "\n--> Saved 'Post' in DB: \n    --> " + item.toString() + "\n"));
  }


  public void cleanTestDb() {
    StepVerifier
         .create(userService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(postService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(commentService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    System.out.println("\n>==================================================>" +
                            "\n>===============> CLEAN-DB-TO-TEST >===============>" +
                            "\n>==================================================>\n");
  }


  public Mono<Void> saveUserShowAllFinalInDb(User user,Post post,Comment comment) {
    return userService.deleteAll()
                      .then(Mono.just(user))
                      .flatMap(userService::save)
                      .flatMap((user2 -> {
                        System.out.println(
                             "\nSaving 'User' in DB:" +
                                  "\n -> ID: " + user2.getId() +
                                  "\n -> Name: " + user2.getName() +
                                  "\n -> Email: " + user2.getEmail() + "\n");
                        return Mono.just(user2);
                      }))
                      .then(postService.deleteAll())
                      .thenMany(userService.findAll())
                      .flatMap(user2 -> {
                        UserAuthorDto authorDto = new ModelMapper().map(user2,UserAuthorDto.class);
                        post.setAuthor(authorDto);
                        return postService.save(post);
                      })
                      .flatMap((post2 -> {
                        System.out.println(
                             "\nSaving 'Post' in DB:" +
                                  "\n -> Post-ID: " + post2.getPostId() +
                                  "\n -> Post-Title: " + post2.getTitle() + "\n" +
                                  "\n -> Author-ID: " + post2.getAuthor()
                                                             .getId() +
                                  "\n -> Author-Name: " + post2.getAuthor()
                                                               .getName() + "\n");
                        return Mono.just(post2);
                      }))
                      .then(commentService.deleteAll())
                      .thenMany(postService.findAll())
                      .flatMap(post2 -> {
                        comment.setPostId(post2.getPostId());
                        comment.setAuthor(post2.getAuthor());
                        return commentService.saveLinkedObject(comment);
                      })
                      .flatMap((comment2 -> {
                        System.out.println(
                             "\nSaving 'Comment' in DB:" +
                                  "\n -> ID: " + comment2.getCommentId() +
                                  "\n -> PostId: " + comment2.getPostId() +
                                  "\n -> Author: " + comment2.getAuthor() + "\n");
                        return Mono.just(comment2);
                      }))
                      .then()

         ;
  }
}




