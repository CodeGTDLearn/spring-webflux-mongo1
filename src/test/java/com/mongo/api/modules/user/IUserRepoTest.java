package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.custom.CustomExceptionsThrower;
import com.mongo.api.core.exceptions.custom.CustomExceptionsCustomAttributes;
import com.mongo.api.modules.comment.CommentService;
import com.mongo.api.modules.post.IPostRepo;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostService;
import config.annotations.MergedRepo;
import config.testcontainer.TcComposeConfig;
import config.utils.TestDbUtils;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static config.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static config.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.BlockhoundUtils.bhWorks;
import static config.utils.TestUtils.*;

@Import({
     TestDbConfig.class,
     UserService.class,
     PostService.class,
     CommentService.class,
     CustomExceptionsThrower.class,
     CustomExceptionsCustomAttributes.class,
     ModelMapper.class,
})
@DisplayName("IUserRepoTest")
@MergedRepo
class IUserRepoTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";
  private User user1, user2WithId, user3;
  private Post post1, post2;
  private Flux<User> userFlux;

  @Autowired
  IUserRepo userRepo;

  @Autowired
  IPostRepo postRepo;

  @Autowired
  TestDbUtils dbUtils;


  @BeforeAll
  static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
    globalComposeServiceContainerMessage(compose,
                                         TC_COMPOSE_SERVICE,
                                         TC_COMPOSE_SERVICE_PORT
                                        );
  }


  @AfterAll
  static void afterAll(TestInfo testInfo) {
    globalAfterAll();
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-start");

    user1 = userFull_IdNull_ListIdPostsEmpty().create();
    user3 = userFull_IdNull_ListIdPostsEmpty().create();
    List<User> userList = Arrays.asList(user1,user3);
    userFlux = dbUtils.saveUserList(userList);
  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Find: PostsByUserId")
  public void findPostByUserId() {

    user2WithId = userWithID_IdPostsEmpty().create();

    post1 = post_IdNull_CommentsEmpty(user2WithId).create();
    post2 = post_IdNull_CommentsEmpty(user2WithId).create();
    List<Post> postList = Arrays.asList(post1,post2);

    dbUtils.cleanTestDb();

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

    Flux<Post> postFlux = dbUtils.savePostList(postList);

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
  void bHWorks() {
    bhWorks();
  }


}