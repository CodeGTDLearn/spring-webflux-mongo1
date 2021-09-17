package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.IPostRepo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import utils.testcontainer.container.ConfigContainerTests;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static utils.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static utils.databuilders.UserBuilder.userWithID_IdPostsEmpty;

public class RepoTests extends ConfigContainerTests {

  final String enabledTest = "true";
  private User user1, user2WithId, user3;
  private Post post1, post2;
  private List<User> userList;

  @Autowired
  private IUserRepo userRepo;

  @Autowired
  private IPostRepo postRepo;


  @BeforeAll
  public static void beforeAll() {
    ConfigContainerTests.beforeAll();
  }


  @AfterAll
  public static void afterAll() {
    ConfigContainerTests.afterAll();
  }


  @BeforeEach
  void beforeEach() {

    user1 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    user3 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    userList = Arrays.asList(user1,user3);
  }


  private void cleanDbToTest() {
    StepVerifier
         .create(userRepo.deleteAll())
         .expectSubscription()
         .verifyComplete();

    System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                            " <==================\n\n");
  }


  @NotNull
  private Flux<User> cleanDb_Saving02Users_GetThemInAFlux(List<User> userList) {
    return userRepo.deleteAll()
                   .thenMany(Flux.fromIterable(userList))
                   .flatMap(userRepo::save)
                   .doOnNext(item -> userRepo.findAll())
                   .doOnNext((item -> System.out.println(
                        "\nRepo - UserID: " + item.getId() +
                             "|Name: " + item.getName() +
                             "|Email: " + item.getEmail())));
  }


  @NotNull
  private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
    return postRepo.deleteAll()
                   .thenMany(Flux.fromIterable(postList))
                   .flatMap(postRepo::save)
                   .doOnNext(item -> postRepo.findAll())
                   .doOnNext((item -> System.out.println(
                        "\nRepo - Post-ID: " + item.getPostId() +
                             "|Author: " + item.getAuthor()
                                                        )));
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Check Container")
  void checkContainer() {
    assertTrue(sharedContainer.isRunning());
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
         .expectNext(post1)
         .expectNext(post2)
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
  @DisplayName("Save: Object")
  public void save_obj() {
    cleanDbToTest();

    StepVerifier
         .create(userRepo.save(user3))
         .expectSubscription()
         .expectNext(user3)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Find: Count")
  public void find_count() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(2)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Find: Objects")
  public void findAll_1() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextMatches(customer -> user1.getEmail()
                                             .equals(customer.getEmail()))
         .expectNextMatches(customer -> user3.getEmail()
                                             .equals(customer.getEmail()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Find: Objects")
  public void findAll_2() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectNext(user1)
         .expectNext(user3)
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

    Flux<User> fluxTest = userRepo.findAll();

    StepVerifier
         .create(fluxTest)
         .expectSubscription()
         .expectNextCount(0)
         .verifyComplete();

  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("DeleteById")
  public void deleteById() {
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

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
    final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

    StepVerifier
         .create(userFlux)
         .expectNext(user1)
         .expectNext(user3)
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

    StepVerifier
         .create(userRepo.findAll()
                         .log("The new item list : "))
         .expectSubscription()
         .expectNextCount(2)
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