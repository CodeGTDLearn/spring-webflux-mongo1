package com.mongo.api.core.exceptions;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.customExceptions.CustomExceptionsProperties;
import com.mongo.api.core.exceptions.globalException.GlobalExceptionProperties;
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

import java.util.ArrayList;
import java.util.List;

import static com.mongo.api.core.routes.RoutesError.ERROR_PATH;
import static com.mongo.api.core.routes.RoutesUser.*;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.BlockhoundUtils.bhWorks;
import static config.utils.TestUtils.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Import({TestDbConfig.class})
@DisplayName("UserResourceExcTest")
@MergedResource
public class UserResourceExcTest {


  // STATIC-@Container: one service for ALL tests
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();

  // final ContentType ANY = ContentType.ANY;
  // final ContentType JSON = ContentType.JSON;
  final String enabledTest = "true";
  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  private User userItemTest;

  @Autowired
  private TestDbUtils dbUtils;

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



         .when()
         .get(REQ_USER + FIND_USER_BY_ID,Faker.instance()
                                              .idNumber()
                                              .valid())

         .then()
         .log()
         .everything()

         // .contentType(JSON)
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("findAll: Empty")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAllEmpty() {
    List<User> emptyList = new ArrayList<>();

    Flux<User> userFlux = dbUtils.saveUserList(emptyList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)



         .when()
         .get(REQ_USER + FIND_ALL_USERS)

         .then()
         .log()
         .everything()

         // .contentType(JSON)
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("findShowAll: Empty")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findShowAllEmpty() {
    List<User> emptyList = new ArrayList<>();

    Flux<User> userFlux = dbUtils.saveUserList(emptyList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)
         // .header("Accept",JSON)


         .when()
         .get(REQ_USER + FIND_ALL_SHOW_ALL_DTO)

         .then()
         .log()
         .everything()

         // .contentType(JSON)
         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
    ;
  }


  @Test
  @DisplayName("findAllDto: Empty")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findAllDtoEmpty() {
    List<User> emptyList = new ArrayList<>();

    Flux<User> userFlux = dbUtils.saveUserList(emptyList);

    StepVerifier
         .create(userFlux)
         .expectSubscription()
         .expectNextCount(0L)
         .verifyComplete();

    RestAssuredWebTestClient

         .given()
         .webTestClient(mockedWebClient)



         .when()
         .get(REQ_USER + FIND_ALL_USERS_DTO)

         .then()
         .log()
         .everything()

         // .contentType(JSON)
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



         .body(userItemTest)
         // .contentType(JSON)

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



         .body(userItemTest)
         // .contentType(JSON)

         .when()
         .put(REQ_USER)

         .then()
         .log()
         .everything()

         // .contentType(JSON)
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
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  public void bHWorks() {
    bhWorks();
  }

}
