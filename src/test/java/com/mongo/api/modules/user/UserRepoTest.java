package com.mongo.api.modules.user;

import com.mongo.testcontainer.compose.ConfigComposeTests;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static com.mongo.databuilders.UserBuilder.userFull_IdNull_ListIdPostsEmpty;

@Slf4j
public class UserRepoTest extends ConfigComposeTests {

    private User user1, user2;
    private List<User> userList;

    @Container
    private static final DockerComposeContainer<?> compose = new ConfigComposeTests().compose;


    @Autowired
    private UserRepo repo;


    @BeforeAll
    public static void beforeAll() {
        ConfigComposeTests.beforeAll();
        ConfigComposeTests.bHWorks();
    }


    @AfterAll
    public static void afterAll() {
        compose.close();
        ConfigComposeTests.afterAll();
    }


    @BeforeEach
    void beforeEach() {
        user1 = userFull_IdNull_ListIdPostsEmpty().create();
        user2 = userFull_IdNull_ListIdPostsEmpty().create();
        userList = Arrays.asList(user1,user2);
    }


    @AfterEach
    void afterEach() {
        repo.deleteAll();
    }


    @Test
    public void save_simple() {
        StepVerifier
                .create(repo.save(user1))
                .expectSubscription()
                .expectNext(user1)
                .verifyComplete();
    }


    @Test
    public void save_checkId() {
        Mono<User> savedItem = repo.save(user1);

        StepVerifier.create(savedItem.log("savedItem : "))
                    .expectSubscription()
                    .expectNextMatches(
                            item -> (item.getId() != null &&
                                    item.getName()
                                        .equals(user1.getName())))
                    .verifyComplete();
    }


    @Test
    public void save_deleteAll() {

        Mono<User> savedItem =
                repo
                        .deleteAll()
                        .then(
                                Mono.just(user1)
                                    .flatMap(repo::save));
//                        .thenMany(repo.findAll())
//                        .subscribe(item -> log.info("saving " + item.toString()));


                StepVerifier.create(savedItem.log("savedItem : "))
                            .expectSubscription()
                            .expectNextMatches(
                                    item -> (item.getId() != null &&
                                            item.getName()
                                                .equals(user1.getName())))
                            .verifyComplete();
    }


    @Test
    public void findAll() {
        StepVerifier
                .create(repo.findAll())
                .expectSubscription()
                .expectNextCount(2)
                .verifyComplete();
    }

}