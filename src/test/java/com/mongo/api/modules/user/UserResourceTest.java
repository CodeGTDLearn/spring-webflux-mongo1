package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.testconfig.DbUtilsConfig;
import com.mongo.api.core.testconfig.TestUtilsConfig;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.comment.ICommentService;
import com.mongo.api.modules.post.IPostService;
import com.mongo.api.modules.post.Post;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import config.utils.DbUtils;
import config.utils.TestUtils;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.hamcrest.CoreMatchers;
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

import static com.mongo.api.core.routes.RoutesUser.*;
import static config.databuilders.CommentBuilder.comment_simple;
import static config.databuilders.PostBuilder.postFull_withId_CommentsEmpty;
import static config.databuilders.UserBuilder.*;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.TestUtils.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

// ==> EXCEPTIONS IN CONTROLLER:
// *** REASON: IN WEBFLUX, EXCEPTIONS MUST BE IN CONTROLLER - WHY?
//     - "Como stream pode ser manipulado por diferentes grupos de thread, caso um erro aconteça em
// uma thread que não é a que operou a controller, o ControllerAdvice não vai ser notificado "
//     - https://medium.com/nstech/programa%C3%A7%C3%A3o-reativa-com-spring-boot-webflux-e-mongodb-chega-de-sofrer-f92fb64517c3
@Import({DbUtilsConfig.class, TestUtilsConfig.class})
@DisplayName("ResourceTests")
@MergedResource
public class UserResourceTest {
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
  
  private User user1, user3, userPostsOwner;
  private List<User> twoUserList;
  private Flux<User> flux;

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

    user1 = userWithID_IdPostsEmpty().createTestUser();
    user3 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    userPostsOwner = userWithID_IdPostsEmpty().createTestUser();
    twoUserList = Arrays.asList(user1,user3);

    flux = userDbUtils.saveUserListAndGetFlux(twoUserList,userService);

    userDbUtils.StepVerifierCountAndExecuteFlux(flux,2);
  }


  @AfterEach
  void tearDown(TestInfo testInfo) {
    globalTestMessage(testInfo.getTestMethod()
                              .toString(),"method-end");
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAll() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + FIND_ALL_USERS)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("size()",is(2))
         .body("id",hasItem(user1.getId()))
         .body("id",hasItem(user3.getId()))

         .body(matchesJsonSchemaInClasspath("contracts/user/findAll.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAllDto() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + FIND_ALL_USERS_DTO)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("$",hasSize(2))
         .body("[0]",hasKey("id"))
         .body("[0]",hasKey("name"))
         .body("[0]",hasKey("email"))
         .body("[0]",CoreMatchers.not(hasKey("idPosts")))
         .body("[1]",hasKey("id"))
         .body("[1]",hasKey("name"))
         .body("[1]",hasKey("email"))
         .body("[1]",CoreMatchers.not(hasKey("idPosts")))
         .body("id",hasItems(user1.getId(),user3.getId()))
         .body("name",hasItems(user1.getName(),user3.getName()))
         .body("email",hasItems(user1.getEmail(),user3.getEmail()))

         .body(matchesJsonSchemaInClasspath("contracts/user/findAllDto.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findShowAll() {
    User user = userWithID_IdPostsEmpty().createTestUser();
    Post post = postFull_withId_CommentsEmpty(user).create();
    Comment comment = comment_simple(post).create();
    UserAllDto userShowAll = userShowAll_Test(user,
                                              post,
                                              comment
                                             ).createTestUserShowAll();

    userDbUtils.cleanTestDb(userService,postService,commentService);

    StepVerifier
         .create(
              userDbUtils.saveUserShowAllFinalInDb(user,
                                                   post,
                                                   comment,
                                                   userService,
                                                   postService,
                                                   commentService
                                                  ))
         .expectSubscription()
         .verifyComplete();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",JSON)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + FIND_ALL_SHOW_ALL_DTO)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("id",hasItem(userShowAll.getId()))
         .body("posts[0].postId",hasItem(post.getPostId()))
         .body("posts[0].listComments[0].commentId",hasItem(comment.getCommentId()))

         .body(matchesJsonSchemaInClasspath("contracts/user/findShowAll.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findById() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + FIND_USER_BY_ID,user3.getId())

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("id",equalTo(user3.getId()))

         .body(matchesJsonSchemaInClasspath("contracts/user/findById.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void save() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .body(userPostsOwner)
         .contentType(JSON)

         .when()
         .post(REQ_USER)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(CREATED.value())
         .body("id",containsStringIgnoringCase(userPostsOwner.getId()))
         .body("email",containsStringIgnoringCase(userPostsOwner.getEmail()))
         .body("name",containsStringIgnoringCase(userPostsOwner.getName()))

         .body(matchesJsonSchemaInClasspath("contracts/user/save.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void delete() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .body(user1)
         .contentType(JSON)

         .when()
         .delete(REQ_USER)

         .then()
         .log()
         .everything()

         .statusCode(NO_CONTENT.value())
    ;

    //    stepVerifierCountAndExecuteFlux(userService.findAll(),1);
    userDbUtils.StepVerifierCountAndExecuteFlux(userService.findAll(),1);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void update() {
    var previousEmail = user1.getEmail();

    var newEmail = Faker.instance()
                        .internet()
                        .emailAddress();

    user1.setEmail(newEmail);

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .body(user1)
         .contentType(JSON)

         .when()
         .put(REQ_USER)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(OK.value())
         .body("id",equalTo(user1.getId()))
         .body("name",equalTo(user1.getName()))
         .body("email",equalTo(newEmail))
         .body("email",not(equalTo(previousEmail)))

         .body(matchesJsonSchemaInClasspath("contracts/user/update.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  public void bHWorks() {
    testUtils.bhWorks();
  }
}