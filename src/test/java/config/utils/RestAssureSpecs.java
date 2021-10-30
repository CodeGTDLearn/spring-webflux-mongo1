package config.utils;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecBuilder;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class RestAssureSpecs {

  final private static Long MAX_TIMEOUT = 15000L;
  final private static ContentType JSON_CONTENT_TYPE = ContentType.JSON;
  final private static ContentType ANY_CONTENT_TYPE = ContentType.ANY;

  private static WebTestClientRequestSpecification globalRequestSpecs;
  private static ResponseSpecification globalResponseSpecs;


  public static void restAssureGlobalRequestSpecificationsConfig() {
    globalRequestSpecs =
         new WebTestClientRequestSpecBuilder()
              .setContentType(JSON_CONTENT_TYPE)
              .build();
    globalRequestSpecs.accept(ANY_CONTENT_TYPE);

    RestAssuredWebTestClient.requestSpecification = globalRequestSpecs;
  }


  public static void restAssureGlobalResponseSpecificationsConfig() {
    globalResponseSpecs =
         new ResponseSpecBuilder()
//              .expectResponseTime(lessThanOrEqualTo(MAX_TIMEOUT))
//              .expectContentType(JSON_CONTENT_TYPE)
              .expectStatusCode(500)
              .build();

    RestAssuredWebTestClient.responseSpecification = globalResponseSpecs;
  }
}
//    given()
//         .when()
//         .spec(requestEspecificado)
//         .get("/users")
//         .then()
//         .spec(responseEsperado)




