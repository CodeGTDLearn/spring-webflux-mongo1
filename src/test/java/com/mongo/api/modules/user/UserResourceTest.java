package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.core.dto.UserAllDto;
import com.mongo.api.core.dto.UserAuthorDto;
import com.mongo.api.modules.comment.Comment;
import com.mongo.api.modules.comment.ICommentService;
import com.mongo.api.modules.post.IPostService;
import com.mongo.api.modules.post.Post;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import testsconfig.annotations.MergedResource;
import testsconfig.testcontainer.TcComposeConfig;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.api.core.routes.RoutesUser.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static testsconfig.databuilders.CommentBuilder.comment_simple;
import static testsconfig.databuilders.PostBuilder.postFull_CommentsEmpty;
import static testsconfig.databuilders.UserBuilder.*;
import static testsconfig.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static testsconfig.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static testsconfig.utils.TestUtils.*;

@DisplayName("ResourceTests")
@MergedResource
public class UserResourceTest {

  //STATIC: one service for ALL tests
  //NON-STATIC: one service for EACH test
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
  private Flux<User> userFlux;

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

    userFlux = saveUserListAndGetItsFlux(twoUserList);
    StepVerifierCountUserFlux(userFlux,2);
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

         .body(matchesJsonSchemaInClasspath("contracts/findAll.json"))
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

         .body(matchesJsonSchemaInClasspath("contracts/findAllDto.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findShowAll() {
    User user = userWithID_IdPostsEmpty().createTestUser();
    Post post = postFull_CommentsEmpty(user).create();
    Comment comment = comment_simple(post).create();
    UserAllDto userShowAll = userShowAll_Test(user,
                                              post,
                                              comment
                                             ).createTestUserShowAll();
    cleanDbToTest();

    StepVerifier
         .create(saveUserShowAllFinalInDb(user,post,comment))
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

         .body(matchesJsonSchemaInClasspath("contracts/findShowAll.json"))
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

         .body(matchesJsonSchemaInClasspath("contracts/findById.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void save() {
    cleanDbToTest();

    StepVerifierCountUserFlux(userService.findAll(),0);

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

         .body(matchesJsonSchemaInClasspath("contracts/save.json"))
    ;

    StepVerifierCountUserFlux(userService.findAll(),1);
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

    StepVerifierCountUserFlux(userService.findAll(),1);
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

         .body(matchesJsonSchemaInClasspath("contracts/update.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  public void bHWorks() {
    try {
      FutureTask<?> task = new FutureTask<>(() -> {
        Thread.sleep(0);
        return "";
      });

      Schedulers.parallel()
                .schedule(task);

      task.get(10,TimeUnit.SECONDS);
      Assertions.fail("should fail");
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      Assertions.assertTrue(e.getCause() instanceof BlockingOperationError,"detected");
    }
  }


  private void cleanDbToTest() {
    StepVerifier
         .create(userService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(postService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    StepVerifier
         .create(commentService.deleteAll())
         .expectSubscription()
         .verifyComplete();

    System.out.println("\n>==================================================>" +
                            "\n>===============> CLEAN-DB-TO-TEST >===============>" +
                            "\n>==================================================>\n");
  }


  private void StepVerifierCountUserFlux(Flux<User> flux,int totalElements) {
    StepVerifier
         .create(flux)
         .expectSubscription()
         .expectNextCount(totalElements)
         .verifyComplete();
  }


  private Flux<User> saveUserListAndGetItsFlux(List<User> listUser) {
    return userService.deleteAll()
                      .thenMany(Flux.fromIterable(listUser))
                      .flatMap(userService::save)
                      .doOnNext(item -> userService.findAll())
                      .doOnNext((item -> System.out.println(
                           "\nSaving 'User' in DB:" +
                                "\n -> ID: " + item.getId() +
                                "\n -> Name: " + item.getName() +
                                "\n -> Email: " + item.getEmail() + "\n")));
  }


  private Mono<Void> saveUserShowAllFinalInDb(User user,Post post,Comment comment) {
    return userService.deleteAll()
                      .then(Mono.just(user))
                      .flatMap(userService::save)
                      .flatMap((user2 -> {
                        System.out.println(
                             "\nSaving 'User' in DB:" +
                                  "\n -> ID: " + user2.getId() +
                                  "\n -> Name: " + user2.getName() +
                                  "\n -> Email: " + user2.getEmail() + "\n");
                        return Mono.just(user2);
                      }))
                      .then(postService.deleteAll())
                      .thenMany(userService.findAll())
                      .flatMap(user2 -> {
                        UserAuthorDto authorDto = modelMapper.map(user2,UserAuthorDto.class);
                        post.setAuthor(authorDto);
                        return postService.save(post);
                      })
                      .flatMap((post2 -> {
                        System.out.println(
                             "\nSaving 'Post' in DB:" +
                                  "\n -> Post-ID: " + post2.getPostId() +
                                  "\n -> Post-Title: " + post2.getTitle() + "\n" +
                                  "\n -> Author-ID: " + post2.getAuthor()
                                                             .getId() +
                                  "\n -> Author-Name: " + post2.getAuthor()
                                                               .getName() + "\n");
                        return Mono.just(post2);
                      }))
                      .then(commentService.deleteAll())
                      .thenMany(postService.findAll())
                      .flatMap(post2 -> {
                        comment.setPostId(post2.getPostId());
                        comment.setAuthor(post2.getAuthor());
                        return commentService.saveLinkedObject(comment);
                      })
                      .flatMap((comment2 -> {
                        System.out.println(
                             "\nSaving 'Comment' in DB:" +
                                  "\n -> ID: " + comment2.getCommentId() +
                                  "\n -> PostId: " + comment2.getPostId() +
                                  "\n -> Author: " + comment2.getAuthor() + "\n");
                        return Mono.just(comment2);
                      }))
                      .then()

         ;
  }


}