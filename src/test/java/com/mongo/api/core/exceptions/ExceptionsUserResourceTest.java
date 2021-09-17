package com.mongo.api.core.exceptions;

import com.github.javafaker.Faker;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.core.exceptions.globalException.GlobalExceptionProperties;
import com.mongo.api.modules.post.IPostRepo;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.IUserService;
import com.mongo.api.modules.user.User;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.scheduler.Schedulers;
import utils.testcontainer.compose.ConfigComposeTests;
import utils.testcontainer.compose.ConfigControllerTests;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.api.core.routes.RoutesError.ERROR_PATH;
import static com.mongo.api.core.routes.RoutesPost.FIND_POSTS_BY_USERID;
import static com.mongo.api.core.routes.RoutesUser.FIND_USER_BY_ID;
import static com.mongo.api.core.routes.RoutesUser.REQ_USER;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static utils.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static utils.databuilders.UserBuilder.userWithID_IdPostsEmpty;

public class ExceptionsUserResourceTest extends ConfigControllerTests {

  @Container
  private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;
  final ContentType ANY = ContentType.ANY;
  final ContentType JSON = ContentType.JSON;
  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;
  private User user1, user3, userPostsOwner, userItemTest;
  private Post post1, post2;
  private List<User> userList;
  @Autowired
  private CustomExceptionsProperties customExceptions;

  @Autowired
  private GlobalExceptionProperties globalException;

  @Autowired
  private IUserService UserServiceInt;

  @Autowired
  private IPostRepo postRepo;


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
    userItemTest = userWithID_IdPostsEmpty().createTestUser();
    userList = Arrays.asList(user1,user3);
  }


  @Test
  void checkServices() {
    new ConfigComposeTests().checkTestcontainerComposeService(
         compose,
         ConfigComposeTests.SERVICE_COMPOSE_FILE,
         ConfigComposeTests.SERVICE_PORT
                                                             );
  }


  @Test
  @DisplayName("findById: UserNotFound")
  void findById() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + FIND_USER_BY_ID,Faker.instance()
                                              .idNumber()
                                              .valid())

         .then()
         .statusCode(NOT_FOUND.value())

         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .log()
    ;
  }


  @Test
  @DisplayName("delete: UserNotFound")
  void delete() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)
         .body(userItemTest)

         .when()
         .delete(REQ_USER)

         .then()
         .statusCode(NOT_FOUND.value())

         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .log()
    ;
  }


  @Test
  @DisplayName("update: UserNotFound")
  void update() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)
         .body(userItemTest)

         .when()
         .put(REQ_USER)

         .then()
         .statusCode(NOT_FOUND.value())

         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .log()
    ;
  }


  @Test
  @DisplayName("findPostsByUserId: UserNotFound")
  void findPostsByUserId() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + FIND_POSTS_BY_USERID,Faker.instance()
                                                   .idNumber()
                                                   .valid())

         .then()
         .statusCode(NOT_FOUND.value())

         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .log()
    ;
  }


  @Test
  @DisplayName("Global-Exception Error")
  void globalExceptionError() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .when()
         .get(REQ_USER + ERROR_PATH)

         .then()
         .statusCode(NOT_FOUND.value())

         .body(globalException.getGlobalAttribute(),
               equalTo("404 NOT_FOUND \"" + globalException.getGlobalMessage() + "\"")
              )
         .body(globalException.getDeveloperAttribute(),
               equalTo(globalException.getDeveloperMessage())
              )
         .log()
    ;
  }


  @Test
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
