package com.mongo.api.modules.post;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import config.utils.TestDbUtils;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecBuilder;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification;
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
import static config.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.BlockhoundUtils.bhWorks;
import static config.utils.TestUtils.*;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpStatus.*;

@Import(TestDbConfig.class)
@DisplayName("PostResourceTest")
@MergedResource
class PostResourceTest {

  // STATIC-@Container: one service for ALL tests -> SUPER FASTER
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final String enabledTest = "true";
  // final ContentType ANY = ContentType.ANY;
  // final ContentType JSON = ContentType.JSON;

  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private Post post1, post3;
  private User postsAuthor;

  @Autowired
  private TestDbUtils dbUtils;

  @Autowired
  private IUserService userService;


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

    postsAuthor = userWithID_IdPostsEmpty().createTestUser();
    Flux<User> userFlux = dbUtils.saveUserList(singletonList(postsAuthor));
    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(1L)
         .verifyComplete();

    post1 = post_IdNull_CommentsEmpty(postsAuthor).createTestPost();
    post3 = post_IdNull_CommentsEmpty(postsAuthor).createTestPost();
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
  void findAll() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)


         .when()
         .get(REQ_POST + FIND_ALL_POSTS)

         .then()
         .log()
         .everything()

         //         .contentType(JSON)
         .statusCode(OK.value())
         .body("size()",is(2))
         .body("body",hasItem(post1.getBody()))
         .body("body",hasItem(post3.getBody()))
         .body("title",hasItems(post1.getTitle(),post3.getTitle()))
         .body("author.id",hasItems(postsAuthor.getId(),postsAuthor.getId()))
         .body(matchesJsonSchemaInClasspath("contracts/post/findAll.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  void bHWorks() {
    bhWorks();
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findById() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)


         .when()
         .get(REQ_POST + FIND_POST_BY_ID,post1.getPostId())

         .then()
         .log()
         .everything()

         //         .contentType(JSON)
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


         .when()
         .get(REQ_POST + FIND_POSTS_BY_AUTHORID,postsAuthor.getId())

         .then()
         .log()
         .everything()

         //         .contentType(JSON)
         .statusCode(OK.value())
         .body("id",hasItems(post1.getPostId(),post3.getPostId()))
         .body("body",hasItems(post1.getBody(),post3.getBody()))

         .body(matchesJsonSchemaInClasspath("contracts/post/findPostsByAuthorId.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findPostByIdShowComments() {

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)


         .when()
         .get(REQ_POST + FIND_POST_BY_ID_SHOW_COMMENTS,post1.getPostId())

         .then()
         .log()
         .everything()
         .log()
         .headers()
         .and()
         .log()
         .body()

         // .contentType(JSON)
         .statusCode(OK.value())
         .body("id",containsString(post1.getPostId()))
         .body("body",containsString(post1.getBody()))

         .body(matchesJsonSchemaInClasspath("contracts/post/findPostByIdShowComments.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void saveEmbedObject() {
    post3 = post_IdNull_CommentsEmpty(postsAuthor).createTestPost();

    WebTestClientRequestSpecification requestSpecs;
    requestSpecs =
         new WebTestClientRequestSpecBuilder()
              .setContentType(JSON)
//              .log(LogDetail.ALL)
              .build();
    requestSpecs.accept(ContentType.ANY);

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .spec(requestSpecs)


         .body(post3)
         // .contentType(JSON)

         .when()
         .post(REQ_POST + SAVE_EMBED_USER_IN_THE_POST)

         .then()
         .log()
         .everything()

         // .contentType(JSON)
         .statusCode(CREATED.value())
         .body("id",equalTo(post3.getPostId()))
         .body("body",equalTo(post3.getBody()))

         .body(matchesJsonSchemaInClasspath("contracts/post/saveObject.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void delete() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         .body(post1)
//         .contentType(JSON)

         .when()
         .delete(REQ_POST)

         .then()
         .log()
         .everything()

         .statusCode(NO_CONTENT.value())
    ;

    dbUtils.countAndExecuteUserFlux(userService.findAll(),1);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void update() {
    var previousTitle = post1.getTitle();

    var updatedTitle = Faker.instance()
                            .book()
                            .title();

    post1.setTitle(updatedTitle);

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)


         .body(post1)
         // .contentType(JSON)

         .when()
         .put(REQ_POST)

         .then()
         .log()
         .everything()

         // .contentType(JSON)
         .statusCode(OK.value())
         .body("title",equalTo(updatedTitle))
         .body("title",not(equalTo(previousTitle)))

         .body(matchesJsonSchemaInClasspath("contracts/post/update.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findUserByPostId() {
    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)


         .when()
         .get(REQ_POST + FIND_USER_BY_POSTID,post1.getPostId())

         .then()
         .log()
         .everything()

         // .contentType(JSON)
         .statusCode(OK.value())
         .body("id",equalTo(post1.getAuthor()
                                 .getId()))

         .body(matchesJsonSchemaInClasspath("contracts/post/findUserByPostId.json"))
    ;
  }
}