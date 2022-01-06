package config.utils;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecBuilder;
import io.restassured.module.webtestclient.specification.WebTestClientRequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.lessThanOrEqualTo;

@Slf4j
@NoArgsConstructor
public class RestAssureSpecs {

  final private static Long MAX_TIMEOUT = 15000L;
  final private static ContentType JSON_CONTENT_TYPE = ContentType.JSON;
  final private static ContentType ANY_CONTENT_TYPE = ContentType.ANY;


  public static WebTestClientRequestSpecification requestSpecs() {

    WebTestClientRequestSpecification requestSpecs =
         new WebTestClientRequestSpecBuilder()
              .setContentType(JSON_CONTENT_TYPE)
              .addHeader("Accept",String.valueOf(ContentType.ANY))
              .log(LogDetail.ALL)
              .build();

    requestSpecs.accept(ANY_CONTENT_TYPE);

    return requestSpecs;
  }

  public static WebTestClientRequestSpecification requestSpecsSetPath(String fullBasePath) {

    WebTestClientRequestSpecification requestSpecs =
         new WebTestClientRequestSpecBuilder()
              .setContentType(JSON_CONTENT_TYPE)
              .addHeader("Accept",String.valueOf(ContentType.ANY))
              .log(LogDetail.ALL)
              .setBasePath(fullBasePath)
              .build();

    requestSpecs.accept(ANY_CONTENT_TYPE);

    return requestSpecs;
  }


  public static ResponseSpecification responseSpecs() {

    ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();

    responseSpecBuilder
         .expectResponseTime(lessThanOrEqualTo(MAX_TIMEOUT))
         .expectContentType(JSON_CONTENT_TYPE)
         .expectHeader("Content-Type",String.valueOf(JSON_CONTENT_TYPE))
    ;
    return responseSpecBuilder.build();
  }


  public static ResponseSpecification noContentTypeAndVoidResponses() {

    return new ResponseSpecBuilder()
         .expectResponseTime(lessThanOrEqualTo(MAX_TIMEOUT))
         .build();
  }
}


//     ╔═════════════════════════════════════════════╗
//     ║             DO-NOT DELETE/REMOVE            ║
//     ╠═════════════════════════════════════════════╣
//     ║             LOCAL SPECIFICATIONS            ║
//     ║                   EXAMPLE                   ║
//     ╚═════════════════════════════════════════════╝
//    WebTestClientRequestSpecification requestSpecs;
//    requestSpecs =
//         new WebTestClientRequestSpecBuilder()
//              .setContentType(JSON)
//              .build();
//    requestSpecs.accept(ContentType.ANY);
//
//    ResponseSpecBuilder respBuilder = new ResponseSpecBuilder();
//    final ResponseSpecification responseEsperado =
//         respBuilder
//              .expectStatusCode(OK.value())
//              .build();

//  RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
//  final RequestSpecification requestEspecificado =
//       reqBuilder
//            .log(LogDetail.COOKIES)
//            .log(LogDetail.URI)
//            .log(LogDetail.HEADERS)
//            .addHeader("bear" ,"Chave de Acesso")
//            .addCookie("TestCookie" ,"resultado")
//            .log(LogDetail.BODY)
//            .build();
//
//  ResponseSpecBuilder respBuilder = new ResponseSpecBuilder();
//  final ResponseSpecification responseEsperado =
//       respBuilder
//            .expectStatusCode(OK.value())
//            .build();
//
//  given()
//                .when()
//                     .spec(requestEspecificado)
//                     .get("/users")
//
//                     .then()
//                     .spec(responseEsperado)
//
//                     .statusCode(OK.value())
//                     .body("$" ,hasSize(3))
//                     .body("name" ,hasItems("João da Silva" ,"Maria Joaquina" ,"Ana Júlia"))
//                     ;