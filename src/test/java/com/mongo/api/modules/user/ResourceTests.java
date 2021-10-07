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
import config.testcontainer.compose.ConfigComposeTests;
import config.testcontainer.compose.ConfigControllerTests;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.api.core.routes.RoutesUser.*;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static config.databuilders.CommentBuilder.comment_simple;
import static config.databuilders.PostBuilder.postFull_CommentsEmpty;
import static config.databuilders.UserBuilder.*;

public class ResourceTests extends ConfigControllerTests {
  final String enabledTest = "true";
  final ContentType CONT_ANY = ContentType.ANY;
  final ContentType CONT_JSON = ContentType.JSON;

  @Container
  private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;
  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;
  private User user1, user3, userPostsOwner;
  private List<User> twoUserList;

  @Autowired
  private IUserService userService;

  @Autowired
  private IPostService postService;

  @Autowired
  private ICommentService commentService;

  @Autowired
  private ModelMapper modelMapper;


  @BeforeAll
  static void beforeAll() {
    ConfigComposeTests.beforeAll();
  }


  @AfterAll
  public static void afterAll() {
    ConfigComposeTests.afterAll();
    compose.close();
  }


  @BeforeEach
  public void beforeEach() {
    RestAssuredWebTestClient.reset();
    //REAL-SERVER INJECTED IN WEB-TEST-CLIENT(non-blocking client)'
    //SHOULD BE USED WHEN 'DOCKER-COMPOSE' UP A REAL-WEB-SERVER
    //BECAUSE THERE IS 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
    // realWebClient = WebTestClient.bindToServer()
    //                      .baseUrl("http://localhost:8080/customer")
    //                      .build();

    user1 = userWithID_IdPostsEmpty().createTestUser();
    user3 = userFull_IdNull_ListIdPostsEmpty().createTestUser();
    userPostsOwner = userWithID_IdPostsEmpty().createTestUser();
    twoUserList = Arrays.asList(user1,user3);
  }


  void cleanDbToTest() {
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


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void checkServices() {
    new ConfigComposeTests().checkTestcontainerComposeService(
         compose,
         ConfigComposeTests.SERVICE_COMPOSE_FILE,
         ConfigComposeTests.SERVICE_PORT
                                                             );
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findAll() {
    final Flux<User> userFlux = saveUserListAndGetItsFlux(twoUserList);

    StepVerifierCountUserFlux(userFlux,2);

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",CONT_ANY)
         .header("Content-type",CONT_JSON)

         .when()
         .get(REQ_USER + FIND_ALL_USERS)

         .then()
         .statusCode(OK.value())
         .log()
         .headers()
         .and()
         .log()

         .body()
         .body("size()",is(2))
         .body("id",hasItem(user1.getId()))
         .body("id",hasItem(user3.getId()))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findAllDto() {
    final Flux<User> userFlux = saveUserListAndGetItsFlux(twoUserList);

    StepVerifierCountUserFlux(userFlux,2);

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",CONT_ANY)
         .header("Content-type",CONT_JSON)

         .when()
         .get(REQ_USER + FIND_ALL_USERS_DTO)

         .then()
         .statusCode(OK.value())
         .log()
         .headers()
         .and()
         .log()

         .body()
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
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findAllShowAllDto() {

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
         .header("Accept",CONT_JSON)
         .header("Content-type",CONT_JSON)

         .when()
         .get(REQ_USER + FIND_ALL_SHOW_ALL_DTO)

         .then()
         .statusCode(OK.value())
         .log()
         .headers()
         .and()
         .log()

         .body()
         .body("id",hasItem(userShowAll.getId()))
         .body("posts[0].postId",hasItem(post.getPostId()))
         .body("posts[0].listComments[0].commentId",hasItem(comment.getCommentId()))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void findById() {
    final Flux<User> userFlux = saveUserListAndGetItsFlux(twoUserList);

    StepVerifierCountUserFlux(userFlux,2);

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",CONT_ANY)
         .header("Content-type",CONT_JSON)

         .when()
         .get(REQ_USER + FIND_USER_BY_ID,user3.getId())

         .then()
         .statusCode(OK.value())
         .log()
         .headers()
         .and()
         .log()

         .body()
         .body("id",equalTo(user3.getId()))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void save() {
    cleanDbToTest();

    StepVerifierCountUserFlux(userService.findAll(),0);

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",CONT_ANY)
         .header("Content-type",CONT_JSON)

         .body(userPostsOwner)

         .when()
         .post(REQ_USER)

         .then()
         .statusCode(CREATED.value())
         .contentType(CONT_JSON)
         .body("id",containsStringIgnoringCase(userPostsOwner.getId()))
         .body("email",containsStringIgnoringCase(userPostsOwner.getEmail()))
         .body("name",containsStringIgnoringCase(userPostsOwner.getName()))
    ;

    StepVerifierCountUserFlux(userService.findAll(),1);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void delete() {
    Flux<User> userFlux = saveUserListAndGetItsFlux(twoUserList);

    StepVerifierCountUserFlux(userFlux,2);

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",CONT_ANY)
         .header("Content-type",CONT_JSON)

         .body(user1)

         .when()
         .delete(REQ_USER)

         .then()
         .log()
         .headers()
         .statusCode(NO_CONTENT.value())
    ;

    StepVerifierCountUserFlux(userService.findAll(),1);
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  void update() {
    final Flux<User> userFlux = saveUserListAndGetItsFlux(twoUserList);

    StepVerifierCountUserFlux(userFlux,2);

    var previousEmail = user1.getEmail();

    var currentEmail = Faker.instance()
                            .internet()
                            .emailAddress();

    user1.setEmail(currentEmail);

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",CONT_ANY)
         .header("Content-type",CONT_JSON)

         .body(user1)

         .when()
         .put(REQ_USER)

         .then()
         .log()
         .headers()
         .and()
         .log()
         .body()
         .and()
         .statusCode(OK.value())
         .contentType(CONT_JSON)

         .body("id",equalTo(user1.getId()))
         .body("name",equalTo(user1.getName()))
         .body("email",equalTo(currentEmail))
         .body("email",not(equalTo(previousEmail)))
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
}