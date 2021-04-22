package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.testcontainer.compose.ConfigComposeTests;
import com.mongo.testcontainer.compose.ConfigControllerTests;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.api.core.Routes.*;
import static com.mongo.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static com.mongo.databuilders.UserBuilder.userWithID_ListIdPostsEmpty;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

public class UserResourceTest extends ConfigControllerTests {


    private User user1, user3, userWithIdForPost1Post2;
    private Post post1, post2;
    private List<User> userList;

    final ContentType ANY = ContentType.ANY;
    final ContentType JSON = ContentType.JSON;


    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
    // SHOULD BE USED WITH 'TEST-CONTAINERS'
    // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
    @Autowired
    WebTestClient mockedWebClient;


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepo postRepo;

    @Autowired
    private UserService service;


    @BeforeAll
    static void beforeAll() {
        ConfigComposeTests.beforeAll();
    }


    @AfterAll
    static void afterAll() {
        ConfigComposeTests.afterAll();
        compose.close();
    }


    @BeforeEach
    public void setUpLocal() {
        //REAL-SERVER INJECTED IN WEB-TEST-CLIENT(non-blocking client)'
        //SHOULD BE USED WHEN 'DOCKER-COMPOSE' UP A REAL-WEB-SERVER
        //BECAUSE THERE IS 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
        // realWebClient = WebTestClient.bindToServer()
        //                      .baseUrl("http://localhost:8080/customer")
        //                      .build();

        //        service = new UserService(userRepo,postRepo);

        user1 = userFull_IdNull_ListIdPostsEmpty().create();
        user3 = userFull_IdNull_ListIdPostsEmpty().create();
        userWithIdForPost1Post2 = userWithID_ListIdPostsEmpty().create();
        userList = Arrays.asList(user1,user3);
    }


    void cleanDbToTest() {
        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                                   " <==================\n\n");
    }


    private void StepVerifierCountUserFlux(Flux<User> flux,int totalElements) {
        StepVerifier
                .create(flux)
                .expectSubscription()
                .expectNextCount(totalElements)
                .verifyComplete();
    }


    @Test
    void checkServices() {

        new ConfigComposeTests().checkTestcontainerComposeService(
                compose,
                ConfigComposeTests.SERVICE,
                ConfigComposeTests.SERVICE_PORT
                                                                 );
    }


    @Test
    void findAll() {
        final Flux<User> userFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(userList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifierCountUserFlux(userFlux,2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

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
    void findAllDto() {
        final Flux<User> userFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(userList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifierCountUserFlux(userFlux,2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .when()
                .get(REQ_USER + FIND_ALL_USERS_DTO)

                .then()
                .statusCode(OK.value())
                .log()
                .headers()
                .and()
                .log()

                .body()
                .body("size()",is(2))

                .body("email",hasItem(user1.getEmail()))
                .body("email",hasItem(user3.getEmail()))

                .body("[0]",hasKey("id"))
                .and()
                .body("[1]",hasKey("name"))
                .and()
                .body("[2]",hasKey("email"))
                .and()
                .body("[3]",CoreMatchers.not(hasKey("idPosts")))
        ;
    }


    @Test
    void findById() {
        final Flux<User> userFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(userList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifierCountUserFlux(userFlux,2);

        given()
                .request()
                .header("Accept",ANY)
                .header("Content-type",JSON)
                .body(new UserDto(user1))

                .when()
                .get(REQ_USER + FIND_USER_BY_ID,user1.getId())

                .then()
                .log()
                .headers()
                .and()
                .log()
                .body()
                .and()
                .statusCode(OK.value())
                .contentType(JSON)

                .body("id",equalTo(user1.getId()))
        ;
    }


    @Test
    void findErrorUserNotFound() {
    }


    @Test
    void save() {
        cleanDbToTest();

        StepVerifierCountUserFlux(service.findAll(),0);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .header("Accept",ContentType.ANY)
                .header("Content-type",ContentType.JSON)
                .body(userWithIdForPost1Post2)

                .when()
                .post(REQ_USER)

                .then()
                .statusCode(CREATED.value())
                .body("id",containsStringIgnoringCase(userWithIdForPost1Post2.getId()))
                .body("email",containsStringIgnoringCase(userWithIdForPost1Post2.getEmail()))
                .body("name",containsStringIgnoringCase(userWithIdForPost1Post2.getName()))
        ;

        StepVerifierCountUserFlux(service.findAll(),1);
    }


    @Test
    void delete() {
        final Flux<User> userFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(userList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifierCountUserFlux(userFlux,2);

        given()
                .request()
                .header("Accept",ANY)
                .header("Content-type",JSON)
                .body(new UserDto(user1))

                .when()
                .delete(REQ_USER)

                .then()
                .log()
                .headers()
                .statusCode(NO_CONTENT.value())
        ;

        StepVerifierCountUserFlux(userFlux,1);
    }


    @Test
    void update() {
        final Flux<User> userFlux =
                service.deleteAll()
                       .thenMany(Flux.fromIterable(userList))
                       .flatMap(service::save)
                       .doOnNext(item -> service.findAll());

        StepVerifierCountUserFlux(userFlux,2);

        var previousEmail = user1.getEmail();
        var currentEmail = Faker.instance()
                                .internet()
                                .emailAddress();
        user1.setEmail(currentEmail);

        given()
                .request()
                .header("Accept",ANY)
                .header("Content-type",JSON)
                .body(new UserDto(user1))

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
                .contentType(JSON)

                .body("email",equalTo(user3.getEmail()))
                .body("email",equalTo(currentEmail))
                .body("email",not(equalTo(previousEmail)))
        ;
    }


    @Test
    void findPostsByUserId() {
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