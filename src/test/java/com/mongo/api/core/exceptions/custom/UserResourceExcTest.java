package com.mongo.api.core.exceptions.custom;

import com.github.javafaker.Faker;
import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.global.GlobalExceptionCustomAttributes;
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

import static com.mongo.api.core.routes.RoutesUser.*;
import static config.databuilders.UserBuilder.userWithID_IdPostsEmpty;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE;
import static config.testcontainer.TcComposeConfig.TC_COMPOSE_SERVICE_PORT;
import static config.utils.BlockhoundUtils.bhWorks;
import static config.utils.RestAssureSpecs.requestSpecsSetPath;
import static config.utils.RestAssureSpecs.responseSpecs;
import static config.utils.TestUtils.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpStatus.NOT_FOUND;

// ==> EXCEPTIONS IN CONTROLLER:
// *** REASON: IN WEBFLUX, EXCEPTIONS MUST BE IN CONTROLLER - WHY?
//     - "Como stream pode ser manipulado por diferentes grupos de thread,
//     - caso um erro aconteça em uma thread que não é a que operou a controller,
//     - o ControllerAdvice não vai ser notificado "
//     - https://medium.com/nstech/programa%C3%A7%C3%A3o-reativa-com-spring-boot-webflux-e-mongodb-chega-de-sofrer-f92fb64517c3
@Import({TestDbConfig.class})
@DisplayName("UserResourceExcTest")
@MergedResource
class UserResourceExcTest {

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
  TestDbUtils dbUtils;

  @Autowired
  CustomExceptionsCustomAttributes customExceptions;

  @Autowired
  GlobalExceptionCustomAttributes globalException;


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
         requestSpecsSetPath("http://localhost:8080/" + REQ_USER);
    RestAssuredWebTestClient.responseSpecification = responseSpecs();
  }


  @AfterAll
  static void afterAll(TestInfo testInfo) {
    globalAfterAll();
    globalTestMessage(testInfo.getDisplayName(),"class-end");
  }


  @BeforeEach
  void beforeEach() {
    userItemTest = userWithID_IdPostsEmpty().create();
  }


  @Test
  @DisplayName("findById: UserNotFound")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void findById() {
    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(FIND_USER_BY_ID,Faker.instance()
                                   .idNumber()
                                   .valid())

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
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
         .get(FIND_ALL_USERS)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
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

         .when()
         .get(FIND_ALL_SHOW_ALL_DTO)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
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
         .get(FIND_ALL_USERS_DTO)

         .then()
         .log()
         .everything()

         .statusCode(NOT_FOUND.value())
         .body("detail",equalTo(customExceptions.getUsersNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
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

         .when()
         .delete()

         .then()
         .statusCode(NOT_FOUND.value())
         .log()
         .everything()

         .body("detail",equalTo(customExceptions.getUserNotFoundMessage()))
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/custom/userNotFound.json"))
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

         .when()
         .put()

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
  @DisplayName("BHWorks")
  void bHWorks() {
    bhWorks();
  }

}