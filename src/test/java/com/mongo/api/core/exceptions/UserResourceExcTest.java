package com.mongo.api.core.exceptions;

import com.github.javafaker.Faker;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.core.exceptions.globalException.GlobalExceptionProperties;
import com.mongo.api.modules.user.IUserRepo;
import com.mongo.api.modules.user.User;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.api.core.routes.RoutesError.ERROR_PATH;
import static com.mongo.api.core.routes.RoutesUser.*;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.TestUtils.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@DisplayName("UserResourceExcTest")
@MergedResource
public class UserResourceExcTest {


  // STATIC-@Container: one service for ALL tests
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  final ContentType ANY = ContentType.ANY;
  final ContentType JSON = ContentType.JSON;
  final String enabledTest = "true";
  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private User userItemTest;

  @Autowired
  private IUserRepo userRepo;

  @Autowired
  private CustomExceptionsProperties customExceptions;

  @Autowired
  private GlobalExceptionProperties globalException;


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
  public void beforeEach() {
    RestAssuredWebTestClient.reset();
    userItemTest = userWithID_IdPostsEmpty().createTestUser();
  }


  @Test
  @DisplayName("findById: UserNotFound")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findById() {
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
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("findAll: Empty")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAllEmpty() {
    List<User> emptyList = new ArrayList<>();

    Flux<User> userFlux = cleanDb_SavingListUsers_GetThemInAFlux(emptyList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();

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
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("findShowAll: Empty")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findShowAllEmpty() {
    List<User> emptyList = new ArrayList<>();

    Flux<User> userFlux = cleanDb_SavingListUsers_GetThemInAFlux(emptyList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
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
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("findAllDto: Empty")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAllDtoEmpty() {
    List<User> emptyList = new ArrayList<>();

    Flux<User> userFlux = cleanDb_SavingListUsers_GetThemInAFlux(emptyList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();

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
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("delete: UserNotFound")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void delete() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .body(userItemTest)
         .contentType(JSON)

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
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void update() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .header("Accept",ANY)
         .header("Content-type",JSON)

         .body(userItemTest)
         .contentType(JSON)

         .when()
         .put(REQ_USER)

         .then()
         .log()
         .everything()

         .contentType(JSON)
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("Global-Exception Error")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void globalExceptionError() {
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
  @EnabledIf(expression = enabledTest, loadContext = true)
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


  private Flux<User> cleanDb_SavingListUsers_GetThemInAFlux(List<User> userList) {
    return userRepo.deleteAll()
                   .thenMany(Flux.fromIterable(userList))
                   .flatMap(userRepo::save)
                   .doOnNext(item -> userRepo.findAll())
                   .doOnNext((item -> System.out.println(
                        "\n>>>>>>>>>>>>>>>Repo - UserID: " + item.getId() +
                             "|Name: " + item.getName() +
                             "|Email: " + item.getEmail())));
  }
}
