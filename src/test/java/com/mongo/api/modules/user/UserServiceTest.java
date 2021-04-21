package com.mongo.api.modules.user;

import com.github.javafaker.Faker;
import com.mongo.api.core.exceptions.custom.exceptions.UserNotFoundException;
import com.mongo.api.modules.post.Post;
import com.mongo.api.modules.post.PostRepo;
import com.mongo.testcontainer.compose.ConfigComposeTests;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongo.databuilders.PostBuilder.post_IdNull_CommentsEmpty;
import static com.mongo.databuilders.UserBuilder.*;

public class UserServiceTest extends ConfigComposeTests {

    private User user1, user3, user2WithId;
    private Post post1, post2;
    private List<User> userList;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepo postRepo;

    private UserService service;

    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    @BeforeAll
    public static void beforeAll() {
        ConfigComposeTests.beforeAll();
    }


    @AfterAll
    public static void afterAll() {
        ConfigComposeTests.afterAll();
    }


    @BeforeEach
    void setUp() {
        service = new UserService(userRepo,postRepo);

        user1 = userFull_IdNull_ListIdPostsEmpty().create();
        user3 = userFull_IdNull_ListIdPostsEmpty().create();
        userList = Arrays.asList(user1,user3);
    }


    private void cleanDbToTest() {

        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        System.out.println("\n\n==================> CLEAN-DB-TO-TEST" +
                                   " <==================\n\n");
    }


    @NotNull
    private Flux<User> cleanDb_Saving02Users_GetThemInAFlux(List<User> userList) {
        return service.deleteAll()
                      .thenMany(Flux.fromIterable(userList))
                      .flatMap(service::save)
                      .doOnNext(item -> service.findAll());
        //                      .doOnNext((item -> System.out.println(
        //                              "\nService - UserID: " + item.getId() +
        //                                      "|Name: " + item.getName() +
        //                                      "|Email: " + item.getEmail() + "\n")));
    }


    @NotNull
    private Flux<Post> cleanDb_Saving02Posts_GetThemInAFlux(List<Post> postList) {
        return postRepo.deleteAll()
                       .thenMany(Flux.fromIterable(postList))
                       .flatMap(postRepo::save)
                       .doOnNext(item -> postRepo.findAll());
        //                       .doOnNext((item -> System.out.println(
        //                               "\nRepo - Post-ID: " + item.getId() +
        //                                       "|Author: " + item.getAuthor()+ "\n")));
    }


    //    @Test
    //    @DisplayName("Check TestContainerServices")
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
                .expectNext(user1)
                .expectNext(user3)
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
                service
                        .findById(user1.getId())
                        .map(itemFound -> itemFound);

        StepVerifier
                .create(itemFoundById)
                .expectSubscription()
                .expectNextMatches(found -> found.getId()
                                                 .equals(user1.getId()))
                .verifyComplete();
    }


    //    @Test
    //    @DisplayName("FindById: Error-ResponseStatusException")
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


//    @Test
//    @DisplayName("Save: Object")
    void save() {
        cleanDbToTest();

        StepVerifier
                .create(service.save(user3))
                .expectSubscription()
                .expectNext(user3)
                .verifyComplete();

    }


    //    @DisplayName("Delete: Count")
    //    @Test
    public void deleteAll_count() {

        StepVerifier
                .create(service.deleteAll())
                .expectSubscription()
                .verifyComplete();

        Flux<User> fluxTest = service.findAll();

        StepVerifier
                .create(fluxTest)
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();

    }


    //        @DisplayName("DeleteById")
    //        @Test
    public void deleteById() {
        final Flux<User> userFlux = cleanDb_Saving02Users_GetThemInAFlux(userList);

        StepVerifier
                .create(userFlux)
                .expectNext(user1)
                .expectNext(user3)
                .verifyComplete();

        Mono<Void> deletedItem =
                service.findById(user1.getId())
                       .map(User::getId)
                       .flatMap(id -> service.deleteById(id));

        StepVerifier
                .create(deletedItem.log())
                .expectSubscription()
                .verifyComplete();

        StepVerifier
                .create(service.findAll()
                               .log("The new item list : "))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }


    //    @DisplayName("update")
    //    @Test
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
                service
                        .findById(user1.getId())
                        .map(itemFound -> {
                            itemFound.setName(newName);
                            return itemFound;
                        })
                        .flatMap(itemToBeUpdated -> service.save(itemToBeUpdated));

        StepVerifier
                .create(updatedItem)
                .expectSubscription()
                .expectNextMatches(user -> user.getName()
                                               .equals(newName))
                .verifyComplete();

        StepVerifier
                .create(service.findAll()
                               .log("The new item list : "))
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }


    @DisplayName("findPostsByUserId")
    @Test
    void findPostsByUserId() {
        user2WithId = userWithID_ListIdPostsEmpty().create();

        post1 = post_IdNull_CommentsEmpty(user2WithId).create();
        post2 = post_IdNull_CommentsEmpty(user2WithId).create();
        List<Post> postList = Arrays.asList(post1,post2);

        cleanDbToTest();

        StepVerifier
                .create(service.save(user2WithId))
                .expectSubscription()
                .expectNext(user2WithId)
                .verifyComplete();

        StepVerifier
                .create(service.findAll())
                .expectSubscription()
                .expectNextMatches(user -> user2WithId.getId()
                                                      .equals(user.getId()))
                .verifyComplete();

        Flux<Post> postFlux = cleanDb_Saving02Posts_GetThemInAFlux(postList);

        StepVerifier
                .create(postFlux)
                .expectSubscription()
                .expectNext(post1)
                .expectNext(post2)
                .verifyComplete();

        Flux<Post> postFluxByUserID = service.findPostsByUserId(user2WithId.getId());

        //<<<<<<<<<<<<< ERRO A SER INVESTIGADO
        StepVerifier
                .create(postFluxByUserID)
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


    //    @Test
    //    @DisplayName("BHWorks")
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