package com.mongo.api.core.exceptions;

import com.mongo.api.core.exceptions.custom.exceptions.UserNotFoundException;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import utils.testcontainer.compose.ConfigComposeTests;
import utils.testcontainer.compose.ConfigControllerTests;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static utils.databuilders.UserBuilder.*;

public class ExceptionsTest extends ConfigControllerTests {

    private User user1, user3, userWithIdForPost1Post2;
    private Post post1, post2;
    private List<User> userList;


    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    // MOCKED-SERVER: WEB-TEST-CLIENT(non-blocking client)'
    // SHOULD BE USED WITH 'TEST-CONTAINERS'
    // BECAUSE THERE IS NO 'REAL-SERVER' CREATED VIA DOCKER-COMPOSE
    @Autowired
    WebTestClient mockedWebClient;


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


    @Test
    void checkServices() {
        new ConfigComposeTests().checkTestcontainerComposeService(
                compose,
                ConfigComposeTests.SERVICE,
                ConfigComposeTests.SERVICE_PORT
                                                                 );
    }


    @Test
    @DisplayName("FindById: Error-UserNotFound")
    void findErrorUserNotFound() {
    }


    @Test
    @DisplayName("FindById: Error-ResponseStatusException")
    void findByIdErrorUserNotFound() {
        cleanDbToTest();

        Mono<User> itemFoundById =
                service
                        .findById(createFakeUniqueRandomId())
                        .map(itemFound -> itemFound);

        StepVerifier
                .create(itemFoundById)
                .expectSubscription()
                .expectError(UserNotFoundException.class)
                .verify();

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