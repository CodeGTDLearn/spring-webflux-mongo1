package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.Post;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import testsconfig.annotations.MergedRepo;
import testsconfig.testcontainer.TcComposeConfig;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static testsconfig.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static testsconfig.utils.TestUtils.*;

@Import({UserService.class})
@DisplayName("ServiceTests")
@MergedRepo
public class ServiceTests {

  //STATIC: one service for ALL tests
  //NON-STATIC: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";
  private User user1, user3, userWithIdForPost1Post2;
  private Post post1, post2;
  private List<User> userList;

  @Autowired
  private IUserService userService;

  //  @Autowired
  //  private IPostService postService;
  //
  //  @Autowired
  //  private ICommentService commentService;

  //  @Autowired
  //  private ModelMapper modelMapper;


  @BeforeAll
  public static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
//    globalComposeServiceContainerMessage(compose,
//                                         TC_COMPOSE_SERVICE,
//                                         TC_COMPOSE_SERVICE_PORT
//                                        );
  }


  @AfterAll
  public static void afterAll(TestInfo testInfo) {
    globalAfterAll();
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  void beforeEach() {
    user1 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    user3 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    userList = Arrays.asList(user1,user3);
  }


  void cleanDbToTest() {
    StepVerifier
         .create(userService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    //    StepVerifier
    //         .create(postService.deleteAll())
    //         .expectSubscription()
    //         .verifyComplete();
    //
    //    StepVerifier
    //         .create(commentService.deleteAll())
    //         .expectSubscription()
    //         .verifyComplete();

    System.out.println("\n>==================================================>" +
                            "\n>===============> CLEAN-DB-TO-TEST >===============>" +
                            "\n>==================================================>\n");
  }


  @NotNull
  private Flux<User> cleanDb_Saving02Users_GetThemInAFlux(List<User> userList) {
    return userService.deleteAll()
                      .thenMany(Flux.fromIterable(userList))
                      .flatMap(userService::save)
                      .thenMany(userService.findAll())
                      .flatMap((user2 -> {
                        System.out.println(
                             "\nSaving 'User' in DB:" +
                                  "\n -> ID: " + user2.getId() +
                                  "\n -> Name: " + user2.getName() +
                                  "\n -> Email: " + user2.getEmail() + "\n");
                        return Mono.just(user2);
                      }));
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAll")
  void findAll() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(2)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  void findById() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectNext(user1)
         .expectNext(user3)
         .verifyComplete();

    Mono<User> itemFoundById =
         userService
              .findById(user1.getId())
              .map(itemFound -> itemFound);

    StepVerifier
         .create(itemFoundById)
         .expectSubscription()
         .expectNextMatches(found -> found.getId()
                                          .equals(user1.getId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Save: Object")
  void save() {
    cleanDbToTest();

    StepVerifier
         .create(userService.save(user3))
         .expectSubscription()
         .expectNext(user3)
         .verifyComplete();

  }


  @DisplayName("Delete: Count")
  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void deleteAll_count() {

    StepVerifier
         .create(userService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    Flux<User> fluxTest = userService.findAll();

    StepVerifier
         .create(fluxTest)
         .expectSubscription()
         .expectNextCount(0)
         .verifyComplete();

  }


  @DisplayName("DeleteById")
  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void deleteById() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectNext(user1)
         .expectNext(user3)
         .verifyComplete();

    Mono<Void> deletedItem =
         userService.findById(user1.getId())
                    .map(User::getId)
                    .flatMap(id -> userService.delete(id));

    StepVerifier
         .create(deletedItem.log())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(userService.findAll()
                            .log("The new item list : "))
         .expectSubscription()
         .expectNextCount(1)
         .verifyComplete();
  }


  @DisplayName("update")
  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void update() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectNext(user1)
         .expectNext(user3)
         .verifyComplete();

    var newName = Faker.instance()
                       .name()
                       .fullName();

    Mono<User> updatedItem =
         userService
              .findById(user1.getId())
              .map(itemFound -> {
                itemFound.setName(newName);
                return itemFound;
              })
              .flatMap(itemToBeUpdated -> userService.save(itemToBeUpdated));

    StepVerifier
         .create(updatedItem)
         .expectSubscription()
         .expectNextMatches(user -> user.getName()
                                        .equals(newName))
         .verifyComplete();

    StepVerifier
         .create(userService.findAll()
                            .log("The new item list : "))
         .expectSubscription()
         .expectNextCount(2)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = "true", loadContext = true)
  @DisplayName("BHWorks")
  public void bHWorks() {
    try {
      FutureTask<?> task = new FutureTask<>(() -> {
        Thread.sleep(0);
        return "";
      });

      Schedulers.parallel()
                .schedule(task);

      task.get(10,TimeUnit.SECONDS);
      Assertions.fail("should fail");
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      Assertions.assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
    }
  }


  //  @DisplayName("findPostsByUserId")
  //  @Test
  //  @EnabledIf(expression = enabledTest, loadContext = true)
  //  void findPostsByUserId() {
  //    userWithIdForPost1Post2 = userWithID_IdPostsEmpty().createTestUser();
  //
  //    post1 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
  //    post2 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
  //    List<Post> postList = Arrays.asList(post1,post2);
  //
  //    cleanDbToTest();
  //
  //    StepVerifier
  //         .create(userService.save(userWithIdForPost1Post2))
  //         .expectSubscription()
  //         .expectNext(userWithIdForPost1Post2)
  //         .verifyComplete();
  //
  //    StepVerifier
  //         .create(userService.findAll())
  //         .expectSubscription()
  //         .expectNextMatches(user -> userWithIdForPost1Post2.getId()
  //                                                           .equals(user.getId()))
  //         .verifyComplete();
  //
  //    Flux<Post> postFluxPost1Post2 = cleanDb_Saving02Posts_GetThemInAFlux(postList);
  //
  //    StepVerifier
  //         .create(postFluxPost1Post2)
  //         .expectSubscription()
  //         .expectNextCount(2)
  //         .verifyComplete();
  //
  //    Flux<Post> postFluxPost1Post2ByUserID = postService.findPostsByAuthorId(
  //         userWithIdForPost1Post2.getId());
  //
  //    StepVerifier
  //         .create(postFluxPost1Post2ByUserID)
  //         .expectSubscription()
  //         .expectNextCount(2)
  //         .verifyComplete();
  //
  //    StepVerifier
  //         .create(postService.findAll())
  //         .expectSubscription()
  //         .expectNextMatches(post -> post1.getPostId()
  //                                         .equals(post.getPostId()))
  //         .expectNextMatches(post -> post2.getPostId()
  //                                         .equals(post.getPostId()))
  //         .verifyComplete();
  //  }
  //
  //
  //  @Test
  //  @EnabledIf(expression = enabledTest, loadContext = true)
  //  void findAllShowAllDto() {
  //
  //    User user = userWithID_IdPostsEmpty().createTestUser();
  //    Post post = postFull_CommentsEmpty(user).create();
  //    Comment comment = comment_simple(post).create();
  //    UserAllDto userShowAll = userShowAll_Test(user,
  //                                              post,
  //                                              comment
  //                                             ).createTestUserShowAll();
  //    cleanDbToTest();
  //
  //    StepVerifier
  //         .create(saveUserShowAllFinalInDb(user,post,comment))
  //         .expectSubscription()
  //         .verifyComplete();
  //
  //    StepVerifier
  //         .create(userService.findAllShowAllDto())
  //         .expectSubscription()
  //         .expectNextMatches(user1 -> userShowAll.getId()
  //                                                .equals(user1.getId()))
  //         .expectNextMatches(user1 ->
  //                                 userShowAll.getPosts()
  //                                            .get(0)
  //                                            .getPostId()
  //                                            .equals(user1.getPosts()
  //                                                         .get(0)
  //                                                         .getPostId()))
  //         .expectNextMatches(user1 ->
  //                                 userShowAll.getPosts()
  //                                            .get(0)
  //                                            .getListComments()
  //                                            .get(0)
  //                                            .getCommentId()
  //                                            .equals(user1.getPosts()
  //                                                         .get(0)
  //                                                         .getListComments()
  //                                                         .get(0)
  //                                                         .getCommentId()))
  //         .verifyComplete();
  //  }

  //  @NotNull
  //  private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
  //    return postService.deleteAll()
  //                      .thenMany(Flux.fromIterable(postList))
  //                      .flatMap(postService::save)
  //                      .doOnNext(item -> postService.findAll())
  //                      .doOnNext((item -> System.out.println(
  //                           "\nUserRepo - Post-ID: " + item.getPostId() +
  //                                "|Author: " + item.getAuthor() + "\n")));
  //  }


  //  private Mono<Void> saveUserShowAllFinalInDb(User user,Post post,Comment comment) {
  //    return userService.deleteAll()
  //                      .then(Mono.just(user))
  //                      .flatMap(userService::save)
  //                      .flatMap((user2 -> {
  //                        System.out.println(
  //                             "\nSaving 'User' in DB:" +
  //                                  "\n -> ID: " + user2.getId() +
  //                                  "\n -> Name: " + user2.getName() +
  //                                  "\n -> Email: " + user2.getEmail() + "\n");
  //                        return Mono.just(user2);
  //                      }))
  //                      .then(postService.deleteAll())
  //                      .thenMany(userService.findAll())
  //                      .flatMap(user2 -> {
  //                        UserAuthorDto authorDto = modelMapper.map(user2,UserAuthorDto.class);
  //                        post.setAuthor(authorDto);
  //                        return postService.save(post);
  //                      })
  //                      .flatMap((post2 -> {
  //                        System.out.println(
  //                             "\nSaving 'Post' in DB:" +
  //                                  "\n -> Post-ID: " + post2.getPostId() +
  //                                  "\n -> Post-Title: " + post2.getTitle() + "\n" +
  //                                  "\n -> Author-ID: " + post2.getAuthor()
  //                                                             .getId() +
  //                                  "\n -> Author-Name: " + post2.getAuthor()
  //                                                               .getName() + "\n");
  //                        return Mono.just(post2);
  //                      }))
  //                      .then(commentService.deleteAll())
  //                      .thenMany(postService.findAll())
  //                      .flatMap(post2 -> {
  //                        comment.setPostId(post2.getPostId());
  //                        comment.setAuthor(post2.getAuthor());
  //                        return commentService.saveLinkedObject(comment);
  //                      })
  //                      .flatMap((comment2 -> {
  //                        System.out.println(
  //                             "\nSaving 'Comment' in DB:" +
  //                                  "\n -> ID: " + comment2.getCommentId() +
  //                                  "\n -> PostId: " + comment2.getPostId() +
  //                                  "\n -> Author: " + comment2.getAuthor() + "\n");
  //                        return Mono.just(comment2);
  //                      }))
  //                      .then()
  //
  //         ;
  //  }
}