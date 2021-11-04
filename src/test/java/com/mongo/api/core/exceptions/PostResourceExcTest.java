package com.mongo.api.core.exceptions;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static com.mongo.api.core.routes.RoutesPost.*;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.RestAssureSpecs.*;
import static config.utils.TestUtils.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Import(TestDbConfig.class)
@DisplayName("PostResourceExcTest")
@MergedResource
class PostResourceExcTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?>
       compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";

  @Autowired
  CustomExceptionsProperties customExceptions;

  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private static Post invalidPostWithoutId;
  private static Post invalidPostWithId;
  private final String invalidId = Faker.instance()
                                        .idNumber()
                                        .valid();

  @Autowired
  TestDbUtils testDbUtils;


  @BeforeAll
  static void beforeAll(TestInfo testInfo) {
    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(),"class-start");
    globalComposeServiceContainerMessage(compose,
                                         TC_COMPOSE_SERVICE,
                                         TC_COMPOSE_SERVICE_PORT
                                        );

    User invalidAuthor = userWithID_IdPostsEmpty().createTestUser();
    invalidPostWithoutId = post_IdNull_CommentsEmpty(invalidAuthor).create();
    invalidPostWithId = postFull_withId_CommentsEmpty(invalidAuthor).create();

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
    //REAL-SERVER INJECTED IN WEB-TEST-CLIENT(non-blocking client)'
    //SHOULD BE USED WHEN 'DOCKER-COMPOSE' UP A REAL-WEB-SERVER
    //BECAUSE THERE IS 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
    // realWebClient = WebTestClient.bindToServer()
    //                      .baseUrl("http://localhost:8080/customer")
    //                      .build();

    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-start");

    User author = userWithID_IdPostsEmpty().createTestUser();
    Flux<User> userFlux = testDbUtils.saveUserList(singletonList(author));
    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    Post post1 = post_IdNull_CommentsEmpty(author).create();
    Post post3 = post_IdNull_CommentsEmpty(author).create();
    List<Post> postList = Arrays.asList(post1,post3);
    Flux<Post> postFlux = testDbUtils.savePostList(postList);
    testDbUtils.countAndExecutePostFlux(postFlux,2);

  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindById")
  void FindById() {

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(REQ_POST + FIND_POST_BY_ID,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindPostsByAuthorId")
  void FindPostsByAuthorId() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(REQ_POST + FIND_POSTS_BY_AUTHORID,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getAuthorNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/authorNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindPostByIdShowComments")
  void FindPostByIdShowComments() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(REQ_POST + FIND_POST_BY_ID_SHOW_COMMENTS,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Save")
  void Save() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .body(invalidPostWithoutId)

         .when()
         .post(REQ_POST + SAVE_EMBED_USER_IN_THE_POST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getAuthorNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/authorNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Delete")
  void Delete() {

    RestAssuredWebTestClient.responseSpecification = responseSpecNoContentType();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(invalidPostWithId)

         .when()
         .delete(REQ_POST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/postNotFound.json"))
    ;

  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("Update")
  void Update() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .body(invalidPostWithId)

         .when()
         .put(REQ_POST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindUserByPostId")
  void FindUserByPostId() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(REQ_POST + FIND_USER_BY_POSTID,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/postNotFound.json"))
    ;
  }
}