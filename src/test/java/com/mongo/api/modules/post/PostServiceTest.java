package com.mongo.api.modules.post;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.modules.comment.CommentService;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserService;
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
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static config.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.BlockhoundUtils.bhWorks;
import static config.utils.TestUtils.*;
import static java.util.Collections.singletonList;


// @Import Annotation:
// - IMPORTS:
//   * The 'main class' must be imported "AND" (Ex.: UserService)
//   * The 'main-class's dependencies' must be imported "AS WELL"
//     (Ex.: PostService, CommentService, CustomExceptions, CustomExceptionsProperties, ModelMapper)
@Import({
     TestDbConfig.class,
     PostService.class,
     UserService.class,
     CommentService.class,
     ModelMapper.class,
})
@DisplayName("PostServiceTest")
@MergedRepo
class PostServiceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";

  private Post post1;
  private Post post2;
  private User author;

  @Autowired
  IPostService postService;

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

    author = userWithID_IdPostsEmpty().create();
    Flux<User> userFlux = dbUtils.saveUserList(singletonList(author));
    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    post1 = post_IdNull_CommentsEmpty(author).create();
    post2 = post_IdNull_CommentsEmpty(author).create();
    Post post3 = post_IdNull_CommentsEmpty(author).create();
    List<Post> postList = Arrays.asList(post1,post3);
    Flux<Post> postFlux = dbUtils.savePostList(postList);
    dbUtils.countAndExecutePostFlux(postFlux,2);

  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  void bHWorks() {
    bhWorks();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAll")
  void FindAll() {
    StepVerifier
         .create(postService.findAll())
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  void FindById() {
    StepVerifier
         .create(postService.findById(post1.getPostId()))
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("findPostsByAuthor_Id")
  void findPostsByAuthor_Id() {
    StepVerifier
         .create(postService.findPostsByAuthor_Id(author.getId()))
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindPostByIdShowComments")
  void FindPostByIdShowComments() {
    StepVerifier
         .create(postService.findPostByIdShowComments(post1.getPostId()))
         .expectSubscription()
         .expectNextMatches(post1 -> post1.getTitle()
                                          .equals(post1.getTitle()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindUserByPostId")
  void FindUserByPostId() {
    StepVerifier
         .create(postService.findUserByPostId(post1.getPostId()))
         .expectSubscription()
         .expectNextMatches(user1 -> user1.getId()
                                          .equals(author.getId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Save")
  void Save() {
    StepVerifier
         .create(postService.save(post2))
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    StepVerifier
         .create(postService.findById(post2.getPostId()))
         .expectSubscription()
         .expectNextMatches(post1 -> post1.getTitle()
                                          .equals(post2.getTitle()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Delete")
  void Delete() {
    StepVerifier
         .create(postService.delete(post1))
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(postService.findById(post1.getPostId()))
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Update")
  void Update() {
    String newTitle = Faker.instance()
                           .book()
                           .title();

    post1.setTitle(newTitle);

    StepVerifier
         .create(postService.update(post1))
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    StepVerifier
         .create(postService.findById(post1.getPostId()))
         .expectSubscription()
         .expectNextMatches(post1 -> post1.getTitle()
                                          .equals(newTitle))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("DeleteAll")
  void DeleteAll() {

    StepVerifier
         .create(postService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(postService.findAll())
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();
  }

}