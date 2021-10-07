package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.IPostRepo;
import com.mongo.api.modules.post.Post;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static testsconfig.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static testsconfig.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static testsconfig.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static testsconfig.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static testsconfig.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static testsconfig.utils.TestUtils.*;

@DisplayName("RepoTests")
@MergedRepo
public class RepoTests {

  //STATIC: one service for ALL tests -> SUPER FASTER
  //NON-STATIC: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";
  private User user1, user2WithId, user3;
  private Post post1, post2;
  private Flux<User> userFlux;

  @Autowired
  private IUserRepo userRepo;

  @Autowired
  private IPostRepo postRepo;


  @BeforeAll
  public static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
    globalComposeServiceContainerMessage(compose,
                                         TC_COMPOSE_SERVICE,
                                         TC_COMPOSE_SERVICE_PORT
                                        );
  }


  @AfterAll
  public static void afterAll(TestInfo testInfo) {
    globalAfterAll();
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-start");

    user1 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    user3 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    List<User> userList = Arrays.asList(user1,user3);
    userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);
  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  private void cleanDbToTest() {
    StepVerifier
         .create(userRepo.deleteAll())
         .expectSubscription()
         .verifyComplete();

    System.out.println("\n\n==================> CLEANING-DB-TO-TEST" +
                            " <==================\n\n");
  }


  private Flux<User> cleanDb_Saving02Users_GetThemInAFlux(List<User> userList) {
    return userRepo.deleteAll()
                   .thenMany(Flux.fromIterable(userList))
                   .flatMap(userRepo::save)
                   .doOnNext(item -> userRepo.findAll())
                   .doOnNext((item -> System.out.println(
                        "\n>>>>>>>>>>>>>>>Repo - UserID: " + item.getId() +
                             "|Name: " + item.getName() +
                             "|Email: " + item.getEmail())));
  }


  private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
    return postRepo.deleteAll()
                   .thenMany(Flux.fromIterable(postList))
                   .flatMap(postRepo::save)
                   .doOnNext(item -> postRepo.findAll())
                   .doOnNext((item -> System.out.println(
                        "\n>>>>>>>>>>>>>>>>>> Repo post - Post-ID: " + item.getPostId() +
                             "|Author: " + item.getAuthor()
                                                        )));
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Find: PostsByUserId")
  public void findPostByUserId() {

    user2WithId = userWithID_IdPostsEmpty().createTestUser();

    post1 = post_IdNull_CommentsEmpty(user2WithId).create();
    post2 = post_IdNull_CommentsEmpty(user2WithId).create();
    List<Post> postList = Arrays.asList(post1,post2);

    cleanDbToTest();

    StepVerifier
         .create(userRepo.save(user2WithId))
         .expectSubscription()
         .expectNext(user2WithId)
         .verifyComplete();

    StepVerifier
         .create(userRepo.findAll())
         .expectSubscription()
         .expectNextMatches(user -> user2WithId.getId()
                                               .equals(user.getId()))
         .verifyComplete();

    Flux<Post> postFlux = cleanDb_Saving02Posts_GetThemInAFlux(postList);

    StepVerifier
         .create(postFlux)
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();

    Flux<Post> postFluxByUserID = postRepo.findPostsByAuthor_Id(user2WithId.getId());

    StepVerifier
         .create(postFluxByUserID)
         .expectSubscription()
         .expectNextCount(2)
         .verifyComplete();

    StepVerifier
         .create(postRepo.findAll())
         .expectSubscription()
         .expectNextMatches(post -> post1.getPostId()
                                         .equals(post.getPostId()))
         .expectNextMatches(post -> post2.getPostId()
                                         .equals(post.getPostId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Save: Objects")
  public void save_obj() {
    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Find: Objects")
  public void findAll() {
    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Delete: Count")
  public void deleteAll_count() {
    StepVerifier
         .create(userRepo.deleteAll())
         .expectSubscription()
         .verifyComplete();

    Flux<User> userFlux = userRepo.findAll();

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0)
         .verifyComplete();

  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("DeleteById")
  public void deleteById() {
    StepVerifier
         .create(userFlux)
         .expectNext(user1)
         .expectNext(user3)
         .verifyComplete();

    Mono<Void> deletedItem =
         userRepo.findById(user1.getId())
                 .map(User::getId)
                 .flatMap(id -> userRepo.deleteById(id));

    StepVerifier
         .create(deletedItem.log())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(userRepo.findAll()
                         .log("The new item list : "))
         .expectSubscription()
         .expectNextCount(1)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  public void findById() {
    StepVerifier
         .create(userFlux)
         .expectNextCount(2L)
         .verifyComplete();

    Mono<User> itemFoundById =
         userRepo
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
  @DisplayName("Update")
  public void update() {
    StepVerifier
         .create(userFlux)
         .expectNextCount(2L)
         .verifyComplete();

    var newName = Faker.instance()
                       .name()
                       .fullName();

    Mono<User> updatedItem =
         userRepo
              .findById(user1.getId())
              .map(itemFound -> {
                itemFound.setName(newName);
                return itemFound;
              })
              .flatMap(itemToBeUpdated -> userRepo.save(itemToBeUpdated));

    StepVerifier
         .create(updatedItem)
         .expectSubscription()
         .expectNextMatches(user -> user.getName()
                                        .equals(newName))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
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

}