package com.mongo.api.core.exceptions;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserService;
import io.restassured.http.ContentType;
import io.restassured.module.webtestclient.RestAssuredWebTestClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;
import static utils.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static utils.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static utils.databuilders.UserBuilder.userWithID_IdPostsEmpty;

public class ExceptionsUserResourceTest extends ConfigControllerTests {

    private User user1, user3, userPostsOwner, userItemTest;
    private Post post1, post2;
    private List<User> userList;

    private Environment env;

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
    private UserService userService;

    @Autowired
    private PostRepo postRepo;


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

        user1 = userWithID_IdPostsEmpty().create();
        user3 = userFull_IdNull_ListIdPostsEmpty().create();
        userPostsOwner = userWithID_IdPostsEmpty().create();
        userItemTest = userWithID_IdPostsEmpty().create();
        userList = Arrays.asList(user1,user3);
    }


    void cleanDbToTest() {
        StepVerifier
                .create(userService.deleteAll())
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


    private void StepVerifierCountPostFlux(Flux<Post> flux,int totalElements) {
        StepVerifier
                .create(flux)
                .expectSubscription()
                .expectNextCount(totalElements)
                .verifyComplete();
    }


    private Flux<User> saveAndGetUserFlux(List<User> listUser) {
        return userService.deleteAll()
                          .thenMany(Flux.fromIterable(listUser))
                          .flatMap(userService::save)
                          .doOnNext(item -> userService.findAll())
                          .doOnNext((item -> System.out.println(
                                  "\nUser-Service|User-ID: " + item.getId() +
                                          "|User-Name: " + item.getName() +
                                          "|User-Email: " + item.getEmail() + "\n")));
    }


    @NotNull
    private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
        return postRepo.deleteAll()
                       .thenMany(Flux.fromIterable(postList))
                       .flatMap(postRepo::save)
                       .doOnNext(item -> postRepo.findAll())
                       .doOnNext((item -> System.out.println(
                               "\nPost-Repo:" + "\n" +
                                       "Post-ID: " + item.getId() +
                                       "|Post-Title: " + item.getTitle() + "\n" +
                                       "Author-ID: " + item.getAuthor()
                                                           .getId() +
                                       "|Author-Name: " + item.getAuthor()
                                                              .getName() + "\n")));
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
    void findById() {

        final Flux<User> userFlux = saveAndGetUserFlux(userList);

        StepVerifierCountUserFlux(userFlux,2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)
                .header("Accept",ANY)
                .header("Content-type",JSON)
                .body(userItemTest)

                .when()
                .get(REQ_USER + FIND_USER_BY_ID,Faker.instance()
                                                     .idNumber()
                                                     .valid())

                .then()
                .statusCode(NOT_FOUND.value())

                .body("AtribMessage",equalTo("CustomExc: (Service) User not Found"))
                .body("devAtribMsg",equalTo("Generic Exception"))
                .log()
        ;
    }


    @Test
    void delete() {
        Flux<User> userFlux = saveAndGetUserFlux(userList);

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

        StepVerifierCountUserFlux(userService.findAll(),1);
    }


    @Test
    void update() {
        final Flux<User> userFlux = saveAndGetUserFlux(userList);

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

        post1 = post_IdNull_CommentsEmpty(userPostsOwner).create();
        post2 = post_IdNull_CommentsEmpty(userPostsOwner).create();
        List<Post> postList = Arrays.asList(post1,post2);

        StepVerifier
                .create(userService.save(userPostsOwner))
                .expectSubscription()
                .expectNext(userPostsOwner)
                .verifyComplete();


        Flux<Post> postFluxPost1Post2 = cleanDb_Saving02Posts_GetThemInAFlux(postList);

        StepVerifierCountPostFlux(postFluxPost1Post2,2);

        RestAssuredWebTestClient
                .given()
                .webTestClient(mockedWebClient)

                .when()
                .get(REQ_USER + FIND_POSTS_BY_USERID,userPostsOwner.getId())


                .then()
                .statusCode(OK.value())
                .log()
                .headers()
                .and()
                .log()

                .body()
                .body("title",hasItems(post1.getTitle(),post2.getTitle()))
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

//        RestAssuredWebTestClient
//                .given()
//                .webTestClient(webTestClient)
//                .header(RoleUsersHeaders.role_admin_header)
//                .body(anime_1)
//
//                .when()
//                .put("/{id}" ,"300")

//                .then()
//                .statusCode(NOT_FOUND.value())
//
//                .body("developerMensagem" ,equalTo("A ResponseStatusException happened!!!"))
//                .body("name" ,nullValue())