package com.mongo.api.core.exceptions.global;

import com.mongo.api.core.config.TestDbConfig;
import com.mongo.api.core.exceptions.custom.CustomExceptionsCustomAttributes;
import com.mongo.api.modules.user.User;
import config.annotations.MergedResource;
import config.testcontainer.TcComposeConfig;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.mongo.api.core.routes.RoutesError.ERROR_PATH;
import static com.mongo.api.core.routes.RoutesUser.REQ_USER;
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
class GlobalResourceExcTest {

  // STATIC-@Container: one service for ALL tests
  // NON-STATIC-@Container: one service for EACH test
  @Container
  private static final DockerComposeContainer<?> compose = new TcComposeConfig().getTcCompose();


  final String enabledTest = "true";
  // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
  // SHOULD BE USED WITH 'TEST-CONTAINERS'
  // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
  @Autowired
  WebTestClient mockedWebClient;

  @Autowired
  GlobalExceptionCustomAttributes globalException;

  @BeforeAll
  static void beforeAll(TestInfo testInfo) {

    globalBeforeAll();
    globalTestMessage(testInfo.getDisplayName(), "class-start");
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
    globalTestMessage(testInfo.getDisplayName(), "class-end");
  }


  @BeforeEach
  void beforeEach() {

    User userItemTest = userWithID_IdPostsEmpty().create();
  }


  @Test
  @DisplayName("Global-Exception Error")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void globalExceptionError() {

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)

         .when()
         .get(ERROR_PATH)

         .then()
         .statusCode(NOT_FOUND.value())
         .log()
         .everything()

         .body(
              globalException.getGlobalAttribute(),
              equalTo("404 NOT_FOUND \"" + globalException.getGlobalMessage() + "\"")
              )
         .body(
              globalException.getDeveloperAttribute(),
              equalTo(globalException.getDeveloperMessage())
              )
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/global/globalException.json"))
    ;
  }

  @Test
  @DisplayName("Global-Exception Error Stack")
  @EnabledIf(expression = enabledTest, loadContext = true)
  public void globalExceptionErrorStack() {

    RestAssuredWebTestClient
         .given()
         .webTestClient(mockedWebClient)
         .queryParam("completeStackTrace", true)

         .when()
         .get(ERROR_PATH)

         .then()
         .statusCode(NOT_FOUND.value())
         .log()
         .everything()

         .body(
              globalException.getGlobalAttribute(),
              equalTo("404 NOT_FOUND \"" + globalException.getGlobalMessage() + "\"")
              )
         .body(
              globalException.getDeveloperAttribute(),
              equalTo(globalException.getDeveloperMessage())
              )
         .body(matchesJsonSchemaInClasspath("contracts/exceptions/global/globalExceptionStack.json"))
    ;
  }


  @Test
  @EnabledIf(expression = enabledTest, loadContext = true)
  @DisplayName("BHWorks")
  void bHWorks() {

    bhWorks();
  }

}