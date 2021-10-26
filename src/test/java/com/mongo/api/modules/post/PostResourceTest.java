package com.mongo.api.modules.post;

import com.mongo.api.core.testconfig.DbUtilsConfig;
import com.mongo.api.core.testconfig.TestUtilsConfig;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import config.utils.DbUtils;
import config.utils.TestUtils;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mongo.api.core.routes.RoutesPost.*;
import static config.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.TestUtils.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.OK;

@Import({DbUtilsConfig.class,TestUtilsConfig.class})
@DisplayName("PostResourceTest")
@MergedResource
class PostResourceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";
  final ContentType ANY = ContentType.ANY;
  final ContentType JSON = ContentType.JSON;

  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private Post post1, post3;
  private List<Post> twoPostsList;
  private Flux<Post> postFlux;
  private User postAuthor;

  @Autowired
  private TestUtils testUtils;

  @Autowired
  private DbUtils<Post> postDbUtils;

  @Autowired
  private IUserService userService;

  @Autowired
  private IPostService postService;


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
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  public void beforeEach(TestInfo testInfo) {
    RestAssuredWebTestClient.reset();
    //REAL-SERVER INJECTED IN WEB-TEST-CLIENT(non-blocking client)'
    //SHOULD BE USED WHEN 'DOCKER-COMPOSE' UP A REAL-WEB-SERVER
    //BECAUSE THERE IS 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
    // realWebClient = WebTestClient.bindToServer()
    //                      .baseUrl("http://localhost:8080/customer")
    //                      .build();

    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-start");

    postAuthor = userWithID_IdPostsEmpty().createTestUser();
    post1 = post_IdNull_CommentsEmpty(postAuthor).create();
    post3 = post_IdNull_CommentsEmpty(postAuthor).create();

    Flux<User> userFlux = postDbUtils.saveUserListAndGetFlux(
         Collections.singletonList(postAuthor),userService);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    twoPostsList = Arrays.asList(post1,post3);
    postFlux = postDbUtils.savePostListAndGetFlux(twoPostsList,postService);
    postDbUtils.StepVerifierCountAndExecuteFlux(postFlux,2);
  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = "true", loadContext = true)
  void findAll() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_POST + FIND_ALL_POSTS)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("size()",is(2))
         .body("body",hasItem(post1.getBody()))
         .body("body",hasItem(post3.getBody()))
         .body("title",hasItems(post1.getTitle(),post3.getTitle()))
         .body("author.id",hasItems(postAuthor.getId(),postAuthor.getId()))
         .body(matchesJsonSchemaInClasspath("contracts/post/findAll.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  void bHWorks() {
    testUtils.bhWorks();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findById() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_POST + FIND_POST_BY_ID,post1.getPostId())

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("id",equalTo(post1.getPostId()))
         .body("body",equalTo(post1.getBody()))

         .body(matchesJsonSchemaInClasspath("contracts/post/findById.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findPostsByAuthorId() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_POST + FIND_POSTS_BY_AUTHORID,postAuthor.getId())

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("id",equalTo(post1.getPostId()))
         .body("body",equalTo(post1.getBody()))

//         .body(matchesJsonSchemaInClasspath("contracts/post/findById.json"))
    ;
  }
}