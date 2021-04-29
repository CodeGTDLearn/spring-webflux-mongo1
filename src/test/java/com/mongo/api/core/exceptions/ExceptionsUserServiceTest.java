package com.mongo.api.core.exceptions;

import com.mongo.api.core.exceptions.custom.exceptions.UserNotFoundException;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.api.modules.user.User;
import com.mongo.api.modules.user.UserRepo;
import com.mongo.api.modules.user.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import utils.testcontainer.compose.ConfigComposeTests;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static utils.databuilders.UserBuilder.*;


public class ExceptionsUserServiceTest extends ConfigComposeTests {

    private User user1, user3, userWithIdForPost1Post2;
    private Post post1, post2;
    private List<User> userList;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepo postRepo;

    private UserService userService;

    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    @BeforeAll
    public static void beforeAll() {
        ConfigComposeTests.beforeAll();
    }


    @AfterAll
    public static void afterAll() {
        ConfigComposeTests.afterAll();
        compose.close();
    }


    @BeforeEach
    void beforeEach() {
        userService = new UserService(userRepo,postRepo);

        user1 = userFull_IdNull_ListIdPostsEmpty().create();
        user3 = userFull_IdNull_ListIdPostsEmpty().create();
        userList = Arrays.asList(user1,user3);
    }


    private void cleanDbToTest() {

        StepVerifier
                .create(userService.deleteAll())
                .expectSubscription()
                .verifyComplete();

        System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                                   " <==================\n\n");
    }


    @NotNull
    private Flux<User> cleanDb_Saving02Users_GetThemInAFlux(List<User> userList) {
        return userService.deleteAll()
                          .thenMany(Flux.fromIterable(userList))
                          .flatMap(userService::save)
                          .doOnNext(item -> userService.findAll())
                          .doOnNext((item -> System.out.println(
                                  "\nService - UserID: " + item.getId() +
                                          "|Name: " + item.getName() +
                                          "|Email: " + item.getEmail() + "\n")));
    }


    @NotNull
    private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
        return postRepo.deleteAll()
                       .thenMany(Flux.fromIterable(postList))
                       .flatMap(postRepo::save)
                       .doOnNext(item -> postRepo.findAll())
                       .doOnNext((item -> System.out.println(
                               "\nUserRepo - Post-ID: " + item.getId() +
                                       "|Author: " + item.getAuthor() + "\n")));
    }


    @Test
    @DisplayName("Check TestContainerServices")
    void checkServices() {
        super.checkTestcontainerComposeService(
                compose,
                ConfigComposeTests.SERVICE,
                ConfigComposeTests.SERVICE_PORT
                                              );
    }


    @Test
    @DisplayName("findById: UserNotFoundException")
    void findByIdErrorUserNotFound() {
        cleanDbToTest();

        Mono<User> itemFoundById =
                userService
                        .findById(createFakeUniqueRandomId())
                        .map(itemFound -> itemFound);

        StepVerifier
                .create(itemFoundById)
                .expectSubscription()
                .expectError(UserNotFoundException.class)
                .verify();

    }

    @Test
    @DisplayName("DeleteById: UserNotFoundException")
    void deleteByIdErrorUserNotFound() {
        cleanDbToTest();

        Mono<Void> itemFoundById =
                userService
                        .deleteById(createFakeUniqueRandomId());

        StepVerifier
                .create(itemFoundById)
                .expectSubscription()
                .expectError(UserNotFoundException.class)
                .verify();

    }

    @Test
    @DisplayName("findPostsByUserId: UserNotFoundException")
    void findPostsByUserIdErrorUserNotFound() {
        cleanDbToTest();

        Flux<Post> itemFoundById =
                userService
                        .findPostsByUserId(createFakeUniqueRandomId());

        StepVerifier
                .create(itemFoundById)
                .expectSubscription()
                .expectError(UserNotFoundException.class)
                .verify();

    }

    @Test
    @DisplayName("Update: UserNotFoundException")
    void updateErrorUserNotFound() {
        cleanDbToTest();

        Mono<User> itemFoundById =
                userService
                        .update(userWithID_IdPostsEmpty().create());

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