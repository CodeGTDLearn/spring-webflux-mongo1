package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.api.modules.post.Post;
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

import static utils.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static utils.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;
import static utils.databuilders.UserBuilder.userWithID_IdPostsEmpty;


public class UserServiceTest extends ConfigComposeTests {

    private User user1, user3, userWithIdForPost1Post2;
    private Post post1, post2;
    private List<User> userList;

    @Autowired
    private UserServiceInt userService;

    @Autowired
    private PostRepo postRepo;

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
        // service = new UserService(userRepo,postRepo);

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
    @DisplayName("FindAll")
    void findAll() {
        final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

        StepVerifier
                .create(userFlux)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }


    @Test
    @DisplayName("FindById")
    void findById() {
        final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

        StepVerifier
                .create(userFlux)
                .expectNext(user1)
                .expectNext(user3)
                .verifyComplete();

        Mono<User> itemFoundById =
                userService
                        .findById(user1.getId())
                        .map(itemFound -> itemFound);

        StepVerifier
                .create(itemFoundById)
                .expectSubscription()
                .expectNextMatches(found -> found.getId()
                                                 .equals(user1.getId()))
                .verifyComplete();
    }


    @Test
    @DisplayName("Save: Object")
    void save() {
        cleanDbToTest();

        StepVerifier
                .create(userService.save(user3))
                .expectSubscription()
                .expectNext(user3)
                .verifyComplete();

    }


    @DisplayName("Delete: Count")
    @Test
    public void deleteAll_count() {

        StepVerifier
                .create(userService.deleteAll())
                .expectSubscription()
                .verifyComplete();

        Flux<User> fluxTest = userService.findAll();

        StepVerifier
                .create(fluxTest)
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();

    }


    @DisplayName("DeleteById")
    @Test
    public void deleteById() {
        final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

        StepVerifier
                .create(userFlux)
                .expectNext(user1)
                .expectNext(user3)
                .verifyComplete();

        Mono<Void> deletedItem =
                userService.findById(user1.getId())
                           .map(User::getId)
                           .flatMap(id -> userService.delete(id));

        StepVerifier
                .create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();

        StepVerifier
                .create(userService.findAll()
                                   .log("The new item list : "))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }


    @DisplayName("update")
    @Test
    public void update() {
        final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

        StepVerifier
                .create(userFlux)
                .expectNext(user1)
                .expectNext(user3)
                .verifyComplete();

        var newName = Faker.instance()
                           .name()
                           .fullName();

        Mono<User> updatedItem =
                userService
                        .findById(user1.getId())
                        .map(itemFound -> {
                            itemFound.setName(newName);
                            return itemFound;
                        })
                        .flatMap(itemToBeUpdated -> userService.save(itemToBeUpdated));

        StepVerifier
                .create(updatedItem)
                .expectSubscription()
                .expectNextMatches(user -> user.getName()
                                               .equals(newName))
                .verifyComplete();

        StepVerifier
                .create(userService.findAll()
                                   .log("The new item list : "))
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }


    @DisplayName("findPostsByUserId")
    @Test
    void findPostsByUserId() {
        userWithIdForPost1Post2 = userWithID_IdPostsEmpty().create();

        post1 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
        post2 = post_IdNull_CommentsEmpty(userWithIdForPost1Post2).create();
        List<Post> postList = Arrays.asList(post1,post2);

        cleanDbToTest();

        StepVerifier
                .create(userService.save(userWithIdForPost1Post2))
                .expectSubscription()
                .expectNext(userWithIdForPost1Post2)
                .verifyComplete();

        StepVerifier
                .create(userService.findAll())
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

        Flux<Post> postFluxPost1Post2ByUserID = userService.findPostsByUserId(
                userWithIdForPost1Post2.getId());

        StepVerifier
                .create(postFluxPost1Post2ByUserID)
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier
                .create(postRepo.findAll())
                .expectSubscription()
                .expectNextMatches(post -> post1.getId()
                                                .equals(post.getId()))
                .expectNextMatches(post -> post2.getId()
                                                .equals(post.getId()))
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