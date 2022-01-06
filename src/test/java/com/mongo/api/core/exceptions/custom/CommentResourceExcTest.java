package com.mongo.api.core.exceptions.custom;//package com.mongo.api.modules.comment;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.comment.ICommentService;
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

import static com.mongo.api.core.routes.RoutesComment.*;
import static config.databuilders.CommentBuilder.commentNoId;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.RestAssureSpecs.*;
import static config.utils.TestUtils.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

// ==> EXCEPTIONS IN CONTROLLER:
// *** REASON: IN WEBFLUX, EXCEPTIONS MUST BE IN CONTROLLER - WHY?
//     - "Como stream pode ser manipulado por diferentes grupos de thread,
//     - caso um erro aconteça em uma thread que não é a que operou a controller,
//     - o ControllerAdvice não vai ser notificado "
//     - https://medium.com/nstech/programa%C3%A7%C3%A3o-reativa-com-spring-boot-webflux-e-mongodb-chega-de-sofrer-f92fb64517c3
@Import({
     TestDbConfig.class,
     //     PostService.class,
     //     UserService.class,
     //     CommentService.class,
     //     CustomExceptions.class,
     //     CustomExceptionsProperties.class,
     //     ModelMapper.class,
})
@DisplayName("CommentResourceTest")
@MergedResource
class CommentResourceExcTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose =
       new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";

  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private Comment comment1;
  private Post commentedPost;
  private User userCommentAuthor;

  private final String invalidId = Faker.instance()
                                        .idNumber()
                                        .valid();

  @Autowired
  CustomExceptionsCustomAttributes customExceptions;

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
  @DisplayName("FindByIdExc")
  void FindByIdExc() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENT_BY_ID,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getCommentNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/commentNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindUserByCommentIdExc")
  void FindUserByCommentIdExc() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_USER_BY_COMMENTID,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getCommentNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/commentNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindCommentsByAuthorIdExc")
  void FindCommentsByAuthorIdExc() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENTS_BY_AUTHORIDV1, invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("FindCommentsByPostIdExc")
  void FindCommentsByPostIdExc() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_COMMENTS_BY_POSTID,invalidId)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveLinkedExcUserNotFound")
  void SaveLinkedExcUserNotFound() {
    User userUnknown = userWithID_IdPostsEmpty().create();
    Post postUnknown = postFull_withId_CommentsEmpty(userUnknown).create();
    Comment comment2 = commentNoId(userUnknown,postUnknown).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_LINKED_OBJECT)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveLinkedExcPostNotFound")
  void SaveLinkedExcPostNotFound() {
    User userUnknown = userWithID_IdPostsEmpty().create();
    Post postUnknown = postFull_withId_CommentsEmpty(userUnknown).create();
    Comment comment2 = commentNoId(userCommentAuthor,postUnknown).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_LINKED_OBJECT)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("saveEmbedSubstExcUserNotFound")
  void saveEmbedSubstExcUserNotFound() {
    User userUnknown = userWithID_IdPostsEmpty().create();
    //    Post postUnknown = postFull_withId_CommentsEmpty(userUnknown).create();
    Comment comment2 = commentNoId(userUnknown,commentedPost).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_EMBED_OBJECT_SUBST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("saveEmbedSubstExcPostNotFound")
  void saveEmbedSubstExcPostNotFound() {
    User userUnknown = userWithID_IdPostsEmpty().create();
    Post postUnknown = postFull_withId_CommentsEmpty(userUnknown).create();
    Comment comment2 = commentNoId(userCommentAuthor,postUnknown).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_EMBED_OBJECT_SUBST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveEmbedListExcUserNotFound")
  void SaveEmbedListExcUserNotFound() {
    User userUnknown = userWithID_IdPostsEmpty().create();
    //    Post postUnknown = postFull_withId_CommentsEmpty(userUnknown).create();
    Comment comment2 = commentNoId(userUnknown,commentedPost).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_EMBED_OBJECT_LIST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("SaveEmbedListExcPostNotFound")
  void SaveEmbedListExcPostNotFound() {
    User userUnknown = userWithID_IdPostsEmpty().create();
    Post postUnknown = postFull_withId_CommentsEmpty(userUnknown).create();
    Comment comment2 = commentNoId(userCommentAuthor,postUnknown).create();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment2)

         .when()
         .post(SAVE_COMMENT_EMBED_OBJECT_LIST)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getPostNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/postNotFound.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("deleteExc")
  void deleteExc() {
    RestAssuredWebTestClient.responseSpecification = noContentTypeAndVoidResponses();

    comment1.setCommentId(invalidId);

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment1)

         .when()
         .delete()

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getCommentNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/commentNotFound.json"))
    ;

    dbUtils.countAndExecuteCommentFlux(commentService.findAll(),1);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("updateExc")
  void updateExc() {
    comment1.setCommentId(invalidId);

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(comment1)

         .when()
         .put()

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getCommentNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/commentNotFound.json"))
    ;
  }
}