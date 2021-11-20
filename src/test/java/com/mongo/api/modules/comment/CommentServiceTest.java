package com.mongo.api.modules.comment;//package com.mongo.api.modules.comment;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import config.utils.TestDbUtils;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.mongo.api.core.routes.RoutesComment.REQ_COMMENT;
import static config.databuilders.CommentBuilder.commentFull;
import static config.databuilders.CommentBuilder.commentNoId;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.RestAssureSpecs.requestSpecsSetPath;
import static config.utils.RestAssureSpecs.responseSpecs;
import static config.utils.TestUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Import({
     TestDbConfig.class,
     ModelMapper.class})
@DisplayName("CommentServiceTest")
@MergedResource
class CommentServiceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?>
       compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";

  private Comment comment1, comment2;
  private Post commentedPost;
  private User userCommentAuthor;


  @Autowired
  CustomExceptionsProperties customExceptions;

  @Autowired
  TestDbUtils dbUtils;

  @Autowired
  ICommentService commentService;


  @BeforeAll
  static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
    globalComposeServiceContainerMessage(compose,
                                         TC_COMPOSE_SERVICE,
                                         TC_COMPOSE_SERVICE_PORT
                                        );

    RestAssuredWebTestClient.reset();
    RestAssuredWebTestClient.requestSpecification =
         requestSpecsSetPath("http://localhost:8080/" + REQ_COMMENT);

    RestAssuredWebTestClient.responseSpecification = responseSpecs();
  }


  @AfterAll
  static void afterAll(TestInfo testInfo) {
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-start");

    userCommentAuthor = userWithID_IdPostsEmpty().create();
    User userPostAuthor = userWithID_IdPostsEmpty().create();
    Flux<User> userFlux =
         dbUtils.saveUserList(asList(userCommentAuthor,userPostAuthor));
    dbUtils.countAndExecuteUserFlux(userFlux,2);


    commentedPost = postFull_withId_CommentsEmpty(userPostAuthor).create();
    Flux<Post> postFlux = dbUtils.savePostList(singletonList(commentedPost));
    dbUtils.countAndExecutePostFlux(postFlux,1);

    comment1 = commentNoId(userCommentAuthor,commentedPost).create();
    comment1.setCommentId(Faker.instance()
                               .idNumber()
                               .valid());
    comment2 = commentNoId(userCommentAuthor,commentedPost).create();
    Flux<Comment> commentFlux = dbUtils.saveLinked(asList(comment1,comment2));
    dbUtils.countAndExecuteCommentFlux(commentFlux,2);

  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAll")
  void FindAll() {
    StepVerifier
         .create(commentService.findAll())
         .expectSubscription()
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment1.getText()))

         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment2.getText()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindAllCommentsDto")
  void FindAllCommentsDto() {
    StepVerifier
         .create(commentService.findAllCommentsDto())
         .expectSubscription()
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment1.getText()))
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment2.getText()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  void FindById() {
    StepVerifier
         .create(commentService.findById(comment1.getCommentId()))
         .expectSubscription()
         .expectNextMatches(comment -> comment.getCommentId()
                                              .equals(comment1.getCommentId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindUserByCommentId")
  void FindUserByCommentId() {
    StepVerifier
         .create(commentService.findUserByCommentId(comment1.getCommentId()))
         .expectSubscription()
         .expectNextMatches(user -> user.getId()
                                        .equals(userCommentAuthor.getId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("findCommentsByAuthorIdV1")
  void findCommentsByAuthorIdV1() {
    StepVerifier
         .create(commentService.findCommentsByAuthorIdV1(userCommentAuthor.getId()))
         .expectSubscription()
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment1.getText()))
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment2.getText()))
         .verifyComplete();
  }

  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("findCommentsByAuthor_IdV2")
  void findCommentsByAuthor_IdV2() {
    StepVerifier
         .create(commentService.findCommentsByAuthor_IdV2(userCommentAuthor.getId()))
         .expectSubscription()
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment1.getText()))
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment2.getText()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindCommentsByPostId")
  void FindCommentsByPostId() {
    StepVerifier
         .create(commentService.findCommentsByPostId(commentedPost.getPostId()))
         .expectSubscription()
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment1.getText()))
         .expectNextMatches(comment -> comment.getText()
                                              .equals(comment2.getText()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveLinked")
  void SaveLinked() {
    var comment = commentNoId(userCommentAuthor,commentedPost).create();
    StepVerifier
         .create(commentService.saveLinked(comment))
         .expectSubscription()
         .expectNextMatches(comm -> comm.getText()
                                        .equals(comment.getText()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveEmbedSubst")
  void SaveEmbedSubst() {
    var comment = commentFull(userCommentAuthor,commentedPost).create();
    StepVerifier
         .create(commentService.saveEmbedSubst(comment))
         .expectSubscription()
         .expectNextMatches(pst ->
                                 (pst.getPostId()
                                     .equals(commentedPost.getPostId()) &&

                                      pst.getAuthor()
                                         .getId()
                                         .equals(commentedPost.getAuthor()
                                                              .getId()) &&

                                      pst.getComment()
                                         .getCommentId()
                                         .equals(comment.getCommentId())
                                 )
                           )
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveEmbedList")
  void SaveEmbedList() {
    var comment = commentNoId(userCommentAuthor,commentedPost).create();
    StepVerifier
         .create(commentService.saveEmbedSubst(comment))
         .expectSubscription()
         .expectNextMatches(post -> post.getPostId()
                                        .equals(commentedPost.getPostId()))
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Delete")
  void Delete() {
    StepVerifier
         .create(commentService.delete(comment1))
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(commentService.findAll())
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Update")
  void Update() {
    comment1.setText(Faker.instance()
                          .lorem()
                          .sentence());
    StepVerifier
         .create(commentService.update(comment1))
         .expectSubscription()
         .expectNextMatches(comm -> comm.getText()
                                        .equals(comment1.getText()))
         .verifyComplete();
  }
}