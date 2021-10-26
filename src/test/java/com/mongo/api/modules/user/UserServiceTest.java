package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.core.testconfig.DbUtilsConfig;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.comment.CommentService;
import com.mongo.api.modules.comment.ICommentService;
import com.mongo.api.modules.post.IPostService;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostService;
import config.annotations.MergedRepo;
import config.testcontainer.TcComposeConfig;
import config.utils.DbUtils;
import config.utils.TestUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static config.databuilders.CommentBuilder.comment_simple;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static config.databuilders.UserBuilder.*;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.TestUtils.*;


// @Import Annotation:
// - IMPORTS:
//   * The 'main class' must be imported "AND"
//   * The 'main-class's dependencies' must be imported "AS WELL"
@Import({
     UserService.class,
     CustomExceptions.class,
     CustomExceptionsProperties.class,
     PostService.class,
     CommentService.class,
     ModelMapper.class,
     DbUtilsConfig.class,
     TestUtils.class})
@DisplayName("ServiceTests")
@MergedRepo
public class UserServiceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";
  private User user1, user3, userWithIdForPost1Post2;
  private Post post1, post2;
  private List<User> userList;

  @Autowired
  private DbUtils<User> userDbUtils;

  @Autowired
  private TestUtils testUtils;

  @Autowired
  private IUserService userService;

  @Autowired
  private IPostService postService;

  @Autowired
  private ICommentService commentService;

  @Autowired
  private ModelMapper modelMapper;


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
  void beforeEach() {
    user1 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    user3 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    userList = Arrays.asList(user1,user3);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAll")
  void findAll() {
    Flux<User> userFlux = userDbUtils.saveUserListAndGetFlux(userList,userService);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(2)
         .verifyComplete();

    List<User> emptyList = new ArrayList<>();

    userFlux = userDbUtils.saveUserListAndGetFlux(emptyList,userService);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  void findById() {
    final Flux<User> userFlux = userDbUtils.saveUserListAndGetFlux(userList,userService);

    StepVerifier
         .create(userFlux)
         .expectNextCount(2L)
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
    userDbUtils.cleanTestDb(userService,postService,commentService);

    StepVerifier
         .create(userService.save(user3))
         .expectSubscription()
         .expectNext(user3)
         .verifyComplete();

  }


  @DisplayName("Delete: Count")
  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void deleteAll() {

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
    final Flux<User> userFlux = userDbUtils.saveUserListAndGetFlux(userList,userService);

    StepVerifier
         .create(userFlux)
         .expectNextCount(2L)
         .verifyComplete();

    Mono<Void> deletedItem = userService.delete(userList.get(0)
                                                        .getId());

    StepVerifier
         .create(deletedItem.log())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(userService.findAll()
                            .log("The new item list : "))
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();
  }


  @DisplayName("update")
  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void update() {
    final Flux<User> userFlux = userDbUtils.saveUserListAndGetFlux(userList,userService);

    StepVerifier
         .create(userFlux)
         .expectNextCount(2L)
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
  }


  @Test
  @EnabledIf(expression = "true", loadContext = true)
  @DisplayName("BHWorks")
  public void bHWorks() {
    testUtils.bhWorks();
  }


  @DisplayName("findPostsByUserId")
  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findPostsByUserId() {
    userWithIdForPost1Post2 = userWithID_IdPostsEmpty().createTestUser();

    post1 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
    post2 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
    List<Post> postList = Arrays.asList(post1,post2);

    userDbUtils.cleanTestDb(userService,postService,commentService);

    StepVerifier
         .create(userService.save(userWithIdForPost1Post2))
         .expectSubscription()
         .expectNext(userWithIdForPost1Post2)
         .verifyComplete();

    StepVerifier
         .create(userService.findAll())
         .expectSubscription()
         .expectNextMatches(user -> userWithIdForPost1Post2.getId()
                                                           .equals(user.getId()))
         .verifyComplete();

    Flux<Post> postFluxPost1Post2 =
         userDbUtils.savePostListAndGetFlux(postList,postService);

    StepVerifier
         .create(postFluxPost1Post2)
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();

    Flux<Post> postFluxPost1Post2ByUserID = postService.findPostsByAuthorId(
         userWithIdForPost1Post2.getId());

    StepVerifier
         .create(postFluxPost1Post2ByUserID)
         .expectSubscription()
         .expectNextCount(2L)
         .verifyComplete();

    StepVerifier
         .create(postService.findAll())
         .expectSubscription()
         .expectNextMatches(post -> post1.getPostId()
                                         .equals(post.getPostId()))
         .expectNextMatches(post -> post2.getPostId()
                                         .equals(post.getPostId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findShowAllDto() {

    User user = userWithID_IdPostsEmpty().createTestUser();
    Post post = postFull_withId_CommentsEmpty(user).create();
    Comment comment = comment_simple(post).create();
    UserAllDto userShowAll = userShowAll_Test(user,
                                              post,
                                              comment
                                             ).createTestUserShowAll();

    StepVerifier
         .create(
              userDbUtils.saveUserShowAllFinalInDb(
                   user,post,comment,userService,postService,commentService))
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(userService.findAllShowAllDto())
         .expectSubscription()
         .expectNextMatches(user1 -> userShowAll.getId()
                                                .equals(user1.getId()))
         .verifyComplete();

    StepVerifier
         .create(userService.findAllShowAllDto())
         .expectSubscription()
         .expectNextMatches(user1 ->
                                 userShowAll.getPosts()
                                            .get(0)
                                            .getPostId()
                                            .equals(user1.getPosts()
                                                         .get(0)
                                                         .getPostId()))
         .verifyComplete();

    StepVerifier
         .create(userService.findAllShowAllDto())
         .expectSubscription()
         .expectNextMatches(user1 ->
                                 userShowAll.getPosts()
                                            .get(0)
                                            .getListComments()
                                            .get(0)
                                            .getCommentId()
                                            .equals(user1.getPosts()
                                                         .get(0)
                                                         .getListComments()
                                                         .get(0)
                                                         .getCommentId()))
         .verifyComplete();
  }
}

