package com.mongo.api.modules.comment;//package com.mongo.api.modules.comment;

import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptions;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostService;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserService;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import config.utils.TestDbUtils;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;

import java.util.Arrays;

import static com.mongo.api.core.routes.RoutesComment.FIND_ALL_COMMENTS;
import static com.mongo.api.core.routes.RoutesComment.REQ_COMMENT;
import static config.databuilders.CommentBuilder.commentNoId;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.RestAssureSpecs.requestSpecs;
import static config.utils.RestAssureSpecs.responseSpecs;
import static config.utils.TestUtils.*;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.OK;

@Import({
     TestDbConfig.class,
     PostService.class,
     UserService.class,
     CommentService.class,
     CustomExceptions.class,
     CustomExceptionsProperties.class,
     ModelMapper.class,
})
@DisplayName("CommentResourceTest")
@MergedResource
class CommentResourceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?>
       compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";

  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private Comment comment1;
  private Post commentedPost;
  private User commentAuthor, postAuthor;

  @Autowired
  TestDbUtils dbUtils;

  @Autowired
  IUserService userService;


  @BeforeAll
  static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
    globalComposeServiceContainerMessage(compose,
                                         TC_COMPOSE_SERVICE,
                                         TC_COMPOSE_SERVICE_PORT
                                        );

    RestAssuredWebTestClient.reset();
    RestAssuredWebTestClient.requestSpecification = requestSpecs();
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

    postAuthor = userWithID_IdPostsEmpty().create();
    commentAuthor = userWithID_IdPostsEmpty().create();
    Flux<User> userFlux = dbUtils.saveUserList(
         Arrays.asList(postAuthor,commentAuthor));
    dbUtils.countAndExecuteUserFlux(userFlux,2);

    commentedPost = postFull_withId_CommentsEmpty(postAuthor).create();
    Flux<Post> postFlux = dbUtils.savePostList(singletonList(commentedPost));
    dbUtils.countAndExecutePostFlux(postFlux,1);

    comment1 = commentNoId(commentAuthor,commentedPost).create();
    Flux<Comment> commentFlux = dbUtils.saveLinked(singletonList(comment1));
    dbUtils.countAndExecuteCommentFlux(commentFlux,1);

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
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(REQ_COMMENT + FIND_ALL_COMMENTS)

         .then()
         .log()
         .everything()

         .statusCode(OK.value())
    //         .body("size()",is(2))
    //         .body("body",hasItem(post1.getBody()))
    //         .body("body",hasItem(post3.getBody()))
    //         .body("title",hasItems(post1.getTitle(),post3.getTitle()))
    //         .body("author.id",hasItems(author.getId(),author.getId()))
    //         .body(matchesJsonSchemaInClasspath("contracts/post/findAll.json"))
    ;
  }
}