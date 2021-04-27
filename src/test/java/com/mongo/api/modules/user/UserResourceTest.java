package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostService;
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
import utils.testcontainer.compose.ConfigComposeTests;
import utils.testcontainer.compose.ConfigControllerTests;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.api.core.Routes.*;
import static org.hamcrest.CoreMatchers.containsStringIgnoringCase;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static utils.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static utils.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static utils.databuilders.UserBuilder.userWithID_IdPostsEmpty;

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

    //    @Autowired
    //    private UserRepo userRepo;
    //
    //    @Autowired
    //    private PostRepo postRepo;

    @Autowired
    private UserService service;

    @Autowired
    private PostService postService;


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


        user1 = userFull_IdNull_ListIdPostsEmpty().create();
        user3 = userFull_IdNull_ListIdPostsEmpty().create();
        userWithIdForPost1Post2 = userWithID_IdPostsEmpty().create();
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


    private Flux<User> getUserFlux(List<User> listUser) {
        return service.deleteAll()
                      .thenMany(Flux.fromIterable(listUser))
                      .flatMap(service::save)
                      .doOnNext(item -> service.findAll());
    }

    @NotNull
    private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
        return service.deleteAll()
                       .thenMany(Flux.fromIterable(postList))
                       .flatMap(postService::saveEmbedObject)
                       .doOnNext(item -> service.findAll())
                       .doOnNext((item -> System.out.println(
                               "\nService - Post-ID: " + item.getId() +
                                       "|Author: " + item.getAuthor() + "\n")));
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
        final Flux<User> userFlux = getUserFlux(userList);

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
        final Flux<User> userFlux = getUserFlux(userList);

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
                .body("$",hasSize(2))
                .body("[0]",hasKey("id"))
                .body("[0]",hasKey("name"))
                .body("[0]",hasKey("email"))
                .body("[0]",CoreMatchers.not(hasKey("idPosts")))
                .body("[1]",hasKey("id"))
                .body("[1]",hasKey("name"))
                .body("[1]",hasKey("email"))
                .body("[1]",CoreMatchers.not(hasKey("idPosts")))
        //                .body("id" ,hasItems(user1.getId(),user3.getId()))
        //                .body("name" ,hasItems(user1.getName(),user3.getName()))
        //                .body("email" ,hasItems(user1.getEmail(),user3.getEmail()))
        ;
    }


    @Test
    void findById() {
        final Flux<User> userFlux = getUserFlux(userList);

        StepVerifierCountUserFlux(userFlux,2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

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
                .contentType(ContentType.JSON)
                .body("id",containsStringIgnoringCase(userWithIdForPost1Post2.getId()))
                .body("email",containsStringIgnoringCase(userWithIdForPost1Post2.getEmail()))
                .body("name",containsStringIgnoringCase(userWithIdForPost1Post2.getName()))
        ;

        StepVerifierCountUserFlux(service.findAll(),1);
    }


    @Test
    void delete() {
        Flux<User> userFlux = getUserFlux(userList);

        StepVerifierCountUserFlux(userFlux,2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .header("Accept",ContentType.ANY)
                .header("Content-type",ContentType.JSON)
                .body(user1)

                .when()
                .delete(REQ_USER)

                .then()
                .log()
                .headers()
                .statusCode(NO_CONTENT.value())
        ;

        StepVerifierCountUserFlux(service.findAll(),1);
    }


    @Test
    void update() {
        final Flux<User> userFlux = getUserFlux(userList);

        StepVerifierCountUserFlux(userFlux,2);

        var previousEmail = user1.getEmail();

        var currentEmail = Faker.instance()
                                .internet()
                                .emailAddress();

        user1.setEmail(currentEmail);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .header("Accept",ContentType.ANY)
                .header("Content-type",ContentType.JSON)
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
                .contentType(ContentType.JSON)

                .body("id",equalTo(user1.getId()))
                .body("name",equalTo(user1.getName()))
                .body("email",equalTo(currentEmail))
                .body("email",not(equalTo(previousEmail)))
        ;
    }


    @Test
    void findPostsByUserId() {
        userWithIdForPost1Post2 = userWithID_IdPostsEmpty().create();

        post1 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
        post2 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
        List<Post> postList = Arrays.asList(post1,post2);

        cleanDbToTest();

        StepVerifier
                .create(service.save(userWithIdForPost1Post2))
                .expectSubscription()
                .expectNext(userWithIdForPost1Post2)
                .verifyComplete();

        StepVerifier
                .create(service.findAll())
                .expectSubscription()
                .expectNextMatches(user -> userWithIdForPost1Post2.getId()
                                                                  .equals(user.getId()))
                .verifyComplete();

        Flux<Post> postFluxPost1Post2 = cleanDb_Saving02Posts_GetThemInAFlux(postList);

        StepVerifier
                .create(postFluxPost1Post2)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
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